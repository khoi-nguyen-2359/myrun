package akio.apps.myrun.data.activity.impl

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.ActivityTcxFileWriter
import akio.apps.myrun.data.activity.api.locationparser.LocationDataPointParserFactory
import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityStorageData
import akio.apps.myrun.data.activity.api.model.ActivitySyncData
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.AthleteInfo
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.DataPointVersion
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.activity.di.ActivityDataScope
import akio.apps.myrun.data.activity.impl.model.LocalActivityData
import akio.apps.myrun.data.activity.impl.model.LocalAthleteInfo
import akio.apps.myrun.data.activity.impl.model.LocalBaseActivity
import akio.apps.myrun.data.activity.impl.model.LocalCyclingActivity
import akio.apps.myrun.data.activity.impl.model.LocalRunningActivity
import akio.apps.myrun.data.user.api.model.PlaceIdentifier
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import java.io.BufferedOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.protobuf.ProtoBuf
import timber.log.Timber

private val Context.prefDataStore:
    DataStore<Preferences> by preferencesDataStore("activity_local_storage_prefs")

@OptIn(ExperimentalSerializationApi::class)
@Singleton
@ContributesBinding(ActivityDataScope::class)
class ActivityLocalStorageImpl @Inject constructor(
    private val application: Application,
    private val activityTcxFileWriter: ActivityTcxFileWriter,
) : ActivityLocalStorage {
    private val prefDataStore: DataStore<Preferences> = application.prefDataStore
    private val protoBuf = ProtoBuf {
        val module = SerializersModule {
            polymorphic(
                LocalBaseActivity::class,
                LocalRunningActivity::class,
                LocalRunningActivity.serializer()
            )
            polymorphic(
                LocalBaseActivity::class,
                LocalCyclingActivity::class,
                LocalCyclingActivity.serializer()
            )
        }
        serializersModule = module
    }

    override suspend fun storeActivityData(
        activity: BaseActivityModel,
        locations: List<ActivityLocation>,
        routeBitmap: Bitmap,
    ) = coroutineScope {
        Timber.d("==== [START] STORE ACTIVITY DATA =====")
        val activityDirectory = createActivityStorageDirectory(activity.id)
        val writeInfoAsync = async {
            val activityInfo = activity.toActivityInfo()
            val activityInfoBytes = protoBuf.encodeToByteArray(activityInfo)
            activityDirectory.infoFile.bufferedWriteByteArray(activityInfoBytes)
            Timber.d("Stored info at ${activityDirectory.infoFile}")
        }
        val writeLocationsAsync = async {
            val parser = LocationDataPointParserFactory.getWriteParser()
            val serializedLocations = parser.flatten(locations)
            val locationsBytes = protoBuf.encodeToByteArray(serializedLocations)
            activityDirectory.locationsFile.bufferedWriteByteArray(locationsBytes)
            Timber.d("Stored locations at ${activityDirectory.locationsFile}")
        }
        val writeRouteImageAsync = async {
            BufferedOutputStream(activityDirectory.routeBitmapFile.outputStream()).use { os ->
                routeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            }
            Timber.d("Stored route image at ${activityDirectory.routeBitmapFile}")
        }
        writeInfoAsync.join()
        writeLocationsAsync.join()
        writeRouteImageAsync.join()
        refreshActivityStorageCount()
        Timber.d("==== [DONE] STORE ACTIVITY DATA =====")
    }

    override suspend fun storeActivitySyncData(
        activityModel: BaseActivityModel,
        activityLocations: List<ActivityLocation>,
    ) = coroutineScope {
        Timber.d("==== [START] STORE ACTIVITY SYNC DATA =====")
        val activitySyncDirectory = createActivitySyncDirectory(activityModel.id)
        val tcxWriterAsync = async {
            activityTcxFileWriter.writeTcxFile(
                activityModel,
                activityLocations,
                emptyList(),
                activitySyncDirectory.tcxFile,
                zip = false
            )
            Timber.d("Stored tcx file at ${activitySyncDirectory.tcxFile}")
        }
        val infoWriteAsync = async {
            val trackingInfo = activityModel.toActivityInfo()
            val infoBytes = protoBuf.encodeToByteArray(trackingInfo)
            activitySyncDirectory.infoFile.bufferedWriteByteArray(infoBytes)
            Timber.d("Stored info file at ${activitySyncDirectory.infoFile}")
        }
        tcxWriterAsync.join()
        infoWriteAsync.join()
        Timber.d("==== [DONE] STORE ACTIVITY SYNC DATA =====")
    }

    override suspend fun deleteActivityData(activityId: String) {
        createActivityStorageDirectory(activityId).storageDir.deleteRecursively()
        refreshActivityStorageCount()
    }

    private suspend fun loadActivityStorageData(
        activityId: String,
    ): ActivityStorageData {
        val activityDirectory = createActivityStorageDirectory(activityId)
        val activityInfoBytes = activityDirectory.infoFile.bufferedReadByteArray()
        val localActivityInfo = protoBuf.decodeFromByteArray<LocalBaseActivity>(activityInfoBytes)
        val activityModel = localActivityInfo.toActivity()

        val locationsBytes = activityDirectory.locationsFile.bufferedReadByteArray()
        val flattenLocations = protoBuf.decodeFromByteArray<List<Double>>(locationsBytes)
        val parser = LocationDataPointParserFactory.getParser(localActivityInfo.version)
        val activityLocations = parser.build(flattenLocations)

        return ActivityStorageData(
            activityModel,
            activityLocations,
            activityDirectory.routeBitmapFile
        )
    }

    override fun getActivityStorageDataCountFlow(): Flow<Int> {
        return prefDataStore.data.map { data ->
            return@map data[KEY_ACTIVITY_STORAGE_DATA_COUNT]
                ?: 0
        }
    }

    private suspend fun refreshActivityStorageCount() {
        val activityStorageRootDir = createActivityStorageRootDir()
        setActivityStorageDataCount(
            activityStorageRootDir.list()?.size
                ?: 0
        )
    }

    private suspend fun setActivityStorageDataCount(count: Int) {
        prefDataStore.edit { data -> data[KEY_ACTIVITY_STORAGE_DATA_COUNT] = count }
    }

    override suspend fun loadAllActivityStorageDataFlow(): Flow<ActivityStorageData> {
        val storageRootDir = createActivityStorageRootDir()
        val listSize = storageRootDir.list()?.size
            ?: 0
        setActivityStorageDataCount(listSize)
        Timber.d("loadAllActivityStorageDataFlow = $listSize")
        return storageRootDir.list().orEmpty()
            .asFlow()
            .map(::loadActivityStorageData)
    }

    private fun File.bufferedWriteByteArray(content: ByteArray) =
        outputStream().buffered().use { os ->
            os.write(content)
        }

    private fun File.bufferedReadByteArray(): ByteArray =
        inputStream().buffered().use { ins ->
            ins.readBytes()
        }

    private fun createActivityStorageRootDir(): File =
        File("${application.filesDir}/$PATH_STORAGE_DIR")

    override fun loadAllActivitySyncDataFlow(): Flow<ActivitySyncData> =
        createActivitySyncRootDir().list().orEmpty()
            .asFlow()
            .map(::loadActivitySyncData)

    override fun deleteActivitySyncData(activityId: String) {
        createActivitySyncDirectory(activityId).syncDir.deleteRecursively()
    }

    override suspend fun clearAll() {
        createActivityStorageRootDir().deleteRecursively()
        createActivitySyncRootDir().deleteRecursively()
        prefDataStore.edit { it.clear() }
    }

    private fun loadActivitySyncData(activityId: String): ActivitySyncData {
        val activitySyncDirectory = createActivitySyncDirectory(activityId)
        val infoBytes = activitySyncDirectory.infoFile.bufferedReadByteArray()
        val activityInfo = protoBuf.decodeFromByteArray<LocalBaseActivity>(infoBytes)
        val activityModel = activityInfo.toActivity()
        return ActivitySyncData(activityModel, activitySyncDirectory.tcxFile)
    }

    private fun createActivityStorageDirectory(activityId: String): ActivityStorageDirectory {
        val activityStorageRoot = createActivityStorageRootDir()
        val activityDir =
            File("$activityStorageRoot/$activityId/").apply { mkdirs() }
        val infoFile = File("$activityDir/info")
        val locationsFile = File("$activityDir/locations")
        val routeImageFile = File("$activityDir/route_image")
        return ActivityStorageDirectory(activityDir, infoFile, locationsFile, routeImageFile)
    }

    private fun createActivitySyncDirectory(activityId: String): ActivitySyncDirectory {
        val syncDir = File("${createActivitySyncRootDir()}/$activityId/").apply { mkdirs() }
        return ActivitySyncDirectory(
            syncDir = syncDir,
            infoFile = File("$syncDir/info"),
            tcxFile = File("$syncDir/strava.tcx")
        )
    }

    private fun createActivitySyncRootDir(): File =
        File("${application.filesDir}/$PATH_SYNC_DIR")

    private fun BaseActivityModel.toActivityInfo(): LocalBaseActivity {
        val trackingInfoData = LocalActivityData(
            id,
            activityType.identity,
            name,
            routeImage,
            placeIdentifier?.toPlaceIdentifierString(),
            startTime,
            endTime,
            duration,
            distance,
            encodedPolyline,
            LocalAthleteInfo(athleteInfo.userId, athleteInfo.userName, athleteInfo.userAvatar)
        )

        return when (this) {
            is RunningActivityModel ->
                LocalRunningActivity(trackingInfoData, pace, cadence, DataPointVersion.max().value)
            is CyclingActivityModel ->
                LocalCyclingActivity(trackingInfoData, speed, DataPointVersion.max().value)
        }
    }

    private fun LocalBaseActivity.toActivity(): BaseActivityModel {
        val activityData = ActivityDataModel(
            id,
            ActivityType.from(activityType),
            name,
            routeImage,
            PlaceIdentifier.fromPlaceIdentifierString(placeIdentifier),
            startTime,
            endTime,
            duration,
            distance,
            encodedPolyline,
            AthleteInfo(
                athleteInfo.userId,
                athleteInfo.userName,
                athleteInfo.userAvatar
            )
        )

        return when (this) {
            is LocalRunningActivity -> RunningActivityModel(activityData, pace, cadence)
            is LocalCyclingActivity -> CyclingActivityModel(activityData, speed)
        }
    }

    data class ActivityStorageDirectory(
        val storageDir: File,
        val infoFile: File,
        val locationsFile: File,
        val routeBitmapFile: File,
    )

    data class ActivitySyncDirectory(
        val syncDir: File,
        val infoFile: File,
        val tcxFile: File,
    )

    companion object {
        private const val PATH_STORAGE_DIR = "activity/storage/"
        private const val PATH_SYNC_DIR = "activity/sync/"
        private val KEY_ACTIVITY_STORAGE_DATA_COUNT =
            intPreferencesKey("KEY_ACTIVITY_STORAGE_DATA_COUNT")
    }
}

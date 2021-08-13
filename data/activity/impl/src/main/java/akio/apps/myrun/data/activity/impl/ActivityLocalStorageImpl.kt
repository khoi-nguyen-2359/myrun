package akio.apps.myrun.data.activity.impl

import akio.apps.common.wiring.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.ActivityTcxFileWriter
import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.ActivityStorageData
import akio.apps.myrun.data.activity.api.model.ActivitySyncData
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.activity.impl.model.AthleteInfo
import akio.apps.myrun.data.activity.impl.model.CyclingTrackingActivityInfo
import akio.apps.myrun.data.activity.impl.model.RunningTrackingActivityInfo
import akio.apps.myrun.data.activity.impl.model.TrackingActivityInfo
import akio.apps.myrun.data.activity.impl.model.TrackingActivityInfoData
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.BufferedOutputStream
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.protobuf.ProtoBuf
import timber.log.Timber

private val Context.prefDataStore: DataStore<Preferences> by
preferencesDataStore("ActivityLocalStorageImpl")

class ActivityLocalStorageImpl @Inject constructor(
    private val application: Application,
    private val activityTcxFileWriter: ActivityTcxFileWriter,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ActivityLocalStorage {

    private val prefDataStore: DataStore<Preferences> = application.prefDataStore

    @OptIn(ExperimentalSerializationApi::class)
    private val protoBuf = ProtoBuf {
        val module = SerializersModule {
            polymorphic(
                TrackingActivityInfo::class,
                RunningTrackingActivityInfo::class,
                RunningTrackingActivityInfo.serializer()
            )
            polymorphic(
                TrackingActivityInfo::class,
                CyclingTrackingActivityInfo::class,
                CyclingTrackingActivityInfo.serializer()
            )
        }
        serializersModule = module
    }

    override suspend fun storeActivityData(
        activity: ActivityModel,
        locations: List<ActivityLocation>,
        routeBitmap: Bitmap
    ) = withContext(ioDispatcher) {
        Timber.d("==== [START] STORE ACTIVITY DATA =====")
        val activityDirectory = createActivityStorageDirectory(activity.id)

        val writeInfoAsync = async {
            val activityInfo = activity.toActivityInfo()
            val activityInfoBytes = protoBuf.encodeToByteArray(activityInfo)
            activityDirectory.infoFile.bufferedWriteByteArray(activityInfoBytes)
            Timber.d("Stored info at ${activityDirectory.infoFile}")
        }

        val writeLocationsAsync = async {
            val serializedLocations = serializeActivityLocations(locations)
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
        activityModel: ActivityModel,
        activityLocations: List<ActivityLocation>
    ) = withContext(ioDispatcher) {
        Timber.d("==== [START] STORE ACTIVITY SYNC DATA =====")
        val activitySyncDirectory = createActivitySyncDirectory(activityModel.id)
        val tcxWriterAsync = async {
            activityTcxFileWriter.writeTcxFile(
                activityModel,
                activityLocations,
                emptyList(),
                activitySyncDirectory.tcxFile,
                false
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
        activityId: String
    ): ActivityStorageData = withContext(ioDispatcher) {
        val activityDirectory = createActivityStorageDirectory(activityId)
        val activityModelDeferred = async {
            val activityInfoBytes = activityDirectory.infoFile.bufferedReadByteArray()
            val activityInfo = protoBuf.decodeFromByteArray<TrackingActivityInfo>(activityInfoBytes)
            activityInfo.toActivity()
        }
        val locationsDeferred = async {
            val locationsBytes = activityDirectory.locationsFile.bufferedReadByteArray()
            val flattenLocations = protoBuf.decodeFromByteArray<List<Double>>(locationsBytes)
            deserializeActivityLocation(activityId, flattenLocations)
        }

        ActivityStorageData(
            activityModelDeferred.await(),
            locationsDeferred.await(),
            activityDirectory.routeBitmapFile
        )
    }

    override fun getActivityStorageDataCountFlow(): Flow<Int> {
        return prefDataStore.data.map { data ->
            return@map data[KEY_ACTIVITY_STORAGE_DATA_COUNT] ?: 0
        }
    }

    private suspend fun refreshActivityStorageCount() {
        val activityStorageRootDir = createActivityStorageRootDir()
        setActivityStorageDataCount(activityStorageRootDir.list()?.size ?: 0)
    }

    private suspend fun setActivityStorageDataCount(count: Int) {
        prefDataStore.edit { data -> data[KEY_ACTIVITY_STORAGE_DATA_COUNT] = count }
    }

    override suspend fun loadAllActivityStorageDataFlow(): Flow<ActivityStorageData> {
        val storageRootDir = createActivityStorageRootDir()
        val listSize = storageRootDir.list()?.size ?: 0
        setActivityStorageDataCount(listSize)
        Timber.d("loadAllActivityStorageDataFlow = $listSize")
        return storageRootDir.list().orEmpty()
            .asFlow()
            .map(::loadActivityStorageData)
            .flowOn(ioDispatcher)
    }

    private fun serializeActivityLocations(locations: List<ActivityLocation>): List<Double> =
        locations.flatMap { listOf(it.time.toDouble(), it.latitude, it.longitude, it.latitude) }

    private fun deserializeActivityLocation(
        activityId: String,
        flattenList: List<Double>
    ): List<ActivityLocation> = flattenList.chunked(4).flatMap {
        listOf(
            ActivityLocation(
                activityId = activityId,
                time = it[0].toLong(),
                latitude = it[1],
                longitude = it[2],
                altitude = it[3]
            )
        )
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
            .flowOn(ioDispatcher)

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
        val activityInfo = protoBuf.decodeFromByteArray<TrackingActivityInfo>(infoBytes)
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

    private fun ActivityModel.toActivityInfo(): TrackingActivityInfo {
        val trackingInfoData = TrackingActivityInfoData(
            id,
            activityType.identity,
            name,
            routeImage,
            placeIdentifier,
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
            is RunningActivityModel ->
                RunningTrackingActivityInfo(trackingInfoData, pace, cadence)
            is CyclingActivityModel ->
                CyclingTrackingActivityInfo(trackingInfoData, speed)
            else -> throw Exception("")
        }
    }

    private fun TrackingActivityInfo.toActivity(): ActivityModel {
        val activityData = ActivityDataModel(
            id,
            ActivityType.from(activityType),
            name,
            routeImage,
            placeIdentifier,
            startTime,
            endTime,
            duration,
            distance,
            encodedPolyline,
            ActivityModel.AthleteInfo(
                athleteInfo.userId,
                athleteInfo.userName,
                athleteInfo.userAvatar
            )
        )

        return when (this) {
            is RunningTrackingActivityInfo ->
                RunningActivityModel(activityData, pace, cadence)
            is CyclingTrackingActivityInfo ->
                CyclingActivityModel(activityData, speed)
            else -> throw Exception("")
        }
    }

    data class ActivityStorageDirectory(
        val storageDir: File,
        val infoFile: File,
        val locationsFile: File,
        val routeBitmapFile: File
    )

    data class ActivitySyncDirectory(
        val syncDir: File,
        val infoFile: File,
        val tcxFile: File
    )

    companion object {
        private const val PATH_STORAGE_DIR = "activity/storage/"
        private const val PATH_SYNC_DIR = "activity/sync/"

        private val KEY_ACTIVITY_STORAGE_DATA_COUNT =
            intPreferencesKey("KEY_ACTIVITY_STORAGE_DATA_COUNT")
    }
}

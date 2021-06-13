package akio.apps.myrun.data.activitysharing.impl

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activity.ActivityTcxFileWriter
import akio.apps.myrun.data.activity.model.ActivityDataModel
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.activity.model.CyclingActivityModel
import akio.apps.myrun.data.activity.model.RunningActivityModel
import akio.apps.myrun.data.activitysharing.ActivityLocalStorage
import akio.apps.myrun.data.activitysharing.entity.AthleteInfo
import akio.apps.myrun.data.activitysharing.entity.CyclingTrackingActivityInfo
import akio.apps.myrun.data.activitysharing.entity.RunningTrackingActivityInfo
import akio.apps.myrun.data.activitysharing.entity.TrackingActivityInfo
import akio.apps.myrun.data.activitysharing.entity.TrackingActivityInfoData
import akio.apps.myrun.data.activitysharing.model.ActivityLocation
import akio.apps.myrun.data.activitysharing.model.ActivityStorageDataOutput
import android.app.Application
import android.graphics.Bitmap
import timber.log.Timber
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

@ExperimentalSerializationApi
class ActivityLocalStorageImpl @Inject constructor(
    private val application: Application,
    private val activityTcxFileWriter: ActivityTcxFileWriter,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ActivityLocalStorage {

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
            Timber.d("Stored info at ${activityDirectory.infoFile.absolutePath}")
        }

        val writeLocationsAsync = async {
            val serializedLocations = serializeActivityLocations(locations)
            val locationsBytes = protoBuf.encodeToByteArray(serializedLocations)
            activityDirectory.locationsFile.bufferedWriteByteArray(locationsBytes)
            Timber.d("Stored locations at ${activityDirectory.locationsFile.absolutePath}")
        }

        val writeRouteImageAsync = async {
            BufferedOutputStream(activityDirectory.routeBitmapFile.outputStream()).use { os ->
                routeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            }
            Timber.d("Stored route image at ${activityDirectory.routeBitmapFile.absolutePath}")
        }
        writeInfoAsync.join()
        writeLocationsAsync.join()
        writeRouteImageAsync.join()
        Timber.d("==== [DONE] STORE ACTIVITY DATA =====")
    }

    override suspend fun storeActivitySyncData(
        activityModel: ActivityModel,
        activityLocations: List<ActivityLocation>
    ) {
        val activitySyncDirectory = createActivitySyncDirectory(activityModel.id)
        activityTcxFileWriter.writeTcxFile(
            activityModel,
            activityLocations,
            emptyList(),
            activitySyncDirectory.tcxFile,
            false
        )
    }

    override suspend fun deleteActivityData(activityId: String) {
        val storageRootDir = createActivityStorageRootDir()
        File("${storageRootDir.absolutePath}/$activityId").deleteRecursively()
    }

    private suspend fun loadActivityStorageData(
        activityId: String
    ): ActivityStorageDataOutput = withContext(ioDispatcher) {
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

        ActivityStorageDataOutput(
            activityModelDeferred.await(),
            locationsDeferred.await(),
            activityDirectory.routeBitmapFile
        )
    }

    override suspend fun loadAllActivityStorageDataFlow(): Flow<ActivityStorageDataOutput> =
        createActivityStorageRootDir().list().orEmpty()
            .asFlow()
            .map(::loadActivityStorageData)
            .flowOn(ioDispatcher)

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

    private fun createActivityStorageDirectory(activityId: String): ActivityStorageDirectory {
        val activityStorageRoot = createActivityStorageRootDir()
        val activityDir =
            File("${activityStorageRoot.absolutePath}/$activityId/").apply { mkdirs() }
        val infoFile = File("${activityDir.absolutePath}/info")
        val locationsFile = File("${activityDir.absolutePath}/locations")
        val routeImageFile = File("${activityDir.absolutePath}/route_image")
        return ActivityStorageDirectory(activityDir, infoFile, locationsFile, routeImageFile)
    }

    private fun createActivitySyncDirectory(activityId: String): ActivitySyncDirectory {
        val syncDir = File("${application.filesDir}/activity/sync/$activityId/").apply { mkdirs() }
        return ActivitySyncDirectory(
            syncDir = syncDir,
            tcxFile = File("${syncDir.absolutePath}/$activityId.tcx")
        )
    }

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
        val tcxFile: File
    )

    companion object {
        private const val PATH_STORAGE_DIR = "activity/storage/"
    }
}

package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun._base.runfile.ClosableFileSerializer
import akio.apps.myrun._base.runfile.ZipFileSerializer
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.fitness.SingleDataPoint
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.feature.routetracking.usecase.SaveRouteTrackingActivityUsecaseImpl
import akio.apps.myrun.feature.strava.ExportActivityToFileUsecase
import akio.apps.myrun.feature.strava.UploadFileManager
import akio.apps.myrun.feature.strava._di.StravaConnectFeatureModule
import akio.apps.myrun.feature.usertimeline.model.Activity
import com.google.maps.android.SphericalUtil
import com.sweetzpot.tcxzpot.*
import com.sweetzpot.tcxzpot.builders.ActivityBuilder
import com.sweetzpot.tcxzpot.builders.LapBuilder
import com.sweetzpot.tcxzpot.builders.TrackpointBuilder
import com.sweetzpot.tcxzpot.builders.TrainingCenterDatabaseBuilder
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class ExportActivityToTcxFileUsecase @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val fitnessDataRepository: FitnessDataRepository,

    @Named(StravaConnectFeatureModule.NAME_STRAVA_UPLOAD_FILE_MANAGER)
    private val uploadFileManager: UploadFileManager
) : ExportActivityToFileUsecase {

    override suspend fun exportActivityToFile(activity: Activity, zip: Boolean): File {
        val startDate = Date(activity.startTime)
        val tempFile = uploadFileManager.createUploadFile(activity.id)
        val serializer = if (zip)
            ZipFileSerializer(tempFile, ".tcx")
        else
            ClosableFileSerializer(tempFile)

        val stepCadenceDataPoints: List<SingleDataPoint<Int>>? = if (activity.activityType == ActivityType.Running) {
            fitnessDataRepository.getSteppingCadenceDataPoints(activity.startTime, activity.endTime, SaveRouteTrackingActivityUsecaseImpl.FITNESS_DATA_INTERVAL)
        } else {
            null
        }
        val avgCadence: Int? = if (stepCadenceDataPoints == null)
            null
        else
            (stepCadenceDataPoints.sumOf { it.value } / stepCadenceDataPoints.size)

        val trackedLocations = routeTrackingLocationRepository.getAllLocations()

        val flattenCadences = mutableListOf<Int>()
        if (stepCadenceDataPoints != null) {
            var lastCadenceSearchIndex = 0
            trackedLocations.forEach { location ->
                val firstCadence = stepCadenceDataPoints.firstOrNull()
                val lastCadence = stepCadenceDataPoints.lastOrNull()
                if (firstCadence != null && location.time >= firstCadence.timestamp && lastCadence != null && location.time <= lastCadence.timestamp) {
                    for (i in lastCadenceSearchIndex until stepCadenceDataPoints.size - 1) {
                        if (location.time >= stepCadenceDataPoints[i].timestamp && location.time <= stepCadenceDataPoints[i + 1].timestamp) {
                            flattenCadences.add((stepCadenceDataPoints[i].value + stepCadenceDataPoints[i + 1].value) / 2)
                            lastCadenceSearchIndex = i + 1
                            return@forEach
                        }
                    }
                }

                flattenCadences.add(0)
            }
        }

        var currentDistance = 0.0
        var lastLocation: TrackingLocationEntity? = trackedLocations.firstOrNull()
        val sportType = when (activity.activityType) {
            ActivityType.Running -> Sport.RUNNING
            else -> Sport.BIKING
        }
        TrainingCenterDatabaseBuilder.trainingCenterDatabase()
            .withActivities(
                Activities.activities(
                    ActivityBuilder.activity(sportType)
                        .withID(TCXDate(startDate))
                        .withLaps(
                            LapBuilder.aLap(TCXDate(startDate))
                                .withTotalTime(activity.duration.toDouble())
                                .withDistance(activity.distance)
                                .withIntensity(Intensity.ACTIVE)
                                .withTriggerMethod(TriggerMethod.MANUAL)
                                .apply {
                                    avgCadence?.let { withCadence(Cadence.cadence(it)) }
                                }
                                .withTracks(
                                    Track.trackWith(
                                        trackedLocations.mapIndexed { index, waypoint ->
                                            TrackpointBuilder.aTrackpoint()
                                                .onTime(TCXDate(Date(waypoint.time)))
                                                .withPosition(Position.position(waypoint.latitude, waypoint.longitude))
                                                .withAltitude(waypoint.altitude)
                                                .withCadence(Cadence.cadence(flattenCadences[index]))
                                                .apply {
                                                    lastLocation?.let {
                                                        currentDistance += SphericalUtil.computeDistanceBetween(it.toGmsLatLng(), waypoint.toGmsLatLng())
                                                        lastLocation = waypoint
                                                        withDistance(currentDistance)
                                                    }
                                                }
                                                .build()
                                        }
                                    ))
                        )
                )
            )
            .build()
            .serialize(serializer)
        serializer.close()

        return tempFile
    }
}
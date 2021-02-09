package akio.apps.myrun.domain.strava

import akio.apps.myrun._base.runfile.ClosableFileSerializer
import akio.apps.myrun._base.runfile.ZipFileSerializer
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.activityfile.ActivityFileTrackingRepository
import akio.apps.myrun.data.activityfile.model.FileTarget
import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.fitness.SingleDataPoint
import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.domain.routetracking.SaveRouteTrackingActivityUsecase
import com.google.maps.android.SphericalUtil
import com.sweetzpot.tcxzpot.Activities
import com.sweetzpot.tcxzpot.Cadence
import com.sweetzpot.tcxzpot.Intensity
import com.sweetzpot.tcxzpot.Position
import com.sweetzpot.tcxzpot.Sport
import com.sweetzpot.tcxzpot.TCXDate
import com.sweetzpot.tcxzpot.Track
import com.sweetzpot.tcxzpot.TriggerMethod
import com.sweetzpot.tcxzpot.builders.ActivityBuilder
import com.sweetzpot.tcxzpot.builders.LapBuilder
import com.sweetzpot.tcxzpot.builders.TrackpointBuilder
import com.sweetzpot.tcxzpot.builders.TrainingCenterDatabaseBuilder
import java.io.File
import java.util.Date
import javax.inject.Inject

class ExportTrackingActivityToStravaFileUsecase @Inject constructor(
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val fitnessDataRepository: FitnessDataRepository,
    private val activityFileTrackingRepository: ActivityFileTrackingRepository
) {
    suspend fun export(activity: ActivityModel, zip: Boolean): File {
        val outputFile = activityFileTrackingRepository.createEmptyFile(activity.id)
        val startDate = Date(activity.startTime)
        val serializer = if (zip)
            ZipFileSerializer(outputFile, ".tcx")
        else
            ClosableFileSerializer(outputFile)

        val stepCadenceDataPoints: List<SingleDataPoint<Int>>? =
            if (activity.activityType == ActivityType.Running) {
                fitnessDataRepository.getSteppingCadenceDataPoints(
                    activity.startTime,
                    activity.endTime,
                    SaveRouteTrackingActivityUsecase.FITNESS_DATA_INTERVAL
                )
            } else {
                null
            }
        val avgCadence: Int? = if (stepCadenceDataPoints.isNullOrEmpty())
            null
        else
            stepCadenceDataPoints.sumOf { it.value } / stepCadenceDataPoints.size

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
                                .withCalories(0)
                                .apply {
                                    avgCadence?.let { withCadence(Cadence.cadence(it)) }
                                }
                                .withTracks(
                                    Track.trackWith(
                                        trackedLocations.mapIndexed { index, waypoint ->
                                            TrackpointBuilder.aTrackpoint()
                                                .onTime(TCXDate(Date(waypoint.time)))
                                                .withPosition(
                                                    Position.position(
                                                        waypoint.latitude,
                                                        waypoint.longitude
                                                    )
                                                )
                                                .withAltitude(waypoint.altitude)
                                                .withCadence(Cadence.cadence(flattenCadences[index]))
                                                .apply {
                                                    lastLocation?.let {
                                                        currentDistance += SphericalUtil.computeDistanceBetween(
                                                            it.toGmsLatLng(),
                                                            waypoint.toGmsLatLng()
                                                        )
                                                        lastLocation = waypoint
                                                        withDistance(currentDistance)
                                                    }
                                                }
                                                .build()
                                        }
                                    )
                                )
                        )
                )
            )
            .build()
            .serialize(serializer)
        serializer.close()

        activityFileTrackingRepository.track(
            activity.id,
            activity.name,
            outputFile,
            FileTarget.STRAVA_UPLOAD
        )

        return outputFile
    }
}

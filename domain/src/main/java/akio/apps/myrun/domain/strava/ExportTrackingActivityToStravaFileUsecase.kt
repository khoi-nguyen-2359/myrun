package akio.apps.myrun.domain.strava

import akio.apps.myrun._base.runfile.ClosableFileSerializer
import akio.apps.myrun._base.runfile.ZipFileSerializer
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.activityexport.ActivityFileTrackingRepository
import akio.apps.myrun.data.activityexport.ExportActivityLocationRepository
import akio.apps.myrun.data.activityexport.model.ActivityLocation
import akio.apps.myrun.data.activityexport.model.FileTarget
import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.fitness.SingleDataPoint
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.util.Date
import javax.inject.Inject

class ExportTrackingActivityToStravaFileUsecase @Inject constructor(
    private val fitnessDataRepository: FitnessDataRepository,
    private val activityFileTrackingRepository: ActivityFileTrackingRepository,
    private val exportActivityLocationRepository: ExportActivityLocationRepository,
    private val activityRepository: ActivityRepository,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(
        activity: ActivityModel,
        zip: Boolean
    ): File = coroutineScope {
        val stepCadenceDataPoints = async(ioDispatcher) {
            if (activity.activityType == ActivityType.Running) {
                fitnessDataRepository.getSteppingCadenceDataPoints(
                    activity.startTime,
                    activity.endTime,
                    SaveRouteTrackingActivityUsecase.FITNESS_DATA_INTERVAL
                )
            } else {
                null
            }
        }

        val exportActivityLocations = async(ioDispatcher) {
            getExportActivityLocations(activity)
        }

        val flattenCadences = createStepCadenceOnLocationDataPoint(
            stepCadenceDataPoints.await(),
            exportActivityLocations.await()
        )

        val outputFile = activityFileTrackingRepository.createEmptyFile(activity.id)
        writeTrainingFile(
            activity,
            exportActivityLocations.await(),
            flattenCadences,
            outputFile,
            zip
        )

        activityFileTrackingRepository.track(
            activity.id,
            activity.name,
            outputFile,
            FileTarget.STRAVA_UPLOAD
        )

        return@coroutineScope outputFile
    }

    private suspend fun getExportActivityLocations(
        activity: ActivityModel
    ): List<ActivityLocation> {
        // first try getting from export data
        val savedTrackingLocations =
            exportActivityLocationRepository.getActivityLocations(activity.id)
        return if (savedTrackingLocations.isEmpty()) {
            // then may fetch from activity data source
            activityRepository.getActivityLocationDataPoints(activity.id)
                .map { locationDataPoint ->
                    ActivityLocation(
                        activity.id,
                        locationDataPoint.timestamp,
                        locationDataPoint.value.latitude,
                        locationDataPoint.value.longitude,
                        locationDataPoint.value.altitude
                    )
                }
        } else {
            savedTrackingLocations
        }
    }

    private fun createStepCadenceOnLocationDataPoint(
        stepCadenceDataPoints: List<SingleDataPoint<Int>>?,
        trackedLocations: List<ActivityLocation>
    ): MutableList<Int> {
        val flattenCadences = mutableListOf<Int>()
        if (stepCadenceDataPoints != null) {
            var lastCadenceSearchIndex = 0
            trackedLocations.forEach { location ->
                val firstCadence = stepCadenceDataPoints.firstOrNull()
                val lastCadence = stepCadenceDataPoints.lastOrNull()
                if (firstCadence != null &&
                    location.time >= firstCadence.timestamp &&
                    lastCadence != null &&
                    location.time <= lastCadence.timestamp
                ) {
                    for (i in lastCadenceSearchIndex until stepCadenceDataPoints.size - 1) {
                        if (location.time >= stepCadenceDataPoints[i].timestamp &&
                            location.time <= stepCadenceDataPoints[i + 1].timestamp
                        ) {
                            flattenCadences.add((stepCadenceDataPoints[i].value + stepCadenceDataPoints[i + 1].value) / 2)
                            lastCadenceSearchIndex = i + 1
                            return@forEach
                        }
                    }
                }

                flattenCadences.add(0)
            }
        }
        return flattenCadences
    }

    private fun writeTrainingFile(
        activity: ActivityModel,
        locations: List<ActivityLocation>,
        cadences: MutableList<Int>,
        outputFile: File,
        zip: Boolean
    ) {
        val serializer = if (zip)
            ZipFileSerializer(outputFile, ".tcx")
        else
            ClosableFileSerializer(outputFile)

        val avgCadence: Int? = if (cadences.isEmpty())
            null
        else
            cadences.average().toInt()

        val startDate = Date(activity.startTime)
        val sportType = when (activity.activityType) {
            ActivityType.Running -> Sport.RUNNING
            else -> Sport.BIKING
        }
        var lastLocation: ActivityLocation? = locations.firstOrNull()
        var currentDistance = 0.0
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
                                        locations.mapIndexed { index, waypoint ->
                                            TrackpointBuilder.aTrackpoint()
                                                .onTime(TCXDate(Date(waypoint.time)))
                                                .withPosition(
                                                    Position.position(
                                                        waypoint.latitude,
                                                        waypoint.longitude
                                                    )
                                                )
                                                .withAltitude(waypoint.altitude)
                                                .withCadence(Cadence.cadence(cadences[index]))
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
    }
}

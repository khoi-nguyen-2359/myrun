package akio.apps.myrun.domain.strava

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.ActivityTcxFileWriter
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.activitysharing.ActivityFileTrackingRepository
import akio.apps.myrun.data.activitysharing.ActivityLocationCache
import akio.apps.myrun.data.activitysharing.model.ActivityLocation
import akio.apps.myrun.data.activitysharing.model.FileTarget
import akio.apps.myrun.data.fitness.FitnessDataRepository
import akio.apps.myrun.data.fitness.SingleDataPoint
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ExportTrackingActivityToStravaFileUsecase @Inject constructor(
    private val fitnessDataRepository: FitnessDataRepository,
    private val activityFileTrackingRepository: ActivityFileTrackingRepository,
    private val activityLocationCache: ActivityLocationCache,
    private val activityRepository: ActivityRepository,
    private val activityTcxFileWriter: ActivityTcxFileWriter,
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
                    3000L
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
        activityTcxFileWriter.writeTcxFile(
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
            activityLocationCache.getActivityLocations(activity.id)
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
                            val combinedCadence =
                                stepCadenceDataPoints[i].value + stepCadenceDataPoints[i + 1].value
                            flattenCadences.add(combinedCadence / 2)
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
}

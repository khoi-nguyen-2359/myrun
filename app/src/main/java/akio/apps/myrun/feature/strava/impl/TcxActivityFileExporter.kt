package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun._base.runfile.ClosableFileSerializer
import akio.apps.myrun._base.runfile.ZipFileSerializer
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun.data.activity.ActivityEntity
import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.data.fitness.SingleDataPoint
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.feature.strava.ActivityFileExporter
import android.content.Context
import com.google.maps.android.SphericalUtil
import com.sweetzpot.tcxzpot.*
import com.sweetzpot.tcxzpot.builders.ActivityBuilder
import com.sweetzpot.tcxzpot.builders.LapBuilder
import com.sweetzpot.tcxzpot.builders.TrackpointBuilder
import com.sweetzpot.tcxzpot.builders.TrainingCenterDatabaseBuilder
import java.io.File
import java.util.Date
import javax.inject.Inject

class TcxActivityFileExporter @Inject constructor(
    val appContext: Context
) : ActivityFileExporter {

    override fun export(
        outputFile: File,
        activity: ActivityEntity,
        locationDataPoints: List<SingleDataPoint<LocationEntity>>,
        speedDataPoints: List<SingleDataPoint<Float>>,
        stepCadenceDataPoints: List<SingleDataPoint<Int>>?,
        zip: Boolean
    ) {
        val startDate = Date(activity.startTime)
        val serializer = if (zip)
            ZipFileSerializer(outputFile, ".tcx")
        else
            ClosableFileSerializer(outputFile)

        val avgCadence: Int? = if (stepCadenceDataPoints == null)
            null
        else
            (stepCadenceDataPoints.sumOf { it.value } / stepCadenceDataPoints.size)

        val flattenCadences = mutableListOf<Int>()
        if (stepCadenceDataPoints != null) {
            var lastCadenceSearchIndex = 0
            locationDataPoints.forEach { location ->
                val firstCadence = stepCadenceDataPoints.firstOrNull()
                val lastCadence = stepCadenceDataPoints.lastOrNull()
                if (firstCadence != null && location.timestamp >= firstCadence.timestamp && lastCadence != null && location.timestamp <= lastCadence.timestamp) {
                    for (i in lastCadenceSearchIndex until stepCadenceDataPoints.size - 1) {
                        if (location.timestamp >= stepCadenceDataPoints[i].timestamp && location.timestamp <= stepCadenceDataPoints[i + 1].timestamp) {
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
        var lastLocation: SingleDataPoint<LocationEntity>? = locationDataPoints.firstOrNull()
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
                                        locationDataPoints.mapIndexed { index, waypoint ->
                                            TrackpointBuilder.aTrackpoint()
                                                .onTime(TCXDate(Date(waypoint.timestamp)))
                                                .withPosition(
                                                    Position.position(
                                                        waypoint.value.latitude,
                                                        waypoint.value.longitude
                                                    )
                                                )
                                                .withAltitude(waypoint.value.latitude)
                                                .withCadence(Cadence.cadence(flattenCadences[index]))
                                                .apply {
                                                    lastLocation?.let {
                                                        currentDistance += SphericalUtil.computeDistanceBetween(
                                                            it.value.toGmsLatLng(),
                                                            waypoint.value.toGmsLatLng()
                                                        )
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
    }
}

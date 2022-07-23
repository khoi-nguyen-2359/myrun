package akio.apps.myrun.data.activity.impl

import akio.apps.myrun.data.activity.api.ActivityTcxFileWriter
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.di.ActivityDataScope
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.squareup.anvil.annotations.ContributesBinding
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

@ContributesBinding(ActivityDataScope::class)
class ActivityTcxFileWriterImpl @Inject constructor() : ActivityTcxFileWriter {
    override suspend fun writeTcxFile(
        activity: BaseActivityModel,
        locations: List<ActivityLocation>,
        cadences: List<Int>,
        outputFile: File,
        zip: Boolean,
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
            ActivityType.Cycling -> Sport.BIKING
            else -> Sport.OTHER
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
                                            val tcxDate = TCXDate(
                                                Date(activity.startTime + waypoint.elapsedTime)
                                            )
                                            TrackpointBuilder.aTrackpoint()
                                                .onTime(tcxDate)
                                                .withPosition(
                                                    Position.position(
                                                        waypoint.latitude,
                                                        waypoint.longitude
                                                    )
                                                )
                                                .withAltitude(waypoint.altitude)
                                                .apply {
                                                    if (cadences.isNotEmpty()) {
                                                        withCadence(
                                                            Cadence.cadence(cadences[index])
                                                        )
                                                    }
                                                }
                                                .apply {
                                                    lastLocation?.let {
                                                        currentDistance +=
                                                            SphericalUtil.computeDistanceBetween(
                                                                LatLng(it.latitude, it.longitude),
                                                                LatLng(
                                                                    waypoint.latitude,
                                                                    waypoint.longitude
                                                                )
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

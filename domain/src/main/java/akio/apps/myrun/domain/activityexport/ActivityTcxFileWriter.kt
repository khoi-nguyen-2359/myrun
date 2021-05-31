package akio.apps.myrun.domain.activityexport

import akio.apps.myrun._base.runfile.ClosableFileSerializer
import akio.apps.myrun._base.runfile.ZipFileSerializer
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.activityexport.model.ActivityLocation
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

class ActivityTcxFileWriter @Inject constructor() {
    fun writeTcxFile(
        activity: ActivityModel,
        locations: List<ActivityLocation>,
        cadences: List<Int>,
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

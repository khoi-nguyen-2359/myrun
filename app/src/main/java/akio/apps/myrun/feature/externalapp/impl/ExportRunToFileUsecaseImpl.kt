package akio.apps.myrun.feature.externalapp.impl

import akio.apps.myrun.feature.externalapp.ExportRunToFileUsecase
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.helper.runfile.ClosableFileSerializer
import akio.apps.myrun.helper.runfile.ZipFileSerializer
import android.content.Context
import com.sweetzpot.tcxzpot.*
import com.sweetzpot.tcxzpot.builders.ActivityBuilder
import com.sweetzpot.tcxzpot.builders.LapBuilder
import com.sweetzpot.tcxzpot.builders.TrainingCenterDatabaseBuilder
import java.io.File
import java.util.*
import javax.inject.Inject

class ExportRunToFileUsecaseImpl @Inject constructor(
	private val appContext: Context
) : ExportRunToFileUsecase {

    override suspend fun exportRunToFile(activity: Activity, zip: Boolean): File {
        val startDate = Date(activity.startTime * 1000)
        val suffix = if (zip) ".zip" else ".tcx"
        val tempFile = File.createTempFile("activity_${activity.id}", suffix, appContext.cacheDir)
        val serializer = if (zip)
            ZipFileSerializer(tempFile, ".tcx")
        else
            ClosableFileSerializer(tempFile)

        TrainingCenterDatabaseBuilder.trainingCenterDatabase()
            .withActivities(
				Activities.activities(
					ActivityBuilder.activity(Sport.RUNNING)
						.withID(TCXDate(startDate))
						.withLaps(
							LapBuilder.aLap(TCXDate(startDate))
								.withTotalTime(activity.duration.toDouble())
								.withDistance(activity.distance)
								.withIntensity(Intensity.ACTIVE)
								.withTriggerMethod(TriggerMethod.MANUAL)
//							.withCalories(userRun.calories?.toInt() ?: 0)
//							.apply {
//								userRun.getAvgHeartRate() ?. let { withAverageHeartRate(HeartRate(it.toInt())) }
//								userRun.getAvgCadence() ?. let { withCadence(Cadence.cadence(it.toInt()))}
//							}
//							.withTracks(
//								Track.trackWith(
//									userRun.waypoints.mapIndexed { index, waypoint ->
//										TrackpointBuilder.aTrackpoint()
//											.onTime(TCXDate(Date(waypoint.timestamp * 1000)))
//											.withPosition(Position.position(waypoint.lat, waypoint.long))
//											.withAltitude(waypoint.altitude)
//											.apply {
//												waypoint.cadence ?. let { withCadence(Cadence.cadence(it.toInt())) }
//												waypoint.heartRate ?. let { withHeartRate(HeartRate(it.toInt()))}
//											}
//											.withDistance(SphericalUtil.computeLength(userRun.waypoints.subList(0, index + 1).map { LatLng(it.lat, it.long) }))
//											.build()
//									}
//								))
						)
				)
			)
            .build()
            .serialize(serializer)
        serializer.close()

        return tempFile
    }
}
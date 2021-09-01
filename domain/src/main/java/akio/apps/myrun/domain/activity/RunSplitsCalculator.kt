package akio.apps.myrun.domain.activity

import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.domain.TrackingValueConverter
import com.google.maps.android.SphericalUtil
import javax.inject.Inject

class RunSplitsCalculator @Inject constructor() {
    fun createRunSplits(
        activityStartTime: Long,
        locationDataPoints: List<ActivityLocation>,
    ): List<Double> {
        var splitDistance = 0.0
        var splitTime: Double = activityStartTime.toDouble()
        var prevLocation = locationDataPoints.firstOrNull() ?: return emptyList()
        val splits = mutableListOf<Double>()
        locationDataPoints.forEach { currLocation ->
            val prevGmsLatLng = prevLocation.toGmsLatLng()
            val currGmsLatLng = currLocation.toGmsLatLng()
            val pairDistance = SphericalUtil.computeDistanceBetween(
                prevGmsLatLng,
                currGmsLatLng
            )
            splitDistance += pairDistance

            if (splitDistance >= 1000) {
                val exceededDistance = splitDistance % 1000
                val splitCount = (splitDistance / 1000).toInt()
                val fraction = 1 - exceededDistance / pairDistance
                val duration = (
                    prevLocation.time +
                        (currLocation.time - prevLocation.time) * fraction - splitTime
                    )
                val pace = TrackingValueConverter.TimeMinute.fromRawValue(duration.toLong())
                repeat(splitCount) {
                    splits.add(pace / splitCount)
                }
                splitDistance = exceededDistance
                splitTime += duration
            }
            prevLocation = currLocation
        }

        // process the last split segment
        if (splitDistance > 0) {
            val duration = (prevLocation.time - splitTime) * 1000 / splitDistance
            splits.add(TrackingValueConverter.TimeMinute.fromRawValue(duration.toLong()))
        }

        return splits
    }
}

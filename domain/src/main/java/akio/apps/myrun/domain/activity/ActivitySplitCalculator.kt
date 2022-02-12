package akio.apps.myrun.domain.activity

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.location.api.SphericalUtil
import akio.apps.myrun.data.user.api.TrackValueConverter
import akio.apps.myrun.data.user.api.model.MeasureSystem
import javax.inject.Inject
import timber.log.Timber

class ActivitySplitCalculator @Inject constructor(private val sphericalUtil: SphericalUtil) {
    fun createRunSplits(
        locationDataPoints: List<ActivityLocation>,
        measureSystem: MeasureSystem,
    ): List<Double> {
        val splitLength = when (measureSystem) {
            MeasureSystem.Metric -> 1000.0
            MeasureSystem.Imperial -> 1609.34
        }
        var traversedDistance = 0.0
        var prevLocation = locationDataPoints.firstOrNull()
            ?: return emptyList()
        var splitTime = 0.0
        val splits = mutableListOf<Double>()
        locationDataPoints.forEach { currLocation ->
            val pairDistance = sphericalUtil.computeDistanceBetween(
                prevLocation.getLatLng(),
                currLocation.getLatLng()
            )
            traversedDistance += pairDistance

            if (traversedDistance >= splitLength) {
                val exceededDistance = traversedDistance % splitLength
                val splitCount = (traversedDistance / splitLength).toInt()
                val fraction = 1 - exceededDistance / pairDistance
                val duration = prevLocation.elapsedTime +
                    (currLocation.elapsedTime - prevLocation.elapsedTime) * fraction - splitTime
                val pace = TrackValueConverter.TimeMinute.fromRawValue(duration.toLong())
                repeat(splitCount) {
                    splits.add(pace / splitCount)
                }
                traversedDistance = exceededDistance
                splitTime += duration
            }
            prevLocation = currLocation
        }

        // process the last split segment
        if (traversedDistance > 0) {
            val duration = (prevLocation.elapsedTime - splitTime) * splitLength / traversedDistance
            splits.add(TrackValueConverter.TimeMinute.fromRawValue(duration.toLong()))
        }
        Timber.d("splits=$splits")
        return splits
    }
}

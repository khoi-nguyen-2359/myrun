package akio.apps.myrun.domain.activity.impl

import akio.apps.myrun.data.location.api.SphericalUtil
import akio.apps.myrun.domain.activity.api.RunSplitsCalculator
import akio.apps.myrun.domain.activity.api.getLatLng
import akio.apps.myrun.domain.activity.api.model.ActivityLocation
import akio.apps.myrun.domain.common.TrackingValueConverter
import javax.inject.Inject
import timber.log.Timber

class RunSplitsCalculatorImpl @Inject constructor(
    private val sphericalUtil: SphericalUtil,
) : RunSplitsCalculator {
    override fun createRunSplits(
        locationDataPoints: List<ActivityLocation>,
    ): List<Double> {
        var splitDistance = 0.0
        var prevLocation = locationDataPoints.firstOrNull()
            ?: return emptyList()
        var splitTime = 0.0
        val splits = mutableListOf<Double>()
        locationDataPoints.forEach { currLocation ->
            val pairDistance = sphericalUtil.computeDistanceBetween(
                prevLocation.getLatLng(),
                currLocation.getLatLng()
            )
            splitDistance += pairDistance

            if (splitDistance >= 1000) {
                val exceededDistance = splitDistance % 1000
                val splitCount = (splitDistance / 1000).toInt()
                val fraction = 1 - exceededDistance / pairDistance
                val duration = prevLocation.elapsedTime +
                    (currLocation.elapsedTime - prevLocation.elapsedTime) * fraction - splitTime
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
            val duration = (prevLocation.elapsedTime - splitTime) * 1000 / splitDistance
            splits.add(TrackingValueConverter.TimeMinute.fromRawValue(duration.toLong()))
        }
        Timber.d("splits=$splits")
        return splits
    }
}

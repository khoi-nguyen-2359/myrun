package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.time.TimeProvider
import androidx.annotation.VisibleForTesting

class AverageLocationAccumulator(
    private val accumulationPeriod: Long,
    private val timeProvider: TimeProvider
) : LocationProcessor {
    private var lastDeliverTime: Long = 0L
    private val currentLocationBatch: MutableList<Location> = mutableListOf()

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun deliverNow(time: Long): Location? {
        if (currentLocationBatch.isEmpty()) {
            return null
        }
        val accumulatedLocation =
            currentLocationBatch.fold(Location(0, 0.0, 0.0, 0.0, 0.0)) { accum, item ->
                Location(
                    item.time,
                    accum.latitude + item.latitude,
                    accum.longitude + item.longitude,
                    accum.altitude + item.altitude,
                    accum.speed + item.speed
                )
            }
        val batchSize = currentLocationBatch.size
        currentLocationBatch.clear()
        lastDeliverTime = time
        return Location(
            accumulatedLocation.time,
            accumulatedLocation.latitude / batchSize,
            accumulatedLocation.longitude / batchSize,
            accumulatedLocation.altitude / batchSize,
            accumulatedLocation.speed / batchSize,
        )
    }

    override fun process(locations: List<Location>): List<Location> {
        currentLocationBatch.addAll(locations)
        val time = timeProvider.currentMillisecond()
        if (time - lastDeliverTime >= accumulationPeriod) {
            return listOfNotNull(deliverNow(time))
        }

        return emptyList()
    }
}

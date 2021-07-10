package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.location.LocationEntity

class AverageLocationAccumulator(private val accumulationPeriod: Long) : LocationProcessor {
    private var lastDeliverTime: Long = 0L
    private val currentLocationBatch: MutableList<LocationEntity> = mutableListOf()

    private fun deliverNow(time: Long): LocationEntity? {
        if (currentLocationBatch.isEmpty()) {
            return null
        }
        val accumulatedLocation =
            currentLocationBatch.fold(LocationEntity(0, 0.0, 0.0, 0.0, 0.0)) { accum, item ->
                LocationEntity(
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
        return LocationEntity(
            accumulatedLocation.time,
            accumulatedLocation.latitude / batchSize,
            accumulatedLocation.longitude / batchSize,
            accumulatedLocation.altitude / batchSize,
            accumulatedLocation.speed / batchSize,
        )
    }

    override fun process(locations: List<LocationEntity>): List<LocationEntity> {
        currentLocationBatch.addAll(locations)
        val time = System.currentTimeMillis()
        if (time - lastDeliverTime >= accumulationPeriod) {
            return listOfNotNull(deliverNow(time))
        }

        return emptyList()
    }
}

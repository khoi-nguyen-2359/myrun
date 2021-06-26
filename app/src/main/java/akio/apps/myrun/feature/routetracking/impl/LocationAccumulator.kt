package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.data.location.LocationEntity

class LocationAccumulator(private val accumulateDuration: Long, startTime: Long) {
    private var lastDeliverTime: Long = startTime
    private val currentLocationBatch: MutableList<LocationEntity> = mutableListOf()
    fun accumulate(locations: List<LocationEntity>, time: Long): LocationEntity? {
        currentLocationBatch.addAll(locations)
        if (time - lastDeliverTime >= accumulateDuration) {
            return deliverNow(time)
        }

        return null
    }

    fun deliverNow(time: Long): LocationEntity? {
        if (currentLocationBatch.isEmpty()) {
            return null
        }
        val accumulatedLocation =
            currentLocationBatch.fold(LocationEntity(0, 0.0, 0.0, 0.0)) { accum, item ->
                LocationEntity(
                    item.time,
                    accum.latitude + item.latitude,
                    accum.longitude + item.longitude,
                    accum.altitude + item.altitude
                )
            }
        val batchSize = currentLocationBatch.size
        currentLocationBatch.clear()
        lastDeliverTime = time
        return LocationEntity(
            accumulatedLocation.time,
            accumulatedLocation.latitude / batchSize,
            accumulatedLocation.longitude / batchSize,
            accumulatedLocation.altitude / batchSize
        )
    }
}

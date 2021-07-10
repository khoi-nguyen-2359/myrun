package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.location.LocationEntity

class LocationProcessorContainer : LocationProcessor {
    private val processorList: MutableList<LocationProcessor> = mutableListOf()
    fun addProcessor(locationProcessor: LocationProcessor) {
        processorList.add(locationProcessor)
    }

    override fun process(locations: List<LocationEntity>): List<LocationEntity> {
        var processedLocations = locations
        processorList.forEach { processor ->
            processedLocations = processor.process(processedLocations)
        }
        return processedLocations
    }
}

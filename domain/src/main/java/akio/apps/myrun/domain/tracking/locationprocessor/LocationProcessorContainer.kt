package akio.apps.myrun.domain.tracking.locationprocessor

import akio.apps.myrun.data.location.api.model.Location

class LocationProcessorContainer : LocationProcessor {
    private val processorList: MutableList<LocationProcessor> = mutableListOf()
    fun addProcessor(locationProcessor: LocationProcessor) {
        processorList.add(locationProcessor)
    }

    override fun process(locations: List<Location>): List<Location> {
        var processedLocations = locations
        processorList.forEach { processor ->
            processedLocations = processor.process(processedLocations)
        }
        return processedLocations
    }

    fun clear() = processorList.clear()
}

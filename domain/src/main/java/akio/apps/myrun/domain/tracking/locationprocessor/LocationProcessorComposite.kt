package akio.apps.myrun.domain.tracking.locationprocessor

import akio.apps.myrun.data.location.api.LOG_TAG_LOCATION
import akio.apps.myrun.data.location.api.model.Location
import timber.log.Timber

class LocationProcessorComposite : LocationProcessor {
    private val processorList: MutableList<LocationProcessor> = mutableListOf()
    fun addProcessor(locationProcessor: LocationProcessor) {
        processorList.add(locationProcessor)
    }

    override fun process(locations: List<Location>): List<Location> {
        Timber.tag(LOG_TAG_LOCATION)
            .d(
                "[LocationProcessorContainer] start processing" +
                    "\nlocation count = ${locations.size}" +
                    "\nprocessor count = ${processorList.size}"
            )
        val result = processorList.fold(locations) { acc, locationProcessor ->
            locationProcessor.process(acc)
        }
        Timber.tag(LOG_TAG_LOCATION)
            .d("[LocationProcessorContainer] end processing, count = ${result.size}")
        return result
    }

    fun clear() = processorList.clear()
}

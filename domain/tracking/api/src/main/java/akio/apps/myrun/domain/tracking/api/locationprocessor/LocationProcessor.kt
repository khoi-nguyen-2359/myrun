package akio.apps.myrun.domain.tracking.api.locationprocessor

import akio.apps.myrun.data.location.api.model.Location

interface LocationProcessor {
    fun process(locations: List<Location>): List<Location>
}

package akio.apps.myrun.domain.tracking.impl

import akio.apps.myrun.data.location.api.model.Location

interface LocationProcessor {
    fun process(locations: List<Location>): List<Location>
}

package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.location.LocationEntity

interface LocationProcessor {
    fun process(locations: List<LocationEntity>): List<LocationEntity>
}

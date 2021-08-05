package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.location.Location

interface LocationProcessor {
    fun process(locations: List<Location>): List<Location>
}

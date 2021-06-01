package akio.apps.myrun.data.activitysharing

import akio.apps.myrun.data.activitysharing.model.ActivityLocation

interface ActivityLocationCache {
    suspend fun saveActivityLocations(activityLocations: List<ActivityLocation>)

    /**
     * Returns empty list if cache misses.
     */
    suspend fun getActivityLocations(activityId: String): List<ActivityLocation>
    suspend fun clearActivityLocations(activityId: String)
}

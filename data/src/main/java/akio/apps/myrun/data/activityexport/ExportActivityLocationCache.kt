package akio.apps.myrun.data.activityexport

import akio.apps.myrun.data.activityexport.model.ActivityLocation

interface ExportActivityLocationCache {
    suspend fun saveActivityLocations(activityLocations: List<ActivityLocation>)

    /**
     * Returns empty list if cache misses.
     */
    suspend fun getActivityLocations(activityId: String): List<ActivityLocation>
    suspend fun clearActivityLocations(activityId: String)
}

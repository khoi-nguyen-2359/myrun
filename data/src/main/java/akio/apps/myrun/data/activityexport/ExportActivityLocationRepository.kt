package akio.apps.myrun.data.activityexport

import akio.apps.myrun.data.activityexport.model.ActivityLocation

interface ExportActivityLocationRepository {
    suspend fun saveActivityLocations(activityLocations: List<ActivityLocation>)
    suspend fun getActivityLocations(activityId: String): List<ActivityLocation>
    suspend fun clearActivityLocations(activityId: String)
}

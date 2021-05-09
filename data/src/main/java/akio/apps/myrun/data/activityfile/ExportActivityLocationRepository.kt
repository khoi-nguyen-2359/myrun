package akio.apps.myrun.data.activityfile

import akio.apps.myrun.data.activityfile.model.ActivityLocation

interface ExportActivityLocationRepository {
    suspend fun saveActivityLocations(activityLocations: List<ActivityLocation>)
    suspend fun getActivityLocations(activityId: String): List<ActivityLocation>
    suspend fun clearActivityLocations(activityId: String)
}

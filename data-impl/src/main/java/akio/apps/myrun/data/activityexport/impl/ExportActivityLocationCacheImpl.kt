package akio.apps.myrun.data.activityexport.impl

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activityexport.ExportActivityLocationCache
import akio.apps.myrun.data.activityexport.entity.RoomExportActivityLocation
import akio.apps.myrun.data.activityexport.model.ActivityLocation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExportActivityLocationCacheImpl @Inject constructor(
    activityExportDatabase: ActivityExportDatabase,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ExportActivityLocationCache {

    private val exportActivityLocationDao: ExportActivityLocationDao =
        activityExportDatabase.activityLocationDao()

    override suspend fun saveActivityLocations(
        activityLocations: List<ActivityLocation>
    ) = withContext(ioDispatcher) {
        val entities = activityLocations.map { activityLocation ->
            RoomExportActivityLocation(
                id = 0,
                activityId = activityLocation.activityId,
                time = activityLocation.time,
                latitude = activityLocation.latitude,
                longitude = activityLocation.longitude,
                altitude = activityLocation.altitude
            )
        }
        exportActivityLocationDao.insert(entities)
    }

    override suspend fun getActivityLocations(
        activityId: String
    ): List<ActivityLocation> = withContext(ioDispatcher) {
        exportActivityLocationDao.getActivityLocations(activityId).map { roomEntities ->
            ActivityLocation(
                activityId,
                roomEntities.time,
                roomEntities.latitude,
                roomEntities.longitude,
                roomEntities.altitude
            )
        }
    }

    override suspend fun clearActivityLocations(activityId: String) = withContext(ioDispatcher) {
        exportActivityLocationDao.delete(activityId)
    }
}

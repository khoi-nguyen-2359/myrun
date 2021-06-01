package akio.apps.myrun.data.activitysharing.impl

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activitysharing.ActivityLocationCache
import akio.apps.myrun.data.activitysharing.entity.RoomExportActivityLocation
import akio.apps.myrun.data.activitysharing.model.ActivityLocation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ActivityLocationCacheImpl @Inject constructor(
    activitySharingDatabase: ActivitySharingDatabase,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ActivityLocationCache {

    private val activityLocationCacheDao: ActivityLocationCacheDao =
        activitySharingDatabase.activityLocationDao()

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
        activityLocationCacheDao.insert(entities)
    }

    override suspend fun getActivityLocations(
        activityId: String
    ): List<ActivityLocation> = withContext(ioDispatcher) {
        activityLocationCacheDao.getActivityLocations(activityId).map { roomEntities ->
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
        activityLocationCacheDao.delete(activityId)
    }
}

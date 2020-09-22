package akio.apps.myrun.data.activity

import android.graphics.Bitmap

interface ActivityRepository {
    suspend fun getActivitiesByStartTime(userId: String, startAfterTime: Long, limit: Int): List<ActivityEntity>
    suspend fun saveActivity(activity: ActivityEntity, routeMapImage: Bitmap)
}
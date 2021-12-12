package akio.apps.myrun.domain.activity.api

import akio.apps.myrun.data.Resource
import akio.apps.myrun.domain.activity.api.model.ActivityModel

interface GetFeedActivitiesUsecase {
    suspend fun getUserTimelineActivity(startAfter: Long, count: Int): Resource<List<ActivityModel>>
}

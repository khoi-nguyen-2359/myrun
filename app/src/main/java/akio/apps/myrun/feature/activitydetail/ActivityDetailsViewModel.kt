package akio.apps.myrun.feature.activitydetail

import akio.apps._base.Resource
import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityModelMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ActivityDetailsViewModel @Inject constructor(
    private val activityId: String,
    private val activityRepository: ActivityRepository,
    private val activityModelMapper: ActivityModelMapper,
    private val activityDateTimeFormatter: ActivityDateTimeFormatter
) : BaseViewModel() {

    val activityDetails: Flow<Resource<Activity>> = flow {
        emit(Resource.Loading<Activity>())
        val activity = activityRepository.getActivity(activityId)?.let(activityModelMapper::map)
        if (activity == null) {
            emit(Resource.Error<Activity>(ActivityNotFoundException()))
        } else {
            emit(Resource.Success(activity))
        }
    }

    val activityDateTime: Flow<ActivityDateTimeFormatter.Result> =
        activityDetails.map {
            activityDateTimeFormatter.formatActivityDateTime(it.data?.startTime ?: 0L)
        }

    class ActivityNotFoundException : Exception()
}

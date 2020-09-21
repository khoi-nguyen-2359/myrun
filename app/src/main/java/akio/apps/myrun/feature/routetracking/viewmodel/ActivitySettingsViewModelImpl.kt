package akio.apps.myrun.feature.routetracking.viewmodel

import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.feature.routetracking.ActivitySettingsViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class ActivitySettingsViewModelImpl @Inject constructor() : ActivitySettingsViewModel() {

    private val _activityType = MutableLiveData(ActivityType.Running)
    override val activityType: LiveData<ActivityType> = _activityType

    override fun onSelectActivityType(activityType: ActivityType) {
        _activityType.value = activityType
    }
}
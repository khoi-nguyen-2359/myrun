package akio.apps.myrun.feature.routetracking

import akio.apps.myrun.data.activity.ActivityType
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

abstract class ActivitySettingsViewModel: ViewModel() {
    abstract val activityType: LiveData<ActivityType>
    abstract fun onSelectActivityType(activityType: ActivityType)
}
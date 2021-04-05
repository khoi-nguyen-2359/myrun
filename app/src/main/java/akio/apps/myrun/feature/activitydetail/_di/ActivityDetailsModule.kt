package akio.apps.myrun.feature.activitydetail._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.feature.activitydetail.ActivityDateTimeFormatter
import akio.apps.myrun.feature.activitydetail.ActivityDetailsViewModel
import akio.apps.myrun.feature.usertimeline.model.ActivityModelMapper
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Module
class ActivityDetailsModule(
    private val activityId: String
) {
    @Provides
    fun activityDetailsViewModel(
        activityRepository: ActivityRepository,
        activityMapper: ActivityModelMapper,
        activityDateTimeFormatter: ActivityDateTimeFormatter
    ): ActivityDetailsViewModel = ActivityDetailsViewModel(
        activityId,
        activityRepository,
        activityMapper,
        activityDateTimeFormatter
    )

    @Module
    interface Bindings {
        @Binds
        @IntoMap
        @ViewModelKey(ActivityDetailsViewModel::class)
        fun editProfileVm(viewModelImpl: ActivityDetailsViewModel): ViewModel
    }
}

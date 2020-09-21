package akio.apps.myrun.feature.usertimeline._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.impl.UserTimelineActivity
import akio.apps.myrun.feature.usertimeline.impl.UserTimelineViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface UserTimelineFeatureModule {
    @ContributesAndroidInjector
    fun userTimelineActivity(): UserTimelineActivity

    @Binds
    @IntoMap
    @ViewModelKey(UserTimelineViewModel::class)
    fun userTimelineViewModel(viewModelImpl: UserTimelineViewModelImpl): ViewModel
}
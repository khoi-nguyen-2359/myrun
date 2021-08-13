package akio.apps.myrun.feature.home._di

import akio.apps.common.wiring.ViewModelKey
import akio.apps.myrun.feature.home.HomeViewModel
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.impl.UserTimelineViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface HomeFeatureModule {
    @Binds
    @IntoMap
    @ViewModelKey(UserTimelineViewModel::class)
    fun userTimelineViewModel(viewModelImpl: UserTimelineViewModelImpl): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun homeViewModel(vm: HomeViewModel): ViewModel
}

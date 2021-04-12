package akio.apps.myrun.feature.routetracking._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface RouteTrackingFeatureModule {
    @Binds
    @IntoMap
    @ViewModelKey(RouteTrackingViewModel::class)
    fun bindRouteTrackingViewModel(viewModelImpl: RouteTrackingViewModelImpl): ViewModel
}

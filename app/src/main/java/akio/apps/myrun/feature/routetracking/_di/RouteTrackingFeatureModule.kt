package akio.apps.myrun.feature.routetracking._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingService
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingViewModelImpl
import akio.apps.myrun.feature.routetracking.impl.UpdateUserRecentPlaceWorker
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface RouteTrackingFeatureModule {
    @ContributesAndroidInjector
    fun routeTrackingService(): RouteTrackingService

    @ContributesAndroidInjector
    fun updateUserRecentPlaceWorker(): UpdateUserRecentPlaceWorker

    @Binds
    @IntoMap
    @ViewModelKey(RouteTrackingViewModel::class)
    fun bindRouteTrackingViewModel(viewModelImpl: RouteTrackingViewModelImpl): ViewModel
}

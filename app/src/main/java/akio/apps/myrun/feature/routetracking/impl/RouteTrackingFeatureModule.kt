package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun._di.ViewModelKey
import akio.apps.myrun.feature.routetracking.GetMapInitialLocationUsecase
import akio.apps.myrun.feature.routetracking.GetTrackingLocationUpdatesUsecase
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface RouteTrackingFeatureModule {
    @ContributesAndroidInjector
    fun trackingActivity(): RouteTrackingActivity

    @ContributesAndroidInjector
    fun routeTrackingService(): RouteTrackingService

    @Binds
    @IntoMap
    @ViewModelKey(RouteTrackingViewModel::class)
    fun routeTrackingViewModel(viewModelImpl: RouteTrackingViewModelImpl): ViewModel

    @Binds
    fun getMapInitialLocation(usecaseImpl: GetMapInitialLocationUsecaseImpl): GetMapInitialLocationUsecase

    @Binds
    fun getTrackingLocationUpdates(usecaseImpl: GetTrackingLocationUpdatesUsecaseImpl): GetTrackingLocationUpdatesUsecase
}
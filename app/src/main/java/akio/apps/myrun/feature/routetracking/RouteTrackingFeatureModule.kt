package akio.apps.myrun.feature.routetracking

import akio.apps.myrun._di.ViewModelKey
import akio.apps.myrun.feature.routetracking.impl.GetMapInitialLocationUsecaseImpl
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface RouteTrackingFeatureModule {
    @ContributesAndroidInjector
    fun trackingActivity(): RouteTrackingActivity

    @Binds
    @IntoMap
    @ViewModelKey(RouteTrackingViewModel::class)
    fun routeTrackingViewModel(viewModelImpl: RouteTrackingViewModelImpl): ViewModel

    @Binds
    fun getMapInitialLocation(usecaseImpl: GetMapInitialLocationUsecaseImpl): GetMapInitialLocationUsecase
}
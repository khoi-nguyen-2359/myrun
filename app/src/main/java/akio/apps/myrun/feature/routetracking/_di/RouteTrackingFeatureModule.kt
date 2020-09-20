package akio.apps.myrun.feature.routetracking._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.routetracking.*
import akio.apps.myrun.feature.routetracking.impl.*
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
    fun getTrackingLocationUpdates(usecaseImpl: GetTrackedLocationsUsecaseImpl): GetTrackedLocationsUsecase

    @Binds
    fun saveRouteTrackingWorkoutUsecase(usecaseImpl: SaveRouteTrackingWorkoutWorkoutImpl): SaveRouteTrackingWorkoutUsecase

    @Binds
    fun clearRouteTrackingStateUsecase(usecase: ClearRouteTrackingStateUsecaseImpl): ClearRouteTrackingStateUsecase
}
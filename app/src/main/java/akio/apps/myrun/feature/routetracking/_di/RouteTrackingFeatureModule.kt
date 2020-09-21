package akio.apps.myrun.feature.routetracking._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.routetracking.*
import akio.apps.myrun.feature.routetracking.impl.*
import akio.apps.myrun.feature.routetracking.usecase.ClearRouteTrackingStateUsecaseImpl
import akio.apps.myrun.feature.routetracking.usecase.GetMapInitialLocationUsecaseImpl
import akio.apps.myrun.feature.routetracking.usecase.GetTrackedLocationsUsecaseImpl
import akio.apps.myrun.feature.routetracking.usecase.SaveRouteTrackingWorkoutUsecaseImpl
import akio.apps.myrun.feature.routetracking.viewmodel.ActivitySettingsViewModelImpl
import akio.apps.myrun.feature.routetracking.viewmodel.RouteTrackingViewModelImpl
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
    @IntoMap
    @ViewModelKey(ActivitySettingsViewModel::class)
    fun activitySettingsViewModel(viewModelImpl: ActivitySettingsViewModelImpl): ViewModel

    @Binds
    fun getMapInitialLocation(usecaseImpl: GetMapInitialLocationUsecaseImpl): GetMapInitialLocationUsecase

    @Binds
    fun getTrackingLocationUpdates(usecaseImpl: GetTrackedLocationsUsecaseImpl): GetTrackedLocationsUsecase

    @Binds
    fun saveRouteTrackingWorkoutUsecase(usecaseImpl: SaveRouteTrackingWorkoutUsecaseImpl): SaveRouteTrackingWorkoutUsecase

    @Binds
    fun clearRouteTrackingStateUsecase(usecase: ClearRouteTrackingStateUsecaseImpl): ClearRouteTrackingStateUsecase
}
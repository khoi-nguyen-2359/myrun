package akio.apps.myrun.feature.routetracking._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.routetracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.feature.routetracking.GetMapInitialLocationUsecase
import akio.apps.myrun.feature.routetracking.GetTrackedLocationsUsecase
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.feature.routetracking.SaveRouteTrackingActivityUsecase
import akio.apps.myrun.feature.routetracking.UpdateUserRecentPlaceUsecase
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingService
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingViewModelImpl
import akio.apps.myrun.feature.routetracking.impl.UpdateUserRecentPlaceWorker
import akio.apps.myrun.feature.routetracking.usecase.ClearRouteTrackingStateUsecaseImpl
import akio.apps.myrun.feature.routetracking.usecase.GetMapInitialLocationUsecaseImpl
import akio.apps.myrun.feature.routetracking.usecase.GetTrackedLocationsUsecaseImpl
import akio.apps.myrun.feature.routetracking.usecase.SaveRouteTrackingActivityUsecaseImpl
import akio.apps.myrun.feature.routetracking.usecase.UpdateUserRecentPlaceUsecaseImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [RouteTrackingFeatureModule.Bindings::class])
interface RouteTrackingFeatureModule {
    @ContributesAndroidInjector
    fun trackingActivity(): RouteTrackingActivity

    @ContributesAndroidInjector
    fun routeTrackingService(): RouteTrackingService

    @ContributesAndroidInjector
    fun updateUserRecentPlaceWorker(): UpdateUserRecentPlaceWorker

    @Module
    interface Bindings {
        @Binds
        @IntoMap
        @ViewModelKey(RouteTrackingViewModel::class)
        fun routeTrackingViewModel(viewModelImpl: RouteTrackingViewModelImpl): ViewModel

        @Binds
        fun getMapInitialLocation(usecaseImpl: GetMapInitialLocationUsecaseImpl): GetMapInitialLocationUsecase

        @Binds
        fun getTrackingLocationUpdates(usecaseImpl: GetTrackedLocationsUsecaseImpl): GetTrackedLocationsUsecase

        @Binds
        fun saveRouteTrackingActivityUsecase(usecaseImpl: SaveRouteTrackingActivityUsecaseImpl): SaveRouteTrackingActivityUsecase

        @Binds
        fun clearRouteTrackingStateUsecase(usecase: ClearRouteTrackingStateUsecaseImpl): ClearRouteTrackingStateUsecase

        @Binds
        fun getCurrentAreaPlaceUsecase(usecaseImpl: UpdateUserRecentPlaceUsecaseImpl): UpdateUserRecentPlaceUsecase
    }
}

package akio.apps.myrun.feature.routetracking._di

import akio.apps.myrun.feature.routetracking.ClearRouteTrackingStateUsecase
import akio.apps.myrun.feature.routetracking.GetMapInitialLocationUsecase
import akio.apps.myrun.feature.routetracking.GetTrackedLocationsUsecase
import akio.apps.myrun.feature.routetracking.SaveRouteTrackingActivityUsecase
import akio.apps.myrun.feature.routetracking.usecase.ClearRouteTrackingStateUsecaseImpl
import akio.apps.myrun.feature.routetracking.usecase.GetMapInitialLocationUsecaseImpl
import akio.apps.myrun.feature.routetracking.usecase.GetTrackedLocationsUsecaseImpl
import akio.apps.myrun.feature.routetracking.usecase.SaveRouteTrackingActivityUsecaseImpl
import dagger.Binds
import dagger.Module

@Module
interface RouteTrackingDomainModule {
    @Binds
    fun getMapInitialLocation(usecaseImpl: GetMapInitialLocationUsecaseImpl): GetMapInitialLocationUsecase

    @Binds
    fun getTrackingLocationUpdates(usecaseImpl: GetTrackedLocationsUsecaseImpl): GetTrackedLocationsUsecase

    @Binds
    fun saveRouteTrackingActivityUsecase(usecaseImpl: SaveRouteTrackingActivityUsecaseImpl): SaveRouteTrackingActivityUsecase

    @Binds
    fun clearRouteTrackingStateUsecase(usecase: ClearRouteTrackingStateUsecaseImpl): ClearRouteTrackingStateUsecase
}

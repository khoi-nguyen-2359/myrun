package akio.apps.myrun.feature.routetracking._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.routetracking.*
import akio.apps.myrun.feature.routetracking.impl.*
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingViewModelImpl
import akio.apps.myrun.feature.routetracking.usecase.*
import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.HistoryClient
import com.google.android.gms.fitness.RecordingClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import javax.annotation.Nullable

@Module(includes = [RouteTrackingFeatureModule.Bindings::class])
abstract class RouteTrackingFeatureModule {
    @ContributesAndroidInjector
    abstract fun trackingActivity(): RouteTrackingActivity

    @ContributesAndroidInjector
    abstract fun routeTrackingService(): RouteTrackingService

    @ContributesAndroidInjector
    abstract fun updateUserRecentPlaceWorker(): UpdateUserRecentPlaceWorker

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
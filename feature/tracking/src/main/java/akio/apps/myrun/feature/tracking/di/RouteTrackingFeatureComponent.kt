package akio.apps.myrun.feature.tracking.di

import akio.apps.myrun.data.activity.ActivityDataModule
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.common.di.FeatureScope
import akio.apps.myrun.data.eapps.ExternalAppDataModule
import akio.apps.myrun.data.location.LocationDataModule
import akio.apps.myrun.data.tracking.TrackingDataModule
import akio.apps.myrun.data.user.UserDataModule
import akio.apps.myrun.domain.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.tracking.RouteTrackingService
import akio.apps.myrun.feature.tracking.RouteTrackingViewModel
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        AuthenticationDataModule::class,
        ExternalAppDataModule::class,
        LocationDataModule::class,
        UserDataModule::class,
        ActivityDataModule::class,
        TrackingDataModule::class
    ],
)
interface RouteTrackingFeatureComponent {
    fun routeTrackingViewModel(): RouteTrackingViewModel

    fun inject(service: RouteTrackingService)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
        ): RouteTrackingFeatureComponent
    }
}

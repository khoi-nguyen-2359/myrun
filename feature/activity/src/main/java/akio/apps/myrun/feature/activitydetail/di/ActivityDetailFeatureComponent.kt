package akio.apps.myrun.feature.activitydetail.di

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.activity.di.ActivityDataComponent
import akio.apps.myrun.data.activity.di.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.di.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.di.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.location.di.DaggerLocationDataComponent
import akio.apps.myrun.data.location.di.LocationDataComponent
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [DispatchersModule::class],
    dependencies = [
        LocationDataComponent::class,
        AuthenticationDataComponent::class,
        ActivityDataComponent::class
    ]
)
internal interface ActivityDetailFeatureComponent {
    fun activityDetailsViewModel(): ActivityDetailViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance savedStateHandle: SavedStateHandle,
            locationDataComponent: LocationDataComponent =
                DaggerLocationDataComponent.factory().create(application),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.factory().create(application),
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.factory().create(application),
        ): ActivityDetailFeatureComponent
    }
}

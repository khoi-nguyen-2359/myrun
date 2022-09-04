package akio.apps.myrun.feature.userstats.di

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.activity.di.ActivityDataComponent
import akio.apps.myrun.data.activity.di.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.di.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.di.DaggerAuthenticationDataComponent
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.userstats.UserStatsViewModel
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        DispatchersModule::class,
        LaunchCatchingModule::class
    ],
    dependencies = [
        AuthenticationDataComponent::class,
        ActivityDataComponent::class,
    ]
)
internal interface UserStatsFeatureComponent {
    fun userStatsViewModel(): UserStatsViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance savedStateHandle: SavedStateHandle,
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.factory().create(application),
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.factory().create(application),
        ): UserStatsFeatureComponent
    }
}

package akio.apps.myrun.feature.feed.di

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.activity.di.ActivityDataComponent
import akio.apps.myrun.data.activity.di.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.di.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.di.DaggerAuthenticationDataComponent
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.feed.FeedViewModel
import android.app.Application
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineScope

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        DispatchersModule::class
    ],
    dependencies = [
        AuthenticationDataComponent::class,
        ActivityDataComponent::class
    ]
)
internal interface FeedFeatureComponent {
    fun feedViewModel(): FeedViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance viewModelScope: CoroutineScope,
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.factory().create(application),
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.factory().create(application),
        ): FeedFeatureComponent
    }
}

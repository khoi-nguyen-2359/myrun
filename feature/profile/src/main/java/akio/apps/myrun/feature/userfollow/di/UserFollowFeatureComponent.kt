package akio.apps.myrun.feature.userfollow.di

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.authentication.di.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.di.DaggerAuthenticationDataComponent
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.profile.di.UserProfileFeatureComponent
import akio.apps.myrun.feature.userfollow.UserFollowViewModel
import android.app.Application
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
    ]
)
internal interface UserFollowFeatureComponent {
    fun userFollowViewModel(): UserFollowViewModel
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.factory().create(application),
        ): UserFollowFeatureComponent
    }
}

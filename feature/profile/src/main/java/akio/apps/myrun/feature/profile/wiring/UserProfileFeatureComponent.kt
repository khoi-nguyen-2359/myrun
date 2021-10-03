package akio.apps.myrun.feature.profile.wiring

import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.data.wiring.FeatureScope
import akio.apps.myrun.data.wiring.LaunchCatchingModule
import akio.apps.myrun.domain.strava.wiring.DaggerStravaDomainComponent
import akio.apps.myrun.domain.strava.wiring.StravaDomainComponent
import akio.apps.myrun.domain.user.wiring.DaggerUserDomainComponent
import akio.apps.myrun.domain.user.wiring.UserDomainComponent
import akio.apps.myrun.feature.profile.UserProfileViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        ApplicationModule::class
    ],
    dependencies = [
        UserDomainComponent::class,
        StravaDomainComponent::class
    ]
)
internal interface UserProfileFeatureComponent {
    fun userProfileViewModel(): UserProfileViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance savedStateHandle: SavedStateHandle,
            userDomainComponent: UserDomainComponent = DaggerUserDomainComponent.factory().create(),
            stravaDomainComponent: StravaDomainComponent =
                DaggerStravaDomainComponent.factory().create()
        ): UserProfileFeatureComponent
    }
}

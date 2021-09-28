package akio.apps.myrun.feature.home.userhome._di

import akio.apps.common.wiring.FeatureScope
import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.feature.home.userhome.UserHomeViewModel
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    dependencies = [
        DomainComponent::class,
        ActivityDataComponent::class
    ]
)
interface UserHomeFeatureComponent {
    fun userHomeViewModel(): UserHomeViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance savedStateHandle: SavedStateHandle,
            domainComponent: DomainComponent = DaggerDomainComponent.factory().create(),
            activityDataComponent: ActivityDataComponent = DaggerActivityDataComponent.create(),
        ): UserHomeFeatureComponent
    }
}

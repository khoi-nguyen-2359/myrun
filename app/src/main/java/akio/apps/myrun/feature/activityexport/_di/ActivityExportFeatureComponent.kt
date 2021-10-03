package akio.apps.myrun.feature.activityexport._di

import akio.apps.myrun.data.wiring.FeatureScope
import akio.apps.myrun.domain.activity.wiring.ActivityDomainComponent
import akio.apps.myrun.domain.activity.wiring.DaggerActivityDomainComponent
import akio.apps.myrun.feature.activityexport.ActivityExportService
import dagger.Component

@FeatureScope
@Component(
    dependencies = [
        ActivityDomainComponent::class
    ]
)
interface ActivityExportFeatureComponent {
    fun inject(activityExportService: ActivityExportService)

    @Component.Factory
    interface Factory {
        fun create(
            activityDomainComponent: ActivityDomainComponent =
                DaggerActivityDomainComponent.factory().create(),
        ): ActivityExportFeatureComponent
    }
}

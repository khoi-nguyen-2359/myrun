package akio.apps.myrun.feature.activitydetail.wiring

import akio.apps.myrun.domain.activity.wiring.ActivityDomainComponent
import akio.apps.myrun.domain.activity.wiring.DaggerActivityDomainComponent
import akio.apps.myrun.feature.activitydetail.ActivityExportService
import dagger.Component

@Component(
    dependencies = [
        ActivityDomainComponent::class
    ]
)
internal interface ActivityExportFeatureComponent {
    fun inject(activityExportService: ActivityExportService)

    @Component.Factory
    interface Factory {
        fun create(
            activityDomainComponent: ActivityDomainComponent =
                DaggerActivityDomainComponent.factory().create(),
        ): ActivityExportFeatureComponent
    }
}

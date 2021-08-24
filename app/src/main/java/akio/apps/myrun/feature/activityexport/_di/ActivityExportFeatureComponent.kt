package akio.apps.myrun.feature.activityexport._di

import akio.apps.common.wiring.FeatureScope
import akio.apps.myrun.feature.activityexport.ActivityExportService
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import dagger.Component

@FeatureScope
@Component(
    dependencies = [DomainComponent::class]
)
interface ActivityExportFeatureComponent {
    fun inject(activityExportService: ActivityExportService)

    @Component.Factory
    interface Factory {
        fun create(
            domainComponent: DomainComponent = DaggerDomainComponent.create(),
        ): ActivityExportFeatureComponent
    }
}

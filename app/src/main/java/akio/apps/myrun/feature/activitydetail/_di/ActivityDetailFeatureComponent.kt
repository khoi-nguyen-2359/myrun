package akio.apps.myrun.feature.activitydetail._di

import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun.data.activity._di.ActivityDataModule
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        ActivityDetailFeatureModule::class,
        ActivityDataModule::class,
    ]
)
interface ActivityDetailFeatureComponent : ViewModelFactoryProvider {
    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance params: ActivityDetailViewModel.Params
        ): ActivityDetailFeatureComponent
    }
}

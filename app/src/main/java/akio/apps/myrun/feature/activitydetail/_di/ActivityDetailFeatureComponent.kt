package akio.apps.myrun.feature.activitydetail._di

import akio.apps._base.di.ViewModelFactoryProvider
import akio.apps.myrun._di.DispatchersModule
import akio.apps.myrun.data.activity._di.ActivityDataModule
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.data.recentplace._di.RecentPlaceDataModule
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        ActivityDetailFeatureModule::class,
        ActivityDataModule::class,
        RecentPlaceDataModule::class,
        AuthenticationDataModule::class,
        DispatchersModule::class
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

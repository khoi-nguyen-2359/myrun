package akio.apps.myrun.feature.home.feed.di

import akio.apps.myrun.data.activity.ActivityDataModule
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.user.UserDataModule
import akio.apps.myrun.domain.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.home.feed.ActivityFeedViewModel
import akio.apps.myrun.wiring.common.FeatureScope
import android.app.Application
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        LaunchCatchingModule::class,
        AuthenticationDataModule::class,
        UserDataModule::class,
        ActivityDataModule::class
    ]
)
internal interface ActivityFeedFeatureComponent {
    fun feedViewModel(): ActivityFeedViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
        ): ActivityFeedFeatureComponent
    }
}

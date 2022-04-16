package akio.apps.myrun.feature.home.userstats.di

import akio.apps.myrun.data.activity.ActivityDataModule
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.common.di.DispatchersModule
import akio.apps.myrun.data.common.di.FeatureScope
import akio.apps.myrun.data.user.UserDataModule
import akio.apps.myrun.feature.home.userstats.UserStatsViewModel
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@FeatureScope
@Component(
    modules = [
        DispatchersModule::class,
        AuthenticationDataModule::class,
        UserDataModule::class,
        ActivityDataModule::class
    ],
)
internal interface UserStatsFeatureComponent {
    fun userStatsViewModel(): UserStatsViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance savedStateHandle: SavedStateHandle,
        ): UserStatsFeatureComponent
    }
}

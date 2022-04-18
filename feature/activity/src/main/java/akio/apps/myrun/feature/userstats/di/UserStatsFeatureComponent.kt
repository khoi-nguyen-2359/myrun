package akio.apps.myrun.feature.userstats.di

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.base.di.FeatureScope
import akio.apps.myrun.data.activity.ActivityDataModule
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.user.UserDataModule
import akio.apps.myrun.feature.userstats.UserStatsViewModel
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

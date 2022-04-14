package akio.apps.myrun.feature.home.userhome.di

import akio.apps.myrun.data.activity.ActivityDataModule
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.user.UserDataModule
import akio.apps.myrun.feature.home.userhome.UserHomeViewModel
import akio.apps.myrun.wiring.common.DispatchersModule
import akio.apps.myrun.wiring.common.FeatureScope
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
internal interface UserHomeFeatureComponent {
    fun userHomeViewModel(): UserHomeViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance savedStateHandle: SavedStateHandle,
        ): UserHomeFeatureComponent
    }
}

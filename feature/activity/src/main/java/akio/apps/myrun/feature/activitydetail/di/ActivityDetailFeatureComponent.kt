package akio.apps.myrun.feature.activitydetail.di

import akio.apps.myrun.data.activity.ActivityDataModule
import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.data.location.LocationDataModule
import akio.apps.myrun.data.user.UserDataModule
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AuthenticationDataModule::class,
        LocationDataModule::class,
        UserDataModule::class,
        ActivityDataModule::class
    ]
)
internal interface ActivityDetailFeatureComponent {
    fun activityDetailsViewModel(): ActivityDetailViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance savedStateHandle: SavedStateHandle,
        ): ActivityDetailFeatureComponent
    }
}

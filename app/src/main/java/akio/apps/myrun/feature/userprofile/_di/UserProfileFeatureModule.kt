package akio.apps.myrun.feature.userprofile._di

import akio.apps.base.wiring.ViewModelKey
import akio.apps.myrun.feature.userprofile.UserProfileViewModel
import akio.apps.myrun.feature.userprofile.impl.UserProfileViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface UserProfileFeatureModule {
    @Binds
    @IntoMap
    @ViewModelKey(UserProfileViewModel::class)
    fun profileVm(vm: UserProfileViewModelImpl): ViewModel
}

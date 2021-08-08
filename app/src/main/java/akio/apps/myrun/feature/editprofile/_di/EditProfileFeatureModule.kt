package akio.apps.myrun.feature.editprofile._di

import akio.apps.myrun.feature.editprofile.EditProfileViewModel
import akio.apps.myrun.feature.editprofile.impl.EditProfileViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface EditProfileFeatureModule {
    @Binds
    @IntoMap
    @akio.apps.base.feature.viewmodel.ViewModelKey(EditProfileViewModel::class)
    fun editProfileVm(viewModelImpl: EditProfileViewModelImpl): ViewModel
}

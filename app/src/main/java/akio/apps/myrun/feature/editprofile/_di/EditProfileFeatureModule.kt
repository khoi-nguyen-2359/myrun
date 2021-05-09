package akio.apps.myrun.feature.editprofile._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.editprofile.EditProfileViewModel
import akio.apps.myrun.feature.editprofile.UserPhoneNumberDelegate
import akio.apps.myrun.feature.editprofile.impl.EditProfileViewModelImpl
import akio.apps.myrun.feature.editprofile.impl.FirebaseUserPhoneNumberDelegate
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface EditProfileFeatureModule {
    @Binds
    @IntoMap
    @ViewModelKey(EditProfileViewModel::class)
    fun editProfileVm(viewModelImpl: EditProfileViewModelImpl): ViewModel

    @Binds
    fun getUpdatePhoneNumberDelegate(delegate: FirebaseUserPhoneNumberDelegate):
        UserPhoneNumberDelegate
}

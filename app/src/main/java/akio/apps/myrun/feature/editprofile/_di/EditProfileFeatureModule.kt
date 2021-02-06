package akio.apps.myrun.feature.editprofile._di

import akio.apps._base.di.ViewModelFactoryModule
import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.editprofile.EditProfileViewModel
import akio.apps.myrun.feature.editprofile.UserPhoneNumberDelegate
import akio.apps.myrun.feature.editprofile.impl.EditProfileActivity
import akio.apps.myrun.feature.editprofile.impl.EditProfileViewModelImpl
import akio.apps.myrun.feature.editprofile.impl.FirebaseUserPhoneNumberDelegate
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface EditProfileFeatureModule {
    @ContributesAndroidInjector(modules = [
        Bindings::class,
        ViewModelFactoryModule::class
    ])
    fun editProfileActivity(): EditProfileActivity

    @Module
    interface Bindings {
        @Binds
        @IntoMap
        @ViewModelKey(EditProfileViewModel::class)
        fun editProfileVm(vm: EditProfileViewModelImpl): ViewModel

        @Binds
        fun getUpdatePhoneNumberDelegate(delegate: FirebaseUserPhoneNumberDelegate): UserPhoneNumberDelegate
    }
}

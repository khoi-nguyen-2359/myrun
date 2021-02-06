package akio.apps.myrun.feature.userprofile._di

import akio.apps._base.di.ViewModelFactoryModule
import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.userprofile.UserProfileViewModel
import akio.apps.myrun.feature.userprofile.impl.UserProfileFragment
import akio.apps.myrun.feature.userprofile.impl.UserProfileViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface UserProfileFeatureModule {
    @ContributesAndroidInjector(modules = [
        UserProfileDomainModule::class,
        ViewModelFactoryModule::class,
        Bindings::class
    ])
    fun userProfileFragment(): UserProfileFragment

    @Module
    interface Bindings {
        @Binds
        @IntoMap
        @ViewModelKey(UserProfileViewModel::class)
        fun profileVm(vm: UserProfileViewModelImpl): ViewModel
    }
}

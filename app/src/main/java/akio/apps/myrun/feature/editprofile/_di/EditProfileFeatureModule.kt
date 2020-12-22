package akio.apps.myrun.feature.editprofile._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.editprofile.*
import akio.apps.myrun.feature.editprofile.impl.*
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [EditProfileFeatureModule.Bindings::class])
abstract class EditProfileFeatureModule {

    @ContributesAndroidInjector
    abstract fun editProfile(): EditProfileActivity

    @Module
    interface Bindings {
        @Binds
        @IntoMap
        @ViewModelKey(EditProfileViewModel::class)
        fun editProfileVm(vm: EditProfileViewModelImpl): ViewModel

        @Binds
        fun updateStravaTokenUsecase(usecase: UpdateStravaTokenUsecaseImpl): UpdateStravaTokenUsecase

        @Binds
        fun getUpdatePhoneNumberDelegate(delegate: FirebaseUserPhoneNumberDelegate): UserPhoneNumberDelegate

        @Binds
        fun updateUserProfileUsecase(usecase: UpdateUserProfileUsecaseImpl): UpdateUserProfileUsecase

        @Binds
        fun exchangeStravaTokenUsecase(usecase: ExchangeStravaLoginCodeUsecaseImpl): ExchangeStravaLoginCodeUsecase
    }
}
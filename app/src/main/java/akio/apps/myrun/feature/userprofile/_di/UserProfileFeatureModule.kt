package akio.apps.myrun.feature.userprofile._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.userprofile.*
import akio.apps.myrun.feature.userprofile.impl.*
import akio.apps.myrun.feature.userprofile.usecase.*
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [UserProfileFeatureModule.Bindings::class])
abstract class UserProfileFeatureModule {
    @ContributesAndroidInjector
    abstract fun userProfile(): UserProfileFragment

    @Module
    interface Bindings {
        @Binds
        @IntoMap
        @ViewModelKey(UserProfileViewModel::class)
        fun profileVm(vm: UserProfileViewModelImpl): ViewModel

        @Binds
        fun getUserProfileUsecase(usecase: GetUserProfileUsecaseImpl): GetUserProfileUsecase

        @Binds
        fun logoutUsecase(firebaseLogoutUsecase: FirebaseLogoutUsecase): LogoutUsecase

        @Binds
        fun linkFacebookUsecase(firebaseLinkWithFacebookUsecase: FirebaseLinkWithFacebookUsecase): LinkFacebookUsecase

        @Binds
        fun getProviderTokensUsecase(usecase: GetProviderTokensUsecaseImpl): GetProviderTokensUsecase

        @Binds
        fun deauthorizeStravaTokenUsecase(usecase: DeauthorizeStravaUsecaseImpl): DeauthorizeStravaUsecase

        @Binds
        fun removeStravaTokeUsecase(removeStravaTokenUsecase: RemoveStravaTokenUsecaseImpl): RemoveStravaTokenUsecase
    }
}
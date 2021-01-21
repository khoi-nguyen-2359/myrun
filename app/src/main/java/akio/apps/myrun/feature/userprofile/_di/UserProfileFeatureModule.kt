package akio.apps.myrun.feature.userprofile._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.userprofile.DeauthorizeStravaUsecase
import akio.apps.myrun.feature.userprofile.GetProviderTokensUsecase
import akio.apps.myrun.feature.userprofile.GetUserProfileUsecase
import akio.apps.myrun.feature.userprofile.LinkFacebookUsecase
import akio.apps.myrun.feature.userprofile.LogoutUsecase
import akio.apps.myrun.feature.userprofile.RemoveStravaTokenUsecase
import akio.apps.myrun.feature.userprofile.UserProfileViewModel
import akio.apps.myrun.feature.userprofile.impl.UserProfileFragment
import akio.apps.myrun.feature.userprofile.impl.UserProfileViewModelImpl
import akio.apps.myrun.feature.userprofile.usecase.DeauthorizeStravaUsecaseImpl
import akio.apps.myrun.feature.userprofile.usecase.FirebaseLinkWithFacebookUsecase
import akio.apps.myrun.feature.userprofile.usecase.FirebaseLogoutUsecase
import akio.apps.myrun.feature.userprofile.usecase.GetProviderTokensUsecaseImpl
import akio.apps.myrun.feature.userprofile.usecase.GetUserProfileUsecaseImpl
import akio.apps.myrun.feature.userprofile.usecase.RemoveStravaTokenUsecaseImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface UserProfileFeatureModule {
    @ContributesAndroidInjector
    fun userProfile(): UserProfileFragment

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

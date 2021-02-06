package akio.apps.myrun.feature.userprofile._di

import akio.apps.myrun.feature.userprofile.DeauthorizeStravaUsecase
import akio.apps.myrun.feature.userprofile.GetProviderTokensUsecase
import akio.apps.myrun.feature.userprofile.GetUserProfileUsecase
import akio.apps.myrun.feature.userprofile.LogoutUsecase
import akio.apps.myrun.feature.userprofile.RemoveStravaTokenUsecase
import akio.apps.myrun.feature.userprofile.usecase.DeauthorizeStravaUsecaseImpl
import akio.apps.myrun.feature.userprofile.usecase.FirebaseLogoutUsecase
import akio.apps.myrun.feature.userprofile.usecase.GetProviderTokensUsecaseImpl
import akio.apps.myrun.feature.userprofile.usecase.GetUserProfileUsecaseImpl
import akio.apps.myrun.feature.userprofile.usecase.RemoveStravaTokenUsecaseImpl
import dagger.Binds
import dagger.Module

@Module
interface UserProfileDomainModule {
    @Binds
    fun getUserProfileUsecase(usecase: GetUserProfileUsecaseImpl): GetUserProfileUsecase

    @Binds
    fun logoutUsecase(firebaseLogoutUsecase: FirebaseLogoutUsecase): LogoutUsecase

    @Binds
    fun getProviderTokensUsecase(usecase: GetProviderTokensUsecaseImpl): GetProviderTokensUsecase

    @Binds
    fun deauthorizeStravaTokenUsecase(usecase: DeauthorizeStravaUsecaseImpl): DeauthorizeStravaUsecase

    @Binds
    fun removeStravaTokeUsecase(removeStravaTokenUsecase: RemoveStravaTokenUsecaseImpl): RemoveStravaTokenUsecase
}

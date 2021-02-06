package akio.apps.myrun.feature.editprofile._di

import akio.apps.myrun.feature.editprofile.UpdateStravaTokenUsecase
import akio.apps.myrun.feature.editprofile.UpdateUserProfileUsecase
import akio.apps.myrun.feature.editprofile.impl.UpdateStravaTokenUsecaseImpl
import akio.apps.myrun.feature.editprofile.impl.UpdateUserProfileUsecaseImpl
import dagger.Binds
import dagger.Module

@Module
interface EditProfileDomainModule {
    @Binds
    fun updateStravaTokenUsecase(usecase: UpdateStravaTokenUsecaseImpl): UpdateStravaTokenUsecase

    @Binds
    fun updateUserProfileUsecase(usecase: UpdateUserProfileUsecaseImpl): UpdateUserProfileUsecase

    @Binds
    fun exchangeStravaTokenUsecase(usecase: akio.apps.myrun.feature.editprofile.impl.ExchangeStravaLoginCodeUsecase): akio.apps.myrun.feature.editprofile.ExchangeStravaLoginCodeUsecase
}

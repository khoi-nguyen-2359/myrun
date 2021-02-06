package akio.apps.myrun.feature.recentplace._di

import akio.apps.myrun.feature.recentplace.UpdateUserRecentPlaceUsecase
import akio.apps.myrun.feature.recentplace.impl.UpdateUserRecentPlaceUsecaseImpl
import dagger.Binds
import dagger.Module

@Module
interface RecentPlaceDomainModule {
    @Binds
    fun getCurrentAreaPlaceUsecase(usecaseImpl: UpdateUserRecentPlaceUsecaseImpl): UpdateUserRecentPlaceUsecase
}

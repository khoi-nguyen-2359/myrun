package akio.apps.myrun.feature.signin._di

import akio.apps.myrun.feature.signin.SyncUserProfileUsecase
import akio.apps.myrun.feature.signin.impl.SyncUserProfileUsecaseImpl
import dagger.Binds
import dagger.Module

@Module
interface SignInDomainModule {
    @Binds
    fun syncUserProfileUsecase(usecaseImpl: SyncUserProfileUsecaseImpl): SyncUserProfileUsecase
}

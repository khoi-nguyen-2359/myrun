package akio.apps.myrun.data.user

import akio.apps.myrun.base.di.DispatchersModule
import akio.apps.myrun.base.firebase.FirebaseDataModule
import akio.apps.myrun.data.user.api.AppMigrationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.data.user.impl.FirebaseUserFollowRepository
import akio.apps.myrun.data.user.impl.FirebaseUserProfileRepository
import akio.apps.myrun.data.user.impl.FirebaseUserRecentPlaceRepository
import akio.apps.myrun.data.user.impl.PreferenceAppMigrationState
import dagger.Binds
import dagger.Module

@Module(includes = [DispatchersModule::class, FirebaseDataModule::class])
interface UserDataModule {
    @Binds
    fun userProfileRepo(repo: FirebaseUserProfileRepository): UserProfileRepository

    @Binds
    fun userFollowRepository(firebaseUserFollowRepository: FirebaseUserFollowRepository):
        UserFollowRepository

    @Binds
    fun recentPlaceRepo(firebaseRecentPlaceRepository: FirebaseUserRecentPlaceRepository):
        UserRecentPlaceRepository

    @Binds
    fun appVersionMigrationState(impl: PreferenceAppMigrationState): AppMigrationState
}

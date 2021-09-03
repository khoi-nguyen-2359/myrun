package akio.apps.myrun.wiring.data.user

import akio.apps.myrun.data.user.api.AppVersionMigrationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.data.user.impl.FirebaseUserFollowRepository
import akio.apps.myrun.data.user.impl.FirebaseUserProfileRepository
import akio.apps.myrun.data.user.impl.FirebaseUserRecentPlaceRepository
import akio.apps.myrun.data.user.impl.PreferenceAppVersionMigrationState
import dagger.Binds
import dagger.Module

@Module
internal interface UserDataModule {
    @Binds
    fun userProfileRepo(repo: FirebaseUserProfileRepository): UserProfileRepository

    @Binds
    fun userFollowRepository(firebaseUserFollowRepository: FirebaseUserFollowRepository):
        UserFollowRepository

    @Binds
    fun recentPlaceRepo(firebaseRecentPlaceRepository: FirebaseUserRecentPlaceRepository):
        UserRecentPlaceRepository

    @Binds
    fun appVersionMigrationState(impl: PreferenceAppVersionMigrationState): AppVersionMigrationState
}

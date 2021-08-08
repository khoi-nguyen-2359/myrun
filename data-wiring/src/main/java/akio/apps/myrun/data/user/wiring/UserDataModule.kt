package akio.apps.myrun.data.user.wiring

import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.data.recentplace.impl.FirebaseUserRecentPlaceRepository
import akio.apps.myrun.data.userfollow.UserFollowRepository
import akio.apps.myrun.data.userfollow.impl.FirebaseUserFollowRepository
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.impl.FirebaseUserProfileRepository
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
}

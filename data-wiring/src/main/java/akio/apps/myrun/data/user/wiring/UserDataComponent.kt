package akio.apps.myrun.data.user.wiring

import akio.apps.common.wiring.DispatchersModule
import akio.apps.myrun.data._base.wiring.FirebaseDataModule
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.data.userfollow.UserFollowRepository
import akio.apps.myrun.data.userprofile.UserProfileRepository
import dagger.Component

@Component(
    modules = [
        UserDataModule::class,
        FirebaseDataModule::class,
        DispatchersModule::class
    ]
)
interface UserDataComponent {
    fun userRecentPlaceRepository(): UserRecentPlaceRepository
    fun userFollowRepository(): UserFollowRepository
    fun userProfileRepository(): UserProfileRepository
}

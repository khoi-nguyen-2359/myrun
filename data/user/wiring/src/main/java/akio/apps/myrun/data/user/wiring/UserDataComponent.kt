package akio.apps.myrun.data.user.wiring

import akio.apps.common.wiring.DispatchersModule
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.data.wiring.FirebaseDataModule
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

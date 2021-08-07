package akio.apps.myrun.data.userfollow.wiring

import akio.apps.myrun.data._base.wiring.FirebaseDataModule
import akio.apps.myrun.data.userfollow.UserFollowRepository
import dagger.Component

@Component(modules = [UserFollowDataModule::class, FirebaseDataModule::class])
interface UserFollowDataComponent {
    fun userFollowRepository(): UserFollowRepository
}

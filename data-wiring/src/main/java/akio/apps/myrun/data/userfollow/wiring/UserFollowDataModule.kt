package akio.apps.myrun.data.userfollow.wiring

import akio.apps.myrun.data.userfollow.UserFollowRepository
import akio.apps.myrun.data.userfollow.impl.FirebaseUserFollowRepository
import dagger.Binds
import dagger.Module

@Module
internal interface UserFollowDataModule {
    @Binds
    fun userFollowRepository(firebaseUserFollowRepository: FirebaseUserFollowRepository):
        UserFollowRepository
}

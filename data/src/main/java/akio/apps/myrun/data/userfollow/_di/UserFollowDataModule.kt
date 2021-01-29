package akio.apps.myrun.data.userfollow._di

import akio.apps.myrun.data.userfollow.UserFollowRepository
import akio.apps.myrun.data.userfollow.impl.UserFollowRepositoryImpl
import dagger.Binds
import dagger.Module

@Module
interface UserFollowDataModule {
    @Binds
    fun userFollowRepository(userFollowRepositoryImpl: UserFollowRepositoryImpl): UserFollowRepository
}

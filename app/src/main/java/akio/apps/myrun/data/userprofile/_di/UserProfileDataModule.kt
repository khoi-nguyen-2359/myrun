package akio.apps.myrun.data.userprofile._di

import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.impl.UserProfileRepositoryImpl
import dagger.Binds
import dagger.Module

@Module
interface UserProfileDataModule {

    @Binds
    fun userProfileRepo(repo: UserProfileRepositoryImpl): UserProfileRepository
}
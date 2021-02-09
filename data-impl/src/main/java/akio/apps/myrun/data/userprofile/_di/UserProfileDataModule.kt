package akio.apps.myrun.data.userprofile._di

import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.impl.FirebaseUserProfileRepository
import dagger.Binds
import dagger.Module

@Module
interface UserProfileDataModule {
    @Binds
    fun userProfileRepo(repo: FirebaseUserProfileRepository): UserProfileRepository
}

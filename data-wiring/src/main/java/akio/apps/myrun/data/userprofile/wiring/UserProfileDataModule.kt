package akio.apps.myrun.data.userprofile.wiring

import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.impl.FirebaseUserProfileRepository
import dagger.Binds
import dagger.Module

@Module
internal interface UserProfileDataModule {
    @Binds
    fun userProfileRepo(repo: FirebaseUserProfileRepository): UserProfileRepository
}

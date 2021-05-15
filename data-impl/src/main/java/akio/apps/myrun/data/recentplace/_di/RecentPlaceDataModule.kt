package akio.apps.myrun.data.recentplace._di

import akio.apps.myrun.data._base.FirebaseDataModule
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.data.recentplace.impl.FirebaseUserRecentPlaceRepository
import dagger.Binds
import dagger.Module

@Module(includes = [FirebaseDataModule::class])
interface RecentPlaceDataModule {
    @Binds
    fun recentPlaceRepo(firebaseRecentPlaceRepository: FirebaseUserRecentPlaceRepository):
        UserRecentPlaceRepository
}

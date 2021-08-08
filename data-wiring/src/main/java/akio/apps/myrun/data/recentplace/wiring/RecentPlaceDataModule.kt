package akio.apps.myrun.data.recentplace.wiring

import akio.apps.myrun.data._base.wiring.FirebaseDataModule
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.data.recentplace.impl.FirebaseUserRecentPlaceRepository
import dagger.Binds
import dagger.Module

@Module(includes = [FirebaseDataModule::class])
internal interface RecentPlaceDataModule {
    @Binds
    fun recentPlaceRepo(firebaseRecentPlaceRepository: FirebaseUserRecentPlaceRepository):
        UserRecentPlaceRepository
}

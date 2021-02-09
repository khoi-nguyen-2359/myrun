package akio.apps.myrun.data.recentplace._di

import akio.apps.myrun.data._base.FirebaseDataModule
import akio.apps.myrun.data.recentplace.RecentPlaceRepository
import akio.apps.myrun.data.recentplace.impl.FirebaseRecentPlaceRepository
import dagger.Binds
import dagger.Module

@Module(includes = [FirebaseDataModule::class])
interface RecentPlaceDataModule {
    @Binds
    fun recentPlaceRepo(firebaseRecentPlaceRepository: FirebaseRecentPlaceRepository): RecentPlaceRepository
}

package akio.apps.myrun.data.recentplace._di

import akio.apps.myrun.data.recentplace.RecentPlaceRepository
import akio.apps.myrun.data.recentplace.impl.RecentPlaceRepositoryImpl
import dagger.Binds
import dagger.Module

@Module
interface RecentPlaceDataModule {
    @Binds
    fun recentPlaceRepo(recentPlaceRepositoryImpl: RecentPlaceRepositoryImpl): RecentPlaceRepository
}
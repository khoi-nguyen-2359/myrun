package akio.apps.myrun.data.activitysharing._di

import akio.apps.myrun.data.activitysharing.ActivityFileTrackingRepository
import akio.apps.myrun.data.activitysharing.ActivityLocationCache
import akio.apps.myrun.data.activitysharing.impl.ActivitySharingDatabase
import akio.apps.myrun.data.activitysharing.impl.ActivityFileTrackingRepositoryImpl
import akio.apps.myrun.data.activitysharing.impl.ActivityLocationCacheImpl
import android.app.Application
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ActivitySharingDataModule.Providers::class])
interface ActivitySharingDataModule {
    @Binds
    fun activityFileTrackingRepo(repositoryImpl: ActivityFileTrackingRepositoryImpl):
        ActivityFileTrackingRepository

    @Binds
    fun activityLocationRepo(repositoryImpl: ActivityLocationCacheImpl):
        ActivityLocationCache

    @Module
    class Providers {
        @Provides
        fun activityFileDatabase(application: Application): ActivitySharingDatabase =
            Room.databaseBuilder(
                application,
                ActivitySharingDatabase::class.java,
                "activity_export_db"
            )
                .enableMultiInstanceInvalidation()
                .build()
    }
}

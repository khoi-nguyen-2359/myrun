package akio.apps.myrun.data.activityfile._di

import akio.apps.myrun.data.activityfile.ActivityFileTrackingRepository
import akio.apps.myrun.data.activityfile.impl.ActivityFileTrackingDatabase
import akio.apps.myrun.data.activityfile.impl.ActivityFileTrackingRepositoryImpl
import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ActivityFileDataModule.Providers::class])
interface ActivityFileDataModule {
    @Binds
    fun activityFileTrackingRepo(repositoryImpl: ActivityFileTrackingRepositoryImpl): ActivityFileTrackingRepository

    @Module
    class Providers {
        @Provides
        @Singleton
        fun activityFileDatabase(application: Context): ActivityFileTrackingDatabase =
            Room.databaseBuilder(
                application,
                ActivityFileTrackingDatabase::class.java,
                "activity_file_tracking_db"
            )
                .build()
    }
}

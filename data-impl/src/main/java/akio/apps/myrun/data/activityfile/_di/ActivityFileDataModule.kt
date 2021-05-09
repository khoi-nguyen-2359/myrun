package akio.apps.myrun.data.activityfile._di

import akio.apps.myrun.data.activityfile.ActivityFileTrackingRepository
import akio.apps.myrun.data.activityfile.ExportActivityLocationRepository
import akio.apps.myrun.data.activityfile.impl.ActivityFileDatabase
import akio.apps.myrun.data.activityfile.impl.ActivityFileTrackingRepositoryImpl
import akio.apps.myrun.data.activityfile.impl.ExportActivityLocationRepositoryImpl
import android.app.Application
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ActivityFileDataModule.Providers::class])
interface ActivityFileDataModule {
    @Binds
    fun activityFileTrackingRepo(repositoryImpl: ActivityFileTrackingRepositoryImpl):
            ActivityFileTrackingRepository

    @Binds
    fun activityLocationRepo(repositoryImpl: ExportActivityLocationRepositoryImpl):
            ExportActivityLocationRepository

    @Module
    class Providers {
        @Provides
        fun activityFileDatabase(application: Application): ActivityFileDatabase =
            Room.databaseBuilder(
                application,
                ActivityFileDatabase::class.java,
                "activity_file_db"
            )
                .enableMultiInstanceInvalidation()
                .build()
    }
}

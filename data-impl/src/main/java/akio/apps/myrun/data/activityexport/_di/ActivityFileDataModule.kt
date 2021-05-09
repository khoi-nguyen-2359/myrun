package akio.apps.myrun.data.activityexport._di

import akio.apps.myrun.data.activityexport.ActivityFileTrackingRepository
import akio.apps.myrun.data.activityexport.ExportActivityLocationRepository
import akio.apps.myrun.data.activityexport.impl.ActivityExportDatabase
import akio.apps.myrun.data.activityexport.impl.ActivityFileTrackingRepositoryImpl
import akio.apps.myrun.data.activityexport.impl.ExportActivityLocationRepositoryImpl
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
        fun activityFileDatabase(application: Application): ActivityExportDatabase =
            Room.databaseBuilder(
                application,
                ActivityExportDatabase::class.java,
                "activity_export_db"
            )
                .enableMultiInstanceInvalidation()
                .build()
    }
}

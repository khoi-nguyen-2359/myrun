package akio.apps.myrun.data.activity._di

import akio.apps.myrun.data._base.FirebaseDataModule
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.ActivityTcxFileWriter
import akio.apps.myrun.data.activity.impl.ActivityTcxFileWriterImpl
import akio.apps.myrun.data.activity.impl.FirebaseActivityRepository
import akio.apps.myrun.data.activitysharing.ActivityLocalStorage
import akio.apps.myrun.data.activitysharing.impl.ActivityLocalStorageImpl
import dagger.Binds
import dagger.Module

@Module(includes = [FirebaseDataModule::class])
interface ActivityDataModule {
    @Binds
    fun activityRepository(repositoryFirebase: FirebaseActivityRepository): ActivityRepository

    @Binds
    fun tcxWriter(writerImpl: ActivityTcxFileWriterImpl): ActivityTcxFileWriter

    @Binds
    fun activityLocalStorage(storageImpl: ActivityLocalStorageImpl): ActivityLocalStorage
}

package akio.apps.myrun.data.activity._di

import akio.apps.myrun.data._base.FirebaseDataModule
import akio.apps.myrun.data.activity.ActivityLocalStorage
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.ActivityTcxFileWriter
import akio.apps.myrun.data.activity.impl.ActivityLocalStorageImpl
import akio.apps.myrun.data.activity.impl.ActivityTcxFileWriterImpl
import akio.apps.myrun.data.activity.impl.FirebaseActivityRepository
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

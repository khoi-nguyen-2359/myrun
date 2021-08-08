package akio.apps.myrun.data.activity.wiring

import akio.apps.myrun.data.activity.ActivityLocalStorage
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.ActivityTcxFileWriter
import akio.apps.myrun.data.activity.impl.ActivityLocalStorageImpl
import akio.apps.myrun.data.activity.impl.ActivityTcxFileWriterImpl
import akio.apps.myrun.data.activity.impl.FirebaseActivityRepository
import dagger.Binds
import dagger.Module

@Module
internal interface ActivityDataModule {
    @Binds
    fun activityRepository(repositoryFirebase: FirebaseActivityRepository): ActivityRepository

    @Binds
    fun tcxWriter(writerImpl: ActivityTcxFileWriterImpl): ActivityTcxFileWriter

    @Binds
    fun activityLocalStorage(storageImpl: ActivityLocalStorageImpl): ActivityLocalStorage
}

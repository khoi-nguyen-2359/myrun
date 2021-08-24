package akio.apps.myrun.wiring.data.activity

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.ActivityTcxFileWriter
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

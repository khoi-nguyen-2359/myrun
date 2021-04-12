package akio.apps.myrun.data.activity._di

import akio.apps.myrun.data._base.FirebaseDataModule
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.impl.FirebaseActivityRepository
import dagger.Binds
import dagger.Module

@Module(includes = [FirebaseDataModule::class])
interface ActivityDataModule {
    @Binds
    fun activityRepository(repositoryFirebase: FirebaseActivityRepository): ActivityRepository
}

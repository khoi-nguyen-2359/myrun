package akio.apps.myrun.data.activity._di

import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.impl.ActivityRepositoryImpl
import dagger.Binds
import dagger.Module

@Module
interface ActivityDataModule {
    @Binds
    fun activityRepository(repositoryImpl: ActivityRepositoryImpl): ActivityRepository
}
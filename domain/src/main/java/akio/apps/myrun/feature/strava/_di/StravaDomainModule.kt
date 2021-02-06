package akio.apps.myrun.feature.strava._di

import akio.apps.myrun.feature.strava.ExportTrackingActivityToStravaFileUsecase
import akio.apps.myrun.feature.strava.GetStravaRoutesUsecase
import akio.apps.myrun.feature.strava.UploadActivityFilesToStravaUsecase
import akio.apps.myrun.feature.strava.impl.ExportTrackingActivityToStravaFileUsecaseImpl
import akio.apps.myrun.feature.strava.impl.GetStravaRoutesUsecaseImpl
import akio.apps.myrun.feature.strava.impl.UploadActivityFilesToStravaUsecaseImpl
import dagger.Binds
import dagger.Module

@Module
interface StravaDomainModule {
    @Binds
    fun getStravaRoutesUsecase(usecase: GetStravaRoutesUsecaseImpl): GetStravaRoutesUsecase

    @Binds
    fun updateLoadRunToStravaUsecase(usecase: UploadActivityFilesToStravaUsecaseImpl): UploadActivityFilesToStravaUsecase

    @Binds
    fun exportRunToFileUsecase(exportRunFileUsecaseImpl: ExportTrackingActivityToStravaFileUsecaseImpl): ExportTrackingActivityToStravaFileUsecase
}

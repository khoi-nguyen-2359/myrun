package akio.apps.myrun.feature.strava._di

import akio.apps.myrun.feature.strava.ExportRunToFileUsecase
import akio.apps.myrun.feature.strava.GetStravaRoutesUsecase
import akio.apps.myrun.feature.strava.UploadRunToStravaUsecase
import akio.apps.myrun.feature.strava.impl.ExportRunToFileUsecaseImpl
import akio.apps.myrun.feature.strava.impl.GetStravaRoutesUsecaseImpl
import akio.apps.myrun.feature.strava.impl.UploadRunToStravaUsecaseImpl
import dagger.Binds
import dagger.Module

@Module
interface StravaConnectFeatureModule {
    @Binds
    fun getStravaRoutesUsecase(usecase: GetStravaRoutesUsecaseImpl): GetStravaRoutesUsecase

    @Binds
    fun updateLoadRunToStravaUsecase(usecase: UploadRunToStravaUsecaseImpl): UploadRunToStravaUsecase

    @Binds
    fun exportRunToFileUsecase(exportRunToFileUsecase: ExportRunToFileUsecaseImpl): ExportRunToFileUsecase
}
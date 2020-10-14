package akio.apps.myrun.feature.externalapp._di

import akio.apps.myrun.feature.externalapp.ExportRunToFileUsecase
import akio.apps.myrun.feature.externalapp.GetStravaRoutesUsecase
import akio.apps.myrun.feature.externalapp.UploadRunToStravaUsecase
import akio.apps.myrun.feature.externalapp.impl.ExportRunToFileUsecaseImpl
import akio.apps.myrun.feature.externalapp.impl.GetStravaRoutesUsecaseImpl
import akio.apps.myrun.feature.externalapp.impl.UploadRunToStravaUsecaseImpl
import dagger.Binds
import dagger.Module

@Module
interface ExternalAppFeatureModule {
    @Binds
    fun getStravaRoutesUsecase(usecase: GetStravaRoutesUsecaseImpl): GetStravaRoutesUsecase

    @Binds
    fun updateLoadRunToStravaUsecase(usecase: UploadRunToStravaUsecaseImpl): UploadRunToStravaUsecase

    @Binds
    fun exportRunToFileUsecase(exportRunToFileUsecase: ExportRunToFileUsecaseImpl): ExportRunToFileUsecase
}
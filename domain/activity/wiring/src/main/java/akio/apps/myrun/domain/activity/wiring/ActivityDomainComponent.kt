package akio.apps.myrun.domain.activity.wiring

import akio.apps.myrun.data.activity.ActivityLocalStorageImpl
import akio.apps.myrun.data.activity.ActivityTcxFileWriterImpl
import akio.apps.myrun.data.activity.FirebaseActivityRepository
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.location.wiring.DaggerLocationDataComponent
import akio.apps.myrun.data.location.wiring.LocationDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.data.wiring.DispatchersModule
import akio.apps.myrun.data.wiring.FirebaseDataModule
import akio.apps.myrun.domain.activity.api.ActivityDateTimeFormatter
import akio.apps.myrun.domain.activity.api.ActivityLocalStorage
import akio.apps.myrun.domain.activity.api.ActivityRepository
import akio.apps.myrun.domain.activity.api.ActivityTcxFileWriter
import akio.apps.myrun.domain.activity.api.ExportTempTcxFileUsecase
import akio.apps.myrun.domain.activity.api.GetFeedActivitiesUsecase
import akio.apps.myrun.domain.activity.api.RunSplitsCalculator
import akio.apps.myrun.domain.activity.impl.ActivityDateTimeFormatterImpl
import akio.apps.myrun.domain.activity.impl.ExportTempTcxFileUsecaseImpl
import akio.apps.myrun.domain.activity.impl.GetFeedActivitiesUsecaseImpl
import akio.apps.myrun.domain.activity.impl.RunSplitsCalculatorImpl
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    dependencies = [
        LocationDataComponent::class,
        UserDataComponent::class,
        AuthenticationDataComponent::class,
    ],
    modules = [
        ActivityComponentModule::class,
        DispatchersModule::class,
        ApplicationModule::class,
        FirebaseDataModule::class
    ]
)
interface ActivityDomainComponent {
    fun getFeedActivitiesUsecase(): GetFeedActivitiesUsecase
    fun exportTempTcxFileUsecase(): ExportTempTcxFileUsecase
    fun runSplitsCalculator(): RunSplitsCalculator
    fun activityDateTimeFormatter(): ActivityDateTimeFormatter
    fun activityLocalStorage(): ActivityLocalStorage
    fun activityRepository(): ActivityRepository
    fun tcxFileWriter(): ActivityTcxFileWriter

    @Component.Factory
    interface Factory {
        fun create(
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create()
        ): ActivityDomainComponent
    }
}

@Module
internal interface ActivityComponentModule {
    @Binds
    fun activityRepository(repositoryFirebase: FirebaseActivityRepository): ActivityRepository

    @Binds
    fun tcxWriter(writerImpl: ActivityTcxFileWriterImpl): ActivityTcxFileWriter

    @Binds
    fun activityLocalStorage(storageImpl: ActivityLocalStorageImpl): ActivityLocalStorage

    @Binds
    fun getFeedActivitiesUsecase(usecaseImpl: GetFeedActivitiesUsecaseImpl): GetFeedActivitiesUsecase

    @Binds
    fun exportTempTcxFileUsecase(usecaseImpl: ExportTempTcxFileUsecaseImpl): ExportTempTcxFileUsecase

    @Binds
    fun runSplitsCalculator(usecaseImpl: RunSplitsCalculatorImpl): RunSplitsCalculator

    @Binds
    fun activityDateTimeFormatter(impl: ActivityDateTimeFormatterImpl): ActivityDateTimeFormatter
}

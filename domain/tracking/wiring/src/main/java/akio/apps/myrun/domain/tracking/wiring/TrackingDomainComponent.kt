package akio.apps.myrun.domain.tracking.wiring

import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.eapps.wiring.DaggerExternalAppDataComponent
import akio.apps.myrun.data.eapps.wiring.ExternalAppDataComponent
import akio.apps.myrun.data.location.wiring.DaggerLocationDataComponent
import akio.apps.myrun.data.location.wiring.LocationDataComponent
import akio.apps.myrun.data.tracking.impl.GoogleFitnessDataRepository
import akio.apps.myrun.data.tracking.impl.PreferencesRouteTrackingState
import akio.apps.myrun.data.tracking.impl.RouteTrackingConfigurationImpl
import akio.apps.myrun.data.tracking.impl.RouteTrackingDatabase
import akio.apps.myrun.data.tracking.impl.RouteTrackingLocationDao
import akio.apps.myrun.data.tracking.impl.RouteTrackingLocationRepositoryImpl
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.data.wiring.DispatchersModule
import akio.apps.myrun.data.wiring.FirebaseDataModule
import akio.apps.myrun.domain.activity.api.UploadActivitiesUsecase
import akio.apps.myrun.domain.activity.wiring.ActivityDomainComponent
import akio.apps.myrun.domain.activity.wiring.DaggerActivityDomainComponent
import akio.apps.myrun.domain.common.ObjectAutoId
import akio.apps.myrun.domain.tracking.api.ClearRouteTrackingStateUsecase
import akio.apps.myrun.domain.tracking.api.FitnessDataRepository
import akio.apps.myrun.domain.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.domain.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.domain.tracking.api.RouteTrackingState
import akio.apps.myrun.domain.tracking.api.StoreTrackingActivityDataUsecase
import akio.apps.myrun.domain.tracking.impl.ClearRouteTrackingStateUsecaseImpl
import akio.apps.myrun.domain.tracking.impl.StoreTrackingActivityDataUsecaseImpl
import android.app.Application
import androidx.room.Room
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides

@Component(
    modules = [
        DispatchersModule::class,
        TrackingDomainModule::class,
        ApplicationModule::class,
        FirebaseDataModule::class
    ],
    dependencies = [
        LocationDataComponent::class,
        UserDataComponent::class,
        ActivityDomainComponent::class,
        AuthenticationDataComponent::class,
        ExternalAppDataComponent::class
    ]
)
interface TrackingDomainComponent {
    fun clearRouteTrackingStateUsecase(): ClearRouteTrackingStateUsecase
    fun storeTrackingActivityDataUsecase(): StoreTrackingActivityDataUsecase
    fun uploadActivitiesUsecase(): UploadActivitiesUsecase

    fun routeTrackingLocationRepo(): RouteTrackingLocationRepository
    fun routeTrackingConfiguration(): RouteTrackingConfiguration
    fun routeTrackingState(): RouteTrackingState
    fun fitnessDataRepository(): FitnessDataRepository

    fun objectAutoId(): ObjectAutoId

    @Component.Factory
    interface Factory {
        fun create(
            locationDataComponent: LocationDataComponent = DaggerLocationDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            activityDomainComponent: ActivityDomainComponent =
                DaggerActivityDomainComponent.factory().create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            externalAppDataComponent: ExternalAppDataComponent =
                DaggerExternalAppDataComponent.factory().create(),
        ): TrackingDomainComponent
    }
}

@Module(includes = [TrackingDomainModule.Providers::class])
internal interface TrackingDomainModule {
    @Binds
    fun clearRouteTrackingStateUsecase(
        impl: ClearRouteTrackingStateUsecaseImpl,
    ): ClearRouteTrackingStateUsecase

    @Binds
    fun storeTrackingActivityDataUsecase(
        impl: StoreTrackingActivityDataUsecaseImpl,
    ): StoreTrackingActivityDataUsecase

    @Binds
    fun routeTrackingLocationRepo(repositoryImpl: RouteTrackingLocationRepositoryImpl):
        RouteTrackingLocationRepository

    @Binds
    fun routeTrackingConfiguration(impl: RouteTrackingConfigurationImpl): RouteTrackingConfiguration

    @Binds
    fun routeTrackingState(impl: PreferencesRouteTrackingState): RouteTrackingState

    @Binds
    fun fitnessDataRepository(repositoryGoogle: GoogleFitnessDataRepository): FitnessDataRepository

    @Module
    class Providers {
        @Provides
        fun routeTrackingDatabase(application: Application): RouteTrackingDatabase =
            Room.databaseBuilder(
                application,
                RouteTrackingDatabase::class.java,
                "route_tracking_db"
            )
                .enableMultiInstanceInvalidation()
                .fallbackToDestructiveMigrationFrom(1)
                .build()

        @Provides
        fun routeTrackingLocationDao(database: RouteTrackingDatabase): RouteTrackingLocationDao =
            database.trackingLocationDao()
    }
}

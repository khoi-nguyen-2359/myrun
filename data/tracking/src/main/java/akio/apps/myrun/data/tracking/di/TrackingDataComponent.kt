package akio.apps.myrun.data.tracking.di

import akio.apps.myrun.data.tracking.TrackingDataModule
import akio.apps.myrun.data.tracking.api.FitnessDataRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.RouteTrackingLocationRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import android.app.Application
import com.squareup.anvil.annotations.MergeComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

abstract class TrackingDataScope private constructor()

@Singleton
@MergeComponent(
    scope = TrackingDataScope::class,
    modules = [TrackingDataModule::class]
)
interface TrackingDataComponent {
    fun routeTrackingConfig(): RouteTrackingConfiguration
    fun routeTrackingLocationRepo(): RouteTrackingLocationRepository
    fun routeTrackingState(): RouteTrackingState
    fun fitnessDataRepo(): FitnessDataRepository

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): TrackingDataComponent
    }
}

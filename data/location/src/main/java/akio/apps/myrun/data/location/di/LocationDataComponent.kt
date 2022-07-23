package akio.apps.myrun.data.location.di

import akio.apps.myrun.data.location.LocationDataModule
import akio.apps.myrun.data.location.api.DirectionDataSource
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.PlaceDataSource
import akio.apps.myrun.data.location.api.PolyUtil
import akio.apps.myrun.data.location.api.SphericalUtil
import android.app.Application
import com.squareup.anvil.annotations.MergeComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

abstract class LocationDataScope private constructor()

@Singleton
@MergeComponent(
    scope = LocationDataScope::class,
    modules = [LocationDataModule::class]
)
interface LocationDataComponent {
    fun locationDataSource(): LocationDataSource
    fun directionDataSource(): DirectionDataSource
    fun sphericalUtil(): SphericalUtil
    fun polyUtil(): PolyUtil
    fun placeDataSource(): PlaceDataSource

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ): LocationDataComponent
    }
}

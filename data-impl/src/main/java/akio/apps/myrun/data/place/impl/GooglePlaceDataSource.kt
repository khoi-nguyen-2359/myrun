package akio.apps.myrun.data.place.impl

import akio.apps.myrun.data.place.LatLngEntity
import akio.apps.myrun.data.place.PlaceDataSource
import akio.apps.myrun.data.place.PlaceEntity
import akio.apps.myrun.data.place.entity.PlaceAddressComponent
import android.annotation.SuppressLint
import android.app.Application
import android.location.Address
import android.location.Geocoder
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

class GooglePlaceDataSource @Inject constructor(
    private val placesClient: PlacesClient,
    private val application: Application
) : PlaceDataSource {
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentPlace(): PlaceEntity? {
        val request: FindCurrentPlaceRequest =
            FindCurrentPlaceRequest.newInstance(listOf(Place.Field.ID, Place.Field.NAME))

        val placeResponse = placesClient.findCurrentPlace(request)
            .await()
        placeResponse.placeLikelihoods
            .maxByOrNull { it.likelihood }
            ?.let { placeLikelihood ->
                val placeId = placeLikelihood.place.id
                    ?: return null

                val fetchPlaceRequest =
                    FetchPlaceRequest.newInstance(placeId, listOf(Place.Field.ADDRESS_COMPONENTS))
                val fetchPlaceResponse = placesClient.fetchPlace(fetchPlaceRequest)
                    .await()
                val latLng = fetchPlaceResponse.place.latLng
                return PlaceEntity(
                    placeId,
                    placeLikelihood.place.name ?: "",
                    fetchPlaceResponse.place.addressComponents?.asList()
                        ?.map { PlaceAddressComponent(it.name, it.types) } ?: emptyList(),
                    LatLngEntity(latLng?.latitude ?: 0.0, latLng?.longitude ?: 0.0)
                )
            }

        return null
    }

    override suspend fun getAddressFromLocation(
        lat: Double,
        lng: Double
    ): List<PlaceAddressComponent> {
        val geoCoder = Geocoder(application, Locale.US)
        val addresses = geoCoder.getFromLocation(lat, lng, Int.MAX_VALUE)
        val addedAddressTypes = mutableSetOf<String>()
        val createAddressEntries: (Address) -> List<Pair<String?, String>> = {
            listOf(
                it.countryCode to GEOCODER_ADDRESS_TYPE_COUNTRY,
                it.adminArea to GEOCODER_ADDRESS_ADMIN,
                it.locality to GEOCODER_ADDRESS_LOCALITY,
                it.subAdminArea to GEOCODER_ADDRESS_SUB_ADMIN,
                it.subLocality to GEOCODER_ADDRESS_SUB_LOCALITY,
                it.thoroughfare to GEOCODER_ADDRESS_THOROUGHFARE,
                it.subThoroughfare to GEOCODER_ADDRESS_SUB_THOROUGHFARE,
                it.featureName to GEOCODER_ADDRESS_FEATURE
            )
        }
        return addresses?.flatMap { addr ->
            val addressComponents = mutableListOf<PlaceAddressComponent>()
            createAddressEntries(addr).forEach { (addressComponentName, addressComponentType) ->
                if (addressComponentName != null &&
                    !addedAddressTypes.contains(addressComponentType)
                ) {
                    addressComponents.add(
                        PlaceAddressComponent(
                            addressComponentName,
                            listOf(addressComponentType)
                        )
                    )
                    addedAddressTypes.add(addressComponentType)
                }
            }

            addressComponents
        }
            ?: emptyList()
    }

    override fun getRecentPlaceAddressSortingOrder(): List<String> {
        return listOf(
            GEOCODER_ADDRESS_TYPE_COUNTRY,
            GEOCODER_ADDRESS_ADMIN,
            GEOCODER_ADDRESS_LOCALITY,
            GEOCODER_ADDRESS_SUB_ADMIN,
            GEOCODER_ADDRESS_SUB_LOCALITY
        )
    }

    companion object {
        const val GEOCODER_ADDRESS_TYPE_COUNTRY = "countryCode"
        const val GEOCODER_ADDRESS_LOCALITY = "locality"
        const val GEOCODER_ADDRESS_SUB_LOCALITY = "sub-locality"
        const val GEOCODER_ADDRESS_ADMIN = "admin"
        const val GEOCODER_ADDRESS_SUB_ADMIN = "sub-admin"
        const val GEOCODER_ADDRESS_THOROUGHFARE = "thoroughfare"
        const val GEOCODER_ADDRESS_SUB_THOROUGHFARE = "sub-thoroughfare"
        const val GEOCODER_ADDRESS_FEATURE = "feature"
    }
}

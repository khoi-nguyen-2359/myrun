package akio.apps.myrun.data.place.impl

import akio.apps.myrun._di.NamedIoDispatcher
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
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GooglePlaceDataSource @Inject constructor(
    private val placesClient: PlacesClient,
    private val application: Application,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PlaceDataSource {
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentPlace(): PlaceEntity? = withContext(ioDispatcher) {
        val request: FindCurrentPlaceRequest =
            FindCurrentPlaceRequest.newInstance(listOf(Place.Field.ID, Place.Field.NAME))

        val placeResponse = placesClient.findCurrentPlace(request).await()
        placeResponse.placeLikelihoods
            .maxByOrNull { it.likelihood }
            ?.let { placeLikelihood ->
                val placeId = placeLikelihood.place.id
                    ?: return@withContext null

                val fetchPlaceRequest =
                    FetchPlaceRequest.newInstance(placeId, listOf(Place.Field.ADDRESS_COMPONENTS))
                val fetchPlaceResponse = placesClient.fetchPlace(fetchPlaceRequest).await()
                val latLng = fetchPlaceResponse.place.latLng
                return@withContext PlaceEntity(
                    placeId,
                    placeLikelihood.place.name ?: "",
                    fetchPlaceResponse.place.addressComponents?.asList()
                        ?.map { PlaceAddressComponent(it.name, it.types) } ?: emptyList(),
                    LatLngEntity(latLng?.latitude ?: 0.0, latLng?.longitude ?: 0.0)
                )
            }

        return@withContext null
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
                it.subAdminArea to GEOCODER_ADDRESS_SUB_ADMIN,
                it.locality to GEOCODER_ADDRESS_LOCALITY,
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

    private fun getRecentPlaceAddressSortingOrder(): List<String> {
        return listOf(
            GEOCODER_ADDRESS_TYPE_COUNTRY,
            GEOCODER_ADDRESS_ADMIN,
            GEOCODER_ADDRESS_SUB_ADMIN,
            GEOCODER_ADDRESS_LOCALITY,
            GEOCODER_ADDRESS_SUB_LOCALITY
        )
    }

    override suspend fun getRecentPlaceAddressFromLocation(
        lat: Double,
        lng: Double
    ): List<PlaceAddressComponent> {
        val sortingOrder = mutableMapOf<String, Int>()
        getRecentPlaceAddressSortingOrder()
            .forEachIndexed { index, addressType -> sortingOrder[addressType] = index }

        val addressComponents = getAddressFromLocation(lat, lng)
            .filter { addressComponent ->
                addressComponent.types.any { addressType -> sortingOrder.containsKey(addressType) }
            }

        return addressComponents.sortedBy { addressComponent ->
            addressComponent.types.find { addressType -> sortingOrder[addressType] != null }
                ?.let { addressType -> sortingOrder[addressType] }
                ?: Int.MAX_VALUE
        }
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

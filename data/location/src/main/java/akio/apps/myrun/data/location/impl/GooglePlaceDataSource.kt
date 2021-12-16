package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.api.PlaceDataSource
import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.location.api.model.PlaceAddressComponent
import akio.apps.myrun.data.location.api.model.PlaceDetails
import akio.apps.myrun.data.location.api.model.PlaceSuggestion
import akio.apps.myrun.wiring.common.NamedIoDispatcher
import android.annotation.SuppressLint
import android.app.Application
import android.location.Address
import android.location.Geocoder
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.Lazy
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GooglePlaceDataSource @Inject constructor(
    /**
     * Use lazy to avoid uninitialized Places when providing PlacesClient
     */
    private val placesClientLazy: Lazy<PlacesClient>,
    private val application: Application,
    @akio.apps.myrun.wiring.common.NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PlaceDataSource {

    private val placesClient by lazy { placesClientLazy.get() }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentPlace(): PlaceDetails? = withContext(ioDispatcher) {
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
                return@withContext PlaceDetails(
                    placeId,
                    placeLikelihood.place.name ?: "",
                    fetchPlaceResponse.place.addressComponents?.asList()
                        ?.map { PlaceAddressComponent(it.name, it.types) } ?: emptyList(),
                    LatLng(latLng?.latitude ?: 0.0, latLng?.longitude ?: 0.0)
                )
            }

        return@withContext null
    }

    override suspend fun getAddressFromLocation(
        lat: Double,
        lng: Double,
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
        lng: Double,
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

    override suspend fun getPlaceSuggestions(query: String): List<PlaceSuggestion> {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()
        val response = placesClient.findAutocompletePredictions(request).await()
        return response.autocompletePredictions.map { prediction ->
            PlaceSuggestion(
                prediction.placeId,
                placeName = prediction.getPrimaryText(null).toString(),
                placeInformation = prediction.getSecondaryText(null).toString()
            )
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

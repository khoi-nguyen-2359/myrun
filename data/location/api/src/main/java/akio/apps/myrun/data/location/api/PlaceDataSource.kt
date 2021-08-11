package akio.apps.myrun.data.location.api

import akio.apps.myrun.data.location.api.model.PlaceAddressComponent
import akio.apps.myrun.data.location.api.model.PlaceDetails

interface PlaceDataSource {
    suspend fun getCurrentPlace(): PlaceDetails?
    suspend fun getAddressFromLocation(lat: Double, lng: Double): List<PlaceAddressComponent>
    suspend fun getRecentPlaceAddressFromLocation(lat: Double, lng: Double):
        List<PlaceAddressComponent>
}

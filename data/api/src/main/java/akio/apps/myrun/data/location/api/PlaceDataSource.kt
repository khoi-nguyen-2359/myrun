package akio.apps.myrun.data.location.api

import akio.apps.myrun.data.location.api.model.PlaceAddressComponent
import akio.apps.myrun.data.location.api.model.PlaceDetails
import akio.apps.myrun.data.location.api.model.PlaceSuggestion

interface PlaceDataSource {
    suspend fun getCurrentPlace(): PlaceDetails?
    suspend fun getRecentPlaceAddressFromLocation(lat: Double, lng: Double):
        List<PlaceAddressComponent>
    suspend fun getPlaceSuggestions(query: String): List<PlaceSuggestion>
}

package akio.apps.myrun.data.place

import akio.apps.myrun.data.place.entity.PlaceAddressComponent

interface PlaceDataSource {
    suspend fun getCurrentPlace(): PlaceEntity?
    suspend fun getAddressFromLocation(lat: Double, lng: Double): List<PlaceAddressComponent>
    suspend fun getRecentPlaceAddressFromLocation(lat: Double, lng: Double):
        List<PlaceAddressComponent>
}

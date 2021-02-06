package akio.apps.myrun.data.place

import akio.apps.myrun.data.place.entity.PlaceAddressComponent

interface PlaceDataSource {
    suspend fun getCurrentPlace(): PlaceEntity?
    fun getRecentPlaceAddressSortingOrder(): List<String>
    suspend fun getAddressFromLocation(lat: Double, lng: Double): List<PlaceAddressComponent>
}

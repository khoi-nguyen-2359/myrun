package akio.apps.myrun.data.place

interface PlaceDataSource {
    suspend fun getCurrentPlace(): PlaceEntity?
    fun getRecentPlaceAddressSortingOrder(): List<String>
    suspend fun getAddressFromLocation(lat: Double, lng: Double): List<PlaceAddressComponent>
}

package akio.apps.myrun.domain.recentplace

import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.place.PlaceDataSource
import akio.apps.myrun.data.recentplace.RecentPlaceRepository
import javax.inject.Inject

class UpdateUserRecentPlaceUsecase @Inject constructor(
    private val placeDataSource: PlaceDataSource,
    private val userAuthenticationState: UserAuthenticationState,
    private val recentPlaceRepository: RecentPlaceRepository
) {
    suspend fun updateUserRecentPlace(lat: Double, lng: Double) {
        val userId = userAuthenticationState.getUserAccountId()
            ?: throw UnauthorizedUserError()

        val sortingOrder = mutableMapOf<String, Int>()
        placeDataSource.getRecentPlaceAddressSortingOrder()
            .forEachIndexed { index, addressType -> sortingOrder[addressType] = index }

        val addressComponents = placeDataSource.getAddressFromLocation(lat, lng)
            .filter { addressComponent ->
                addressComponent.types.any { addressType -> sortingOrder.containsKey(addressType) }
            }

        val sortedAddressTexts = addressComponents.sortedBy { addressComponent ->
            addressComponent.types.find { addressType -> sortingOrder[addressType] != null }
                ?.let { addressType -> sortingOrder[addressType] }
                ?: Int.MAX_VALUE
        }
            .map { it.name }

        recentPlaceRepository.saveRecentPlace(userId, sortedAddressTexts)
    }
}

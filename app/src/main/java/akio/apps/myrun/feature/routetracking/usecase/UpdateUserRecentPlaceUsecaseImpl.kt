package akio.apps.myrun.feature.routetracking.usecase

import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.place.PlaceDataSource
import akio.apps.myrun.data.recentplace.RecentPlaceRepository
import akio.apps.myrun.feature.routetracking.UpdateUserRecentPlaceUsecase
import javax.inject.Inject

class UpdateUserRecentPlaceUsecaseImpl @Inject constructor(
    private val placeDataSource: PlaceDataSource,
    private val userAuthenticationState: UserAuthenticationState,
    private val recentPlaceRepository: RecentPlaceRepository
) : UpdateUserRecentPlaceUsecase {
    override suspend fun updateUserRecentPlace(lat: Double, lng: Double) {
        val userId = userAuthenticationState.getUserAccountId()
            ?: throw UnauthorizedUserError()

        val sortingOrder = mutableMapOf<String, Int>()
        placeDataSource.getRecentPlaceAddressSortingOrder()
            .forEachIndexed { index, item -> sortingOrder[item] = index }

        val addressComponents = placeDataSource.getAddressFromLocation(lat, lng)
            .filter { component -> component.types.any { type -> sortingOrder.containsKey(type) } }

        val sortedAddressTexts = addressComponents.sortedBy {
            it.types.find { sortingOrder[it] != null }
                ?.let { sortingType -> sortingOrder[sortingType] }
                ?: Int.MAX_VALUE
        }
            .map { it.name }

        recentPlaceRepository.saveRecentPlace(userId, sortedAddressTexts)
    }
}
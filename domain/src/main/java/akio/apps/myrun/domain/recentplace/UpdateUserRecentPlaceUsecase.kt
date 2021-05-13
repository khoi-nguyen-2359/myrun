package akio.apps.myrun.domain.recentplace

import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.place.PlaceDataSource
import akio.apps.myrun.data.recentplace.RecentPlaceRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import javax.inject.Inject

class UpdateUserRecentPlaceUsecase @Inject constructor(
    private val placeDataSource: PlaceDataSource,
    private val userAuthenticationState: UserAuthenticationState,
    private val recentPlaceRepository: RecentPlaceRepository,
    private val routeTrackingState: RouteTrackingState
) {
    suspend operator fun invoke(lat: Double, lng: Double) {
        val userId = userAuthenticationState.getUserAccountId()
            ?: throw UnauthorizedUserError()

        val sortedAddressTexts = placeDataSource.getRecentPlaceAddressFromLocation(lat, lng)
            .map { it.name }
            .distinct()

        val areaIdentifier = sortedAddressTexts.joinToString("-")
        recentPlaceRepository.saveRecentPlace(userId, areaIdentifier)
        routeTrackingState.setPlaceIdentifier(areaIdentifier)
    }
}

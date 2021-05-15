package akio.apps.myrun.domain.recentplace

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.place.PlaceDataSource
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.data.recentplace.entity.PlaceIdentifier
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import javax.inject.Inject

class UpdateUserRecentPlaceUsecase @Inject constructor(
    private val placeDataSource: PlaceDataSource,
    private val userAuthenticationState: UserAuthenticationState,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val routeTrackingState: RouteTrackingState
) {
    suspend operator fun invoke(lat: Double, lng: Double) {
        val userId = userAuthenticationState.getUserAccountId() ?: return

        val sortedAddressTexts = placeDataSource.getRecentPlaceAddressFromLocation(lat, lng)

        val placeIdentifier = PlaceIdentifier.fromAddressComponentList(sortedAddressTexts)
        userRecentPlaceRepository.saveRecentPlace(userId, placeIdentifier.identifier)

        if (routeTrackingState.getTrackingStatus() != RouteTrackingStatus.STOPPED) {
            routeTrackingState.setPlaceIdentifier(placeIdentifier.identifier)
        }
    }
}

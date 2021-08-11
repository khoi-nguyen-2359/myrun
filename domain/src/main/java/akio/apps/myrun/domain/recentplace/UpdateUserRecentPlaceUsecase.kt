package akio.apps.myrun.domain.recentplace

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.location.api.PlaceDataSource
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.RouteTrackingStatus
import javax.inject.Inject

class UpdateUserRecentPlaceUsecase @Inject constructor(
    private val placeDataSource: PlaceDataSource,
    private val userAuthenticationState: UserAuthenticationState,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val routeTrackingState: RouteTrackingState,
    private val placeIdentifierConverter: PlaceIdentifierConverter
) {
    suspend operator fun invoke(lat: Double, lng: Double) {
        val userId = userAuthenticationState.getUserAccountId() ?: return

        val sortedAddressTexts = placeDataSource.getRecentPlaceAddressFromLocation(lat, lng)

        val placeIdentifier = placeIdentifierConverter.fromAddressComponentList(sortedAddressTexts)
        userRecentPlaceRepository.saveRecentPlace(userId, placeIdentifier)

        if (routeTrackingState.getTrackingStatus() != RouteTrackingStatus.STOPPED) {
            routeTrackingState.setPlaceIdentifier(placeIdentifier)
        }
    }
}

package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.PlaceDataSource
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import java.io.IOException
import javax.inject.Inject

class UpdateUserRecentPlaceUsecase @Inject constructor(
    private val locationDataSource: LocationDataSource,
    private val placeDataSource: PlaceDataSource,
    private val userAuthenticationState: UserAuthenticationState,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val routeTrackingState: RouteTrackingState,
    private val placeIdentifierConverter: PlaceIdentifierConverter,
) {
    suspend operator fun invoke(): Result {
        val userId = userAuthenticationState.getUserAccountId()
            ?: return Result.InvalidUser
        val lastLocation = locationDataSource.getLastLocation()
            ?: return Result.LocationUnavailable

        try {
            val sortedAddressTexts = placeDataSource.getRecentPlaceAddressFromLocation(
                lastLocation.latitude,
                lastLocation.longitude
            )

            val placeIdentifier =
                placeIdentifierConverter.fromAddressNameList(sortedAddressTexts.map { it.name })
            userRecentPlaceRepository.saveRecentPlace(userId, placeIdentifier)

            if (routeTrackingState.getTrackingStatus() != RouteTrackingStatus.STOPPED) {
                routeTrackingState.setPlaceIdentifier(placeIdentifier)
            }
        } catch (ex: IOException) {
            return Result.IOFailure
        }
        return Result.Success
    }

    enum class Result {
        LocationUnavailable,
        Success,
        IOFailure,
        InvalidUser
    }
}

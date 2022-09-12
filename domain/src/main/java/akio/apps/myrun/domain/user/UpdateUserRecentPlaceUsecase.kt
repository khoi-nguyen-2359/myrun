package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.PlaceDataSource
import akio.apps.myrun.data.user.api.UserRecentActivityRepository
import akio.apps.myrun.data.user.api.model.PlaceIdentifier
import java.io.IOException
import javax.inject.Inject

class UpdateUserRecentPlaceUsecase @Inject constructor(
    private val locationDataSource: LocationDataSource,
    private val placeDataSource: PlaceDataSource,
    private val userAuthenticationState: UserAuthenticationState,
    private val userRecentActivityRepository: UserRecentActivityRepository,
) {
    suspend fun updateUserRecentPlace(): Result {
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
                PlaceIdentifier.fromAddressComponents(sortedAddressTexts.map { it.name })
            if (placeIdentifier != null) {
                userRecentActivityRepository.saveRecentPlace(userId, placeIdentifier)
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
        InvalidUser,
    }
}

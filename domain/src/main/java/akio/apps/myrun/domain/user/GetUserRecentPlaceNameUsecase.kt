package akio.apps.myrun.domain.user

import akio.apps.myrun.data.user.api.UserRecentActivityRepository
import javax.inject.Inject

class GetUserRecentPlaceNameUsecase @Inject constructor(
    private val userRecentActivityRepository: UserRecentActivityRepository,
    private val placeNameSelector: PlaceNameSelector,
) {
    suspend fun getUserRecentPlaceName(userId: String): String? {
        return userRecentActivityRepository.getRecentPlaceIdentifier(userId, useCache = false)
            .let { placeNameSelector.select(it, it) }
    }
}

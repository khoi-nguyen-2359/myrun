package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.PlaceIdentifier
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetUserRecentPlaceNameUsecase @Inject constructor(
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val placeNameSelector: PlaceNameSelector
) {
    fun getUserRecentPlaceNameFlow(): Flow<PlaceIdentifier?> {
        val userId = userAuthenticationState.requireUserAccountId()
        return userRecentPlaceRepository.getRecentPlaceIdentifierFlow(userId).map {
            placeNameSelector.invoke(it.data, it.data)
        }
    }
}

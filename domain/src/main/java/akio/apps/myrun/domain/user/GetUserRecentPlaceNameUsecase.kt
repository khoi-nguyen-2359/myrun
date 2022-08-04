package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.PlaceIdentifier
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class GetUserRecentPlaceNameUsecase @Inject constructor(
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val placeNameSelector: PlaceNameSelector,
) {
    fun getUserRecentPlaceNameFlow(): Flow<PlaceIdentifier?> {
        val userId = userAuthenticationState.requireUserAccountId()
        return userRecentPlaceRepository.getRecentPlaceIdentifierFlow(userId)
            .onStart { emit(Resource.Success(null)) }
            .map {
                placeNameSelector.select(it.data, it.data)
            }
    }
}

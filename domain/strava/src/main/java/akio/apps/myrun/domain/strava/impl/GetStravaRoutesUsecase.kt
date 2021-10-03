package akio.apps.myrun.domain.strava.impl

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.api.StravaDataRepository
import akio.apps.myrun.data.eapps.api.model.StravaRouteModel
import akio.apps.myrun.domain.common.error.UnauthorizedUserError
import javax.inject.Inject

class GetStravaRoutesUsecase @Inject constructor(
    private val stravaDataRepository: StravaDataRepository,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState,
) {
    suspend fun getStravaRoutes(): List<StravaRouteModel> {
        val userAccountId = userAuthenticationState.getUserAccountId()
            ?: throw UnauthorizedUserError()

        val stravaToken = externalAppProvidersRepository
            .getStravaProviderToken(userAccountId)
            ?: return emptyList()

        return stravaDataRepository.getRoutes(stravaToken)
    }
}

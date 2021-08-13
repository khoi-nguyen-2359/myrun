package akio.apps.myrun.domain.strava

import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.api.StravaDataRepository
import akio.apps.myrun.data.eapps.api.model.StravaRouteModel
import javax.inject.Inject

class GetStravaRoutesUsecase @Inject constructor(
    private val stravaDataRepository: StravaDataRepository,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val userAuthenticationState: UserAuthenticationState
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

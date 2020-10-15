package akio.apps.myrun.feature.editprofile

import akio.apps.myrun.data.externalapp.model.ExternalAppToken

interface UpdateStravaTokenUsecase {
    suspend fun updateStravaToken(stravaToken: ExternalAppToken.StravaToken)
}
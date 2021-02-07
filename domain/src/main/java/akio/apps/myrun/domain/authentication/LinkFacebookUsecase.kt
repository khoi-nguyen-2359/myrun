package akio.apps.myrun.domain.authentication

import akio.apps.myrun.data.authentication.SignInManager
import javax.inject.Inject

class LinkFacebookUsecase @Inject constructor(
    private val signInManager: SignInManager
) {
    suspend fun linkFacebook(facebookAccessToken: String) {
        signInManager.linkFacebook(facebookAccessToken)
    }
}

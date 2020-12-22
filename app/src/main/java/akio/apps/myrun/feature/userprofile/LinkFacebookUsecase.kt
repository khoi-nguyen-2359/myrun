package akio.apps.myrun.feature.userprofile

interface LinkFacebookUsecase {
    suspend fun linkFacebook(accessTokenValue: String)
}
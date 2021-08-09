package akio.apps.myrun.domain.authentication

import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.authentication.model.SignInSuccessResult
import akio.apps.myrun.data.authentication.model.UserAccount
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import javax.inject.Inject

/**
 * Finishes all jobs after signing in successfully.
 */
class PostSignInUsecase @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val userProfileRepository: UserProfileRepository,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository
) {

    suspend operator fun invoke(result: SignInSuccessResult) {
        val userAccount = userAuthenticationState.getUserAccount()
        if (userAccount != null) {
            if (result.isNewUser) {
                syncUserProfile(userAccount)
            }

            // fetch strava sync status for using in offline mode
            externalAppProvidersRepository.getStravaProviderToken(userAccount.uid)
        }
    }

    /**
     * In case user doesn't make a profile update in onboarding process (for new user), this usecase will clone info from auth account to user profile.
     */
    private suspend fun syncUserProfile(userAccount: UserAccount) {
        val editData = ProfileEditData.createFromUserAccount(userAccount)
        userProfileRepository.updateUserProfile(userAccount.uid, editData)
    }
}
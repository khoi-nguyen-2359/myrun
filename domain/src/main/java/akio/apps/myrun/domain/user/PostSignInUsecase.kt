package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.authentication.api.model.SignInSuccessResult
import akio.apps.myrun.data.authentication.api.model.UserAccount
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.model.ProfileEditData
import java.io.IOException
import javax.inject.Inject
import timber.log.Timber

/**
 * Finishes all jobs after signing in successfully.
 */
class PostSignInUsecase @Inject constructor(
    private val userAuthenticationState: UserAuthenticationState,
    private val userProfileRepository: UserProfileRepository,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
) {

    suspend fun invoke(result: SignInSuccessResult) {
        val userAccount = userAuthenticationState.getUserAccount()
        if (userAccount != null) {
            if (result.isNewUser) {
                syncUserProfile(userAccount)
            }

            syncStravaToken(userAccount)
        }
    }

    /**
     * Fetch strava sync status to be able to check that flag in offline mode (eg, check to
     * kick-start worker for strava sync after recording an activity in offline mode)
     */
    private suspend fun syncStravaToken(userAccount: UserAccount) = try {
        // need try catch in case logging in without internet (google sign in with cached session)
        externalAppProvidersRepository.getStravaProviderToken(userAccount.uid)
    } catch (ioEx: IOException) {
        // let the strava sync flag untouched.
        Timber.e(ioEx)
    }

    /**
     * Clones info from auth account to user profile database.
     */
    private suspend fun syncUserProfile(userAccount: UserAccount) {
        val editData = ProfileEditData.createFromUserAccount(userAccount)
        userProfileRepository.updateUserProfile(userAccount.uid, editData)
    }
}

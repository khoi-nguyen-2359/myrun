package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserProfileRepository
import javax.inject.Inject

class UploadUserAvatarImageUsecase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val userAuthenticationState: UserAuthenticationState,
) {

    suspend fun uploadUserAvatarImage(imageFileUri: String) =
        userAuthenticationState.requireUserAccountId().let { userId ->
            userProfileRepository.uploadUserAvatarImage(userId, imageFileUri)
        }
}

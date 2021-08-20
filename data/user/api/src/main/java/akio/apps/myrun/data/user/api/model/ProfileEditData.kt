package akio.apps.myrun.data.user.api.model

import akio.apps.myrun.data.authentication.api.model.UserAccount
import android.net.Uri

// Leave a field null in order not to include that field in the update request.
data class ProfileEditData(
    val displayName: String? = null,
    val birthdate: Long? = null,
    val gender: Gender? = null,
    val weight: Float? = null,
    val avatarUri: Uri? = null,
    val phoneNumber: String? = null
) {
    companion object {
        fun createFromUserAccount(userAccount: UserAccount): ProfileEditData {
            return ProfileEditData(
                displayName = userAccount.displayName ?: "",
                avatarUri = userAccount.photoUrl?.let { Uri.parse(it) },
                phoneNumber = userAccount.phoneNumber
            )
        }
    }
}

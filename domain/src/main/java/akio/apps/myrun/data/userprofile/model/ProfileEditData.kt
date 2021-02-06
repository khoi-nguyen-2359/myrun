package akio.apps.myrun.data.userprofile.model

import akio.apps.myrun.data.authentication.model.UserAccount
import android.net.Uri

// Leave a field null in order not to send an update request for that field
data class ProfileEditData(
    var displayName: String = "",
    var gender: Gender? = null,
    var height: Float? = null,
    var weight: Float? = null,
    var avatarUri: Uri? = null,
    var phoneNumber: String? = null
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

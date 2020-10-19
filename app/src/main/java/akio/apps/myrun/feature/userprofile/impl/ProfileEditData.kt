package akio.apps.myrun.feature.userprofile.impl

import akio.apps.myrun.data.userprofile.model.Gender
import android.os.Parcelable
import java.io.File

// Leave a field null in order not to send an update request for that field
data class ProfileEditData(
    var displayName: String = "",
    var gender: Gender? = null,
    var height: Float? = null,
    var weight: Float? = null,
    var avatarFile: File? = null,
    var phoneNumber: String? = null
)
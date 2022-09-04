package akio.apps.myrun.data.user.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    val accountId: String,
    val name: String = "",
    val gender: Gender = Gender.Others,
    val weight: Float = 0f,
    val photo: String? = null,
    val birthdate: Long = 0,
    val lastActiveTime: Long = 0
) : Parcelable

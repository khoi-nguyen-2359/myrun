package akio.apps.myrun.data.authentication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SignInSuccessResult(
    val isNewUser: Boolean,
    val signInMethod: SignInMethod
) : Parcelable

package akio.apps.myrun.data.authentication.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SignInSuccessResult(
    val isNewUser: Boolean,
    val signInMethod: SignInMethod
) : Parcelable

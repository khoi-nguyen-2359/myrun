package akio.apps.myrun.data.authentication.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// TODO: create another model for Parcelable
@Parcelize
data class SignInSuccessResult(
    val isNewUser: Boolean,
    val signInMethod: SignInMethod
) : Parcelable

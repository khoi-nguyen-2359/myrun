package akio.apps.myrun.feature.signin.impl

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SignInSuccessResult(
    val isNewUser: Boolean,
    val signInMethod: SignInMethod
) : Parcelable

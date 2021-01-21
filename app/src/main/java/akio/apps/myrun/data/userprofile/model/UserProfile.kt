package akio.apps.myrun.data.userprofile.model

import java.text.DecimalFormat

data class UserProfile(
    var accountId: String,
    var name: String? = null,
    var email: String? = null,
    var phone: String?,
    var gender: Gender?,
    var height: Float?,
    var weight: Float?,
    var photo: String?
) {
    fun getHeightText() = height?.let { String.format("%.0f cm", it) }
    fun getWeightText() = weight?.let {
        DecimalFormat("#.# kg").format(it)
    }

}

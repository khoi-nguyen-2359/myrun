package akio.apps.myrun.data.user.api.model

import java.text.DecimalFormat

data class UserProfile(
    val accountId: String,
    val name: String? = null,
    val email: String? = null,
    val phone: String?,
    val gender: Gender?,
    val height: Float?,
    val weight: Float?,
    val photo: String?
) {
    fun getHeightText() = height?.let { String.format("%.0f cm", it) }
    fun getWeightText() = weight?.let {
        DecimalFormat("#.# kg").format(it)
    }
}

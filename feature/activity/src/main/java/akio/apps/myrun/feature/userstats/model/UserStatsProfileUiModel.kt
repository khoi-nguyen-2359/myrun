package akio.apps.myrun.feature.userstats.model

import akio.apps.myrun.data.user.api.model.UserProfile
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserStatsProfileUiModel(
    val userProfile: UserProfile,
    val userRecentPlace: String?,
) : Parcelable {
    companion object {
        val Default: UserStatsProfileUiModel =
            UserStatsProfileUiModel(UserProfile(""), null)
    }
}

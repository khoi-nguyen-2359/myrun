package akio.apps.myrun.data.user.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserFollowCounter(
    val followingCount: Int,
    val followerCount: Int,
) : Parcelable

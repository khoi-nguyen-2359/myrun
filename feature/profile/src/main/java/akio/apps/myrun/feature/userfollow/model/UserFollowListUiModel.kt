package akio.apps.myrun.feature.userfollow.model

import akio.apps.myrun.data.user.api.model.FollowStatus
import akio.apps.myrun.data.user.api.model.UserFollow

sealed class UserFollowListUiModel(val id: String)

data class UserFollowUiModel(
    val userFollow: UserFollow,
    val isLoading: Boolean,
) : UserFollowListUiModel(userFollow.uid)

data class FollowStatusTitle(val status: FollowStatus) : UserFollowListUiModel(
    id = "${FollowStatusTitle::class.qualifiedName}-${status.name}"
)

package akio.apps.myrun.feature.userfollow.model

import akio.apps.myrun.data.user.api.model.UserFollow

sealed interface UserFollowListUiModel {
    val id: String
}

data class UserFollowUiModel(
    val userFollow: UserFollow,
    override val id: String = userFollow.uid
) : UserFollowListUiModel

object FollowStatusDivider : UserFollowListUiModel {
    override val id: String
        get() = "FollowStatusDivider"
}

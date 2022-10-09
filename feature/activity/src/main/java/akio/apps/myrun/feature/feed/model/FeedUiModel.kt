package akio.apps.myrun.feature.feed.model

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion

sealed class FeedUiModel(val id: String)

data class FeedActivity(
    val activityData: BaseActivityModel,
    val isCurrentUser: Boolean,
) : FeedUiModel(activityData.id)

data class FeedSuggestedUserFollow(
    val userFollow: UserFollowSuggestion,
    val isRequested: Boolean,
)

data class FeedUserFollowSuggestionList(val userList: List<FeedSuggestedUserFollow>) : FeedUiModel(
    id = FeedUserFollowSuggestionList::class.qualifiedName.toString()
)

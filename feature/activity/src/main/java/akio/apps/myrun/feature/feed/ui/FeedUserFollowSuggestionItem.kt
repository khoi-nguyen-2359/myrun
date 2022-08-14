package akio.apps.myrun.feature.feed.ui

import akio.apps.myrun.data.user.api.model.UserFollowSuggestion
import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.core.ui.AppColors
import akio.apps.myrun.feature.core.ui.AppDimensions
import akio.apps.myrun.feature.feed.model.FeedUserFollowSuggestion
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FeedUserFollowSuggestionItem(
    followSuggestion: FeedUserFollowSuggestion,
    onClickFollowUserId: (UserFollowSuggestion) -> Unit,
) = Column(
    modifier = Modifier.padding(vertical = ActivityFeedDimensions.feedItemVerticalPadding)
) {
    Text(
        modifier = Modifier.padding(horizontal = ActivityFeedDimensions.feedItemHorizontalPadding),
        text = stringResource(id = R.string.activity_feed_follow_suggestion_item_title),
        style = MaterialTheme.typography.caption,
        fontWeight = FontWeight.Bold
    )
    LazyRow(
        modifier = Modifier
            .padding(vertical = AppDimensions.rowVerticalPadding)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        items(
            followSuggestion.userList,
            key = { userFollow -> userFollow.uid }
        ) {
            SuggestedUserCardItem(it, onClickFollowUserId)
        }
    }
}

@Composable
private fun SuggestedUserCardItem(
    followSuggestion: UserFollowSuggestion,
    onClickFollowUser: (UserFollowSuggestion) -> Unit,
) {
    val followButtonColors = if (followSuggestion.isRequested) {
        ButtonDefaults.outlinedButtonColors(
            Color.White,
            AppColors.primary
        )
    } else {
        ButtonDefaults.outlinedButtonColors(
            AppColors.primary,
            AppColors.onPrimary
        )
    }

    val followClickAction = if (followSuggestion.isRequested) {
        null
    } else {
        onClickFollowUser
    }

    @StringRes
    val buttonTextResId = if (followSuggestion.isRequested) {
        R.string.status_requested
    } else {
        R.string.action_follow
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(6.dp),
        elevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
        ) {
            UserAvatarImage(followSuggestion.photoUrl, avatarDimension = 80.dp)
            Text(
                text = followSuggestion.displayName,
                modifier = Modifier.padding(10.dp),
                maxLines = 2,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                shape = RoundedCornerShape(3.dp),
                colors = followButtonColors,
                contentPadding = PaddingValues(0.dp),
                enabled = followClickAction != null,
                onClick = { followClickAction?.invoke(followSuggestion) },
                modifier = Modifier.heightIn(min = 30.dp).width(100.dp)
            ) {
                Text(
                    text = stringResource(buttonTextResId),
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

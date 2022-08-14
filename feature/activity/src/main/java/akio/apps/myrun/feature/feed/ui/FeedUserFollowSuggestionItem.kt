package akio.apps.myrun.feature.feed.ui

import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.core.ui.AppColors
import akio.apps.myrun.feature.core.ui.AppDimensions
import akio.apps.myrun.feature.feed.model.FeedUserFollowSuggestion
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
        modifier = Modifier.padding(vertical = AppDimensions.rowVerticalPadding).fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        items(
            followSuggestion.userList,
            key = { userFollow -> userFollow.uid }
        ) {
            Card(
                modifier = Modifier
                    .width(140.dp)
                    .padding(horizontal = 6.dp),
                elevation = 2.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(4.dp)
                ) {
                    UserAvatarImage(it.photoUrl, avatarDimension = 80.dp)
                    Text(
                        text = it.displayName,
                        modifier = Modifier.padding(10.dp),
                        maxLines = 2,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        shape = RoundedCornerShape(3.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            AppColors.primary,
                            Color.White
                        ),
                        contentPadding = PaddingValues(0.dp),
                        onClick = {
                        },
                        modifier = Modifier.heightIn(min = 30.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.action_follow),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

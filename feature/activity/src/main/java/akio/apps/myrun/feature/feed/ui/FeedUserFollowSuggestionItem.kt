package akio.apps.myrun.feature.feed.ui

import akio.apps.myrun.feature.feed.model.FeedUserFollowSuggestion
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FeedUserFollowSuggestionItem(
    feedUserFollowSuggestion: FeedUserFollowSuggestion,
) = FeedItem {
    Row {
        feedUserFollowSuggestion.userList.forEach {
            Text(text = it.displayName, modifier = Modifier.padding(10.dp))
        }
    }
}

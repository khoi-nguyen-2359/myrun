package akio.apps.myrun.feature.feed.ui

import akio.apps.myrun.feature.feed.model.FeedUserFollowSuggestion
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FeedUserFollowSuggestionItem(
    followSuggestion: FeedUserFollowSuggestion,
) = FeedItem {
    LazyRow {
        items(
            followSuggestion.userList,
            key = { userFollow -> userFollow.uid }
        ) {
            Text(text = it.displayName, modifier = Modifier.padding(10.dp))
        }
    }
}

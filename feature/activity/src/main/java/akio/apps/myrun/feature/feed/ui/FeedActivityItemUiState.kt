package akio.apps.myrun.feature.feed.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue

class FeedActivityItemUiState(
    isDetailExpanded: Boolean = false,
    isShareMenuExpanded: Boolean = false,
) {
    var isDetailExpanded: Boolean by mutableStateOf(isDetailExpanded)
    var isShareMenuExpanded: Boolean by mutableStateOf(isShareMenuExpanded)

    companion object {
        val Saver: Saver<FeedActivityItemUiState, *> = listSaver(
            save = { listOf(it.isDetailExpanded, it.isShareMenuExpanded) },
            restore = {
                FeedActivityItemUiState(
                    isDetailExpanded = it[0],
                    isShareMenuExpanded = it[1]
                )
            }
        )
    }
}

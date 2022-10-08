package akio.apps.myrun.feature.userstats.ui

import akio.apps.myrun.data.activity.api.model.ActivityType
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp

class UserStatsUiState(val contentPaddingBottom: Dp, val scrollState: ScrollState) {
    var errorMessage: String? by mutableStateOf(null)
    var selectedActivityType: ActivityType by mutableStateOf(ActivityType.Running)

    companion object {
        val Saver: Saver<UserStatsUiState, Any> = listSaver(
            save = {
                listOf(
                    it.contentPaddingBottom.value,
                    it.scrollState.value,
                    it.errorMessage,
                    it.selectedActivityType
                )
            },
            restore = {
                UserStatsUiState(
                    contentPaddingBottom = Dp(it[0] as Float),
                    scrollState = ScrollState(it[1] as Int)
                ).apply {
                    errorMessage = it[2] as String?
                    selectedActivityType = it[3] as ActivityType
                }
            }
        )
    }
}
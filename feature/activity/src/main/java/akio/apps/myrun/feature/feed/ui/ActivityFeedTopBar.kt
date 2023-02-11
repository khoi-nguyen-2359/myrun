package akio.apps.myrun.feature.feed.ui

import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.core.ui.AppBarIconButton
import akio.apps.myrun.feature.core.ui.AppDimensions
import akio.apps.myrun.feature.feed.FeedViewModel
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenu
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.sharp.CheckCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private object ActivityFeedTopBarColors {
    val uploadingBadgeContentColor = Color(0xffffffff)
}

private enum class UploadBadgeState {
    InProgress, Completed, Dismissed
}

@Composable
internal fun ActivityFeedTopBar(
    uiState: FeedUiState,
    viewModel: FeedViewModel,
    modifier: Modifier = Modifier,
    onClickUserPreferencesButton: () -> Unit,
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        TopAppBar(
            title = {
                Text(
                    stringResource(id = R.string.home_nav_activity_feed_tab_label),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            actions = {
                ActivityUploadNotifierBadge(
                    viewModel.getUploadingActivityCount(),
                    uiState::dismissActivityUploadBadge
                )
                AppBarIconButton(iconImageVector = Icons.Rounded.Settings) {
                    onClickUserPreferencesButton()
                }
            }
        )
    }
}

@Composable
private fun ActivityUploadNotifierBadge(
    activityUploadingCount: Int,
    dismissAction: () -> Unit,
) {
    var badgeState by rememberSaveable { mutableStateOf(UploadBadgeState.Dismissed) }
    badgeState = when {
        activityUploadingCount > 0 -> UploadBadgeState.InProgress
        activityUploadingCount == 0 && badgeState != UploadBadgeState.Dismissed ->
            UploadBadgeState.Completed
        else -> UploadBadgeState.Dismissed
    }
    Crossfade(targetState = badgeState) {
        when (it) {
            UploadBadgeState.InProgress -> {
                UploadInProgressBadge(count = activityUploadingCount)
            }
            UploadBadgeState.Completed -> {
                UploadCompleteBadge {
                    badgeState = UploadBadgeState.Dismissed
                    dismissAction()
                }
            }
            else -> {
                // render void
            }
        }
    }
}

@Composable
private fun UploadInProgressBadge(count: Int) {
    var isUploadInfoPopupShowing by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(AppDimensions.AppBarHeight)
            .clickable { isUploadInfoPopupShowing = true }
            .padding(AppDimensions.iconButtonPadding),
        contentAlignment = Alignment.Center
    ) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter),
            color = ActivityFeedTopBarColors.uploadingBadgeContentColor
        )
        Text(
            text = "$count",
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            color = ActivityFeedTopBarColors.uploadingBadgeContentColor
        )
        DropdownMenu(
            modifier = Modifier
                .background(Color.Black)
                .padding(horizontal = 8.dp),
            expanded = isUploadInfoPopupShowing,
            onDismissRequest = { isUploadInfoPopupShowing = false }
        ) {
            val popupMessage = stringResource(R.string.home_upload_progress, count)
            Text(
                text = popupMessage,
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun UploadCompleteBadge(onClickUploadCompleteBadge: () -> Unit) {
    var isDismissed by remember { mutableStateOf(false) }
    if (isDismissed) {
        return
    }
    AppBarIconButton(
        iconImageVector = Icons.Sharp.CheckCircleOutline
    ) {
        onClickUploadCompleteBadge()
        isDismissed = true
    }
}

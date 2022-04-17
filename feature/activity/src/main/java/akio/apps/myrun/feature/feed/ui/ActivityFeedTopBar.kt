package akio.apps.myrun.feature.feed.ui

import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.core.ui.AppBarIconButton
import akio.apps.myrun.feature.core.ui.AppDimensions
import akio.apps.myrun.feature.feed.ActivityFeedViewModel
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
import androidx.compose.material.icons.sharp.CheckCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

@Composable
internal fun ActivityFeedTopBar(
    activityUploadBadge: ActivityFeedViewModel.ActivityUploadBadgeStatus?,
    onClickUploadCompleteBadge: () -> Unit,
    modifier: Modifier = Modifier,
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
                ActivityUploadNotifierBadge(activityUploadBadge, onClickUploadCompleteBadge)
            }
        )
    }
}

@Composable
private fun ActivityUploadNotifierBadge(
    activityUploadBadge: ActivityFeedViewModel.ActivityUploadBadgeStatus?,
    onClickUploadCompleteBadge: () -> Unit,
) {
    Crossfade(targetState = activityUploadBadge) {
        when (it) {
            is ActivityFeedViewModel.ActivityUploadBadgeStatus.InProgress -> {
                UploadInProgressBadge(count = it.activityCount)
            }
            ActivityFeedViewModel.ActivityUploadBadgeStatus.Complete -> {
                UploadCompleteBadge(onClickUploadCompleteBadge)
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
            onDismissRequest = { isUploadInfoPopupShowing = false },
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

package akio.apps.myrun.feature.home.ui

import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.ui.UserTimeline
import akio.apps.myrun.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BottomAppBarHeight = 56.dp

@Composable
fun HomeScreen(
    userTimelineViewModel: UserTimelineViewModel,
    onClickUserProfileButton: () -> Unit,
    onClickFloatingActionButton: () -> Unit,
    onClickActivityItemAction: (Activity) -> Unit,
    onClickActivityFileAction: (Activity) -> Unit,
    onClickUserAvatar: (String) -> Unit
) {
    AppTheme {
        Scaffold(
            bottomBar = { HomeBottomBar(onClickUserProfileButton) },
            floatingActionButton = { HomeFloatingActionButton(onClickFloatingActionButton) },
            floatingActionButtonPosition = FabPosition.Center,
            isFloatingActionButtonDocked = true
        ) {
            Box {
                UserTimeline(
                    userTimelineViewModel,
                    PaddingValues(
                        bottom = BottomAppBarHeight * 1.5f // avoid the bottom bar
                    ),
                    onClickActivityItemAction,
                    onClickActivityFileAction,
                    onClickUserAvatar
                )
            }
        }
    }
}

@Composable
private fun UploadingNotifierItem(activityStorageCount: Int) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .background(Color.White)
) {
    Text(
        text = "Uploading $activityStorageCount activities.",
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
        fontSize = 12.sp,
        textAlign = TextAlign.Center
    )
    LinearProgressIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
    )
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewUploadingNotifierItem() {
    UploadingNotifierItem(activityStorageCount = 3)
}

@Composable
private fun HomeFloatingActionButton(onClick: () -> Unit) = FloatingActionButton(
    onClick = onClick
) {
    Icon(
        imageVector = Icons.Rounded.Add,
        contentDescription = "Floating action button on bottom bar"
    )
}

@Composable
private fun HomeBottomBar(
    onClickUserProfileButton: () -> Unit
) = BottomAppBar(
    elevation = 2.dp
) {
    Spacer(modifier = Modifier.weight(weight = 1f, fill = true))
    BottomBarIconButton(onClickUserProfileButton, Icons.Rounded.Settings)
}

@Composable
private fun BottomBarIconButton(onClick: () -> Unit, iconImageVector: ImageVector) = IconButton(
    onClick = onClick,
) {
    Icon(
        imageVector = iconImageVector,
        contentDescription = "User profile icon button on bottom bar"
    )
}

@Preview
@Composable
private fun PreviewBottomBar() = HomeBottomBar { }

package akio.apps.myrun.feature.home.ui

import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.ui.UserTimeline
import akio.apps.myrun.ui.theme.MyRunAppTheme
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
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BottomAppBarHeight = 56.dp

@Composable
fun HomeScreen(
    userTimelineViewModel: UserTimelineViewModel,
    onClickUserProfileButton: () -> Unit,
    onClickFloatingActionButton: () -> Unit,
    onClickActivityItemAction: (Activity) -> Unit,
    onClickActivityFileAction: (Activity) -> Unit
) = MyRunAppTheme {
    val activityStorageCount by userTimelineViewModel.activityStorageCount
        .collectAsState(initial = 0)
    Scaffold(
        topBar = { if (activityStorageCount > 0) HomeTopBar(activityStorageCount) else { Unit } },
        bottomBar = { HomeBottomBar(onClickUserProfileButton) },
        floatingActionButton = { HomeFloatingActionButton(onClickFloatingActionButton) },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true
    ) {
        UserTimeline(
            userTimelineViewModel,
            PaddingValues(bottom = BottomAppBarHeight * 1.5f),
            onClickActivityItemAction,
            onClickActivityFileAction
        )
    }
}

@Composable
fun HomeTopBar(activityStorageCount: Int) {
    UploadingNotifierItem(activityStorageCount)
}

@Composable
private fun UploadingNotifierItem(activityStorageCount: Int) = Column(
    modifier = Modifier.fillMaxWidth()
) {
    Text(
        text = "Uploading $activityStorageCount activities.",
        modifier = Modifier.fillMaxWidth().padding(3.dp),
        fontSize = 12.sp,
        textAlign = TextAlign.Center
    )
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
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
) = BottomAppBar {
    Spacer(modifier = Modifier.weight(weight = 1f, fill = true))
    BottomBarIconButton(onClickUserProfileButton, Icons.Rounded.Person)
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

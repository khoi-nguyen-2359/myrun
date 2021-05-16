package akio.apps.myrun.feature.home.ui

import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.ui.UserTimelineList
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun HomeScreen(
    userTimelineViewModel: UserTimelineViewModel,
    onClickUserProfileButton: () -> Unit,
    onClickFloatingActionButton: () -> Unit,
    onClickActivityItemAction: (Activity) -> Unit
) = MaterialTheme {
    Scaffold(
        bottomBar = { HomeBottomBar(onClickUserProfileButton) },
        floatingActionButton = { HomeFloatingActionButton(onClickFloatingActionButton) },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true
    ) {
        UserTimelineList(userTimelineViewModel, onClickActivityItemAction)
    }
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

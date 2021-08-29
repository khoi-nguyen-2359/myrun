package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.common.data.Resource
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import akio.apps.myrun.feature.activitydetail.ActivityRouteMapActivity
import akio.apps.myrun.feature.activitydetail.R
import akio.apps.myrun.feature.base.navigation.HomeNavigationDestination
import akio.apps.myrun.feature.base.ui.AppTheme
import akio.apps.myrun.feature.base.ui.CentralAnnouncementView
import akio.apps.myrun.feature.base.ui.CentralLoadingView
import akio.apps.myrun.feature.base.ui.NavigationBarSpacer
import akio.apps.myrun.feature.base.ui.StatusBarSpacer
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController

private sealed class ActivityDetailScreenState {
    object FullScreenLoading : ActivityDetailScreenState()
    object ErrorAndRetry : ActivityDetailScreenState()
    object UnknownState : ActivityDetailScreenState()

    class DataAvailable(
        val activityData: ActivityModel,
        val activityPlaceName: String?,
        val isStillLoading: Boolean,
    ) : ActivityDetailScreenState()

    companion object {
        fun create(
            activityDetailResource: Resource<ActivityModel>,
            activityPlaceName: String?,
        ): ActivityDetailScreenState {
            val activityData = activityDetailResource.data
            return when {
                activityData == null && activityDetailResource is Resource.Loading ->
                    FullScreenLoading
                activityData == null && activityDetailResource is Resource.Error ->
                    ErrorAndRetry
                activityData != null -> {
                    DataAvailable(
                        activityData,
                        activityPlaceName,
                        isStillLoading = activityDetailResource is Resource.Loading
                    )
                }
                else -> UnknownState
            }
        }
    }
}

@Composable
fun ActivityDetailScreen(
    activityDetailViewModel: ActivityDetailViewModel,
    onClickExportFile: (ActivityModel) -> Unit,
    navController: NavController,
) = AppTheme {
    val activityResource by activityDetailViewModel.activityDetails
        .collectAsState(Resource.Loading())
    val activityDisplayPlaceName by activityDetailViewModel.activityPlaceName
        .collectAsState(initial = null)
    val screenState = ActivityDetailScreenState.create(activityResource, activityDisplayPlaceName)
    ActivityDetailScreen(
        screenState,
        navController,
        onClickExportFile
    ) {
        activityDetailViewModel.loadActivityDetails()
    }
}

@Composable
private fun ActivityDetailScreen(
    screenState: ActivityDetailScreenState,
    navController: NavController,
    onClickExportFile: (ActivityModel) -> Unit,
    onActivityDetailLoadRetry: () -> Unit,
) {
    Column {
        StatusBarSpacer()
        ActivityDetailTopBar(
            screenState,
            { navController.popBackStack() },
            onClickExportFile
        )
        when (screenState) {
            is ActivityDetailScreenState.FullScreenLoading -> {
                CentralLoadingView(
                    text = stringResource(id = R.string.activity_details_loading_message)
                )
            }
            is ActivityDetailScreenState.ErrorAndRetry -> {
                CentralAnnouncementView(
                    text = stringResource(id = R.string.activity_details_loading_error)
                ) {
                    onActivityDetailLoadRetry()
                }
            }
            is ActivityDetailScreenState.DataAvailable -> {
                ActivityDetailDataContainer(
                    screenState,
                    navController,
                    modifier = Modifier.weight(1f)
                )
            }
            ActivityDetailScreenState.UnknownState -> {
            }
        }
        NavigationBarSpacer()
    }
}

@Composable
private fun ActivityDetailDataContainer(
    screenState: ActivityDetailScreenState.DataAvailable,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(modifier) {
        ActivityInfoHeaderView(
            screenState.activityData,
            screenState.activityPlaceName
        ) {
            navController.navigateToProfile(screenState.activityData.athleteInfo.userId)
        }
        ActivityRouteImage(screenState.activityData) {
            navigateToActivityMap(context, screenState.activityData.encodedPolyline)
        }
        PerformanceTableComposable(screenState.activityData)
        if (screenState.isStillLoading) {
            BottomLoadingIndicator()
        }
    }
}

fun navigateToActivityMap(context: Context, encodedPolyline: String) {
    val intent = ActivityRouteMapActivity.createLaunchIntent(context, encodedPolyline)
    context.startActivity(intent)
}

private fun NavController.navigateToProfile(userId: String) {
    navigate(HomeNavigationDestination.Profile.routeWithUserId(userId))
}

@Composable
private fun ActivityDetailTopBar(
    screenState: ActivityDetailScreenState,
    onClickBackButton: () -> Unit,
    onClickExportFile: (ActivityModel) -> Unit,
) {
    val topBarTitle =
        (screenState as? ActivityDetailScreenState.DataAvailable)?.activityData?.name ?: ""
    TopAppBar(
        title = { Text(text = topBarTitle) },
        actions = {
            if (screenState is ActivityDetailScreenState.DataAvailable) {
                ShareActionMenu { onClickExportFile(screenState.activityData) }
            }
        },
        navigationIcon = {
            IconButton(onClick = onClickBackButton) {
                Icon(imageVector = Icons.Sharp.ArrowBack, contentDescription = "Back button")
            }
        }
    )
}

@Composable
private fun ShareActionMenu(onClickExportFile: () -> Unit) {
    Box {
        var isExpanded by remember { mutableStateOf(false) }
        IconButton(
            onClick = {
                isExpanded = !isExpanded
            }
        ) {
            Icon(
                imageVector = Icons.Sharp.Share,
                contentDescription = "Share action menu"
            )
        }
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    onClickExportFile()
                    isExpanded = false
                }
            ) {
                Text(
                    text = stringResource(
                        id = R.string.activity_details_share_menu_item_export_file
                    )
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.BottomLoadingIndicator() = Box(
    modifier = Modifier.weight(1.0f),
    contentAlignment = Alignment.BottomCenter
) {
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun LoadingErrorSnackbar(snackbarData: SnackbarData) {
    val snackbarBackgroundColor = MaterialTheme.colors.error
    val snackbarContentColor = contentColorFor(backgroundColor = snackbarBackgroundColor)
    Snackbar(
        backgroundColor = snackbarBackgroundColor,
        contentColor = snackbarContentColor,
        actionColor = snackbarContentColor,
        snackbarData = snackbarData
    )
}

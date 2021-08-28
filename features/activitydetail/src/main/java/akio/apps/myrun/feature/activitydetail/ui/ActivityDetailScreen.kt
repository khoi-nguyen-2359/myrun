package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.common.data.Resource
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import akio.apps.myrun.feature.activitydetail.ActivityRouteMapActivity
import akio.apps.myrun.feature.activitydetail.R
import akio.apps.myrun.feature.base.navigation.HomeNavigationDestination
import akio.apps.myrun.feature.base.ui.AppTheme
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
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Share
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun ActivityDetailScreen(
    activityDetailViewModel: ActivityDetailViewModel,
    onClickExportFile: (ActivityModel) -> Unit,
    navController: NavController,
) = AppTheme {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val activityResource by activityDetailViewModel.activityDetails
        .collectAsState(Resource.Loading())
    val activityDisplayPlaceName by activityDetailViewModel.activityPlaceName
        .collectAsState(initial = null)
    val activityDetail = activityResource.data
    val activityName = activityDetail?.name
    val context = LocalContext.current
    Column {
        StatusBarSpacer()
        Scaffold(
            modifier = Modifier.weight(1f),
            topBar = {
                ActivityDetailTopBar(
                    activityName,
                    { navController.popBackStack() },
                    { if (activityDetail != null) onClickExportFile(activityDetail) }
                )
            },
            scaffoldState = scaffoldState,
            snackbarHost = { hostState ->
                SnackbarHost(hostState) { snackbarData ->
                    LoadingErrorSnackbar(snackbarData)
                }
            }
        ) {
            if (activityDetail != null) {
                Column {
                    ActivityInfoHeaderView(
                        activityDetail,
                        activityDisplayPlaceName
                    ) { navController.navigateToProfile(activityDetail.athleteInfo.userId) }
                    ActivityRouteImage(activityDetail) {
                        navigateToActivityMap(context, activityDetail.encodedPolyline)
                    }
                    PerformanceTableComposable(activityDetail)
                    if (activityResource is Resource.Loading) {
                        BottomLoadingIndicator()
                    }
                }
            } else if (activityResource is Resource.Loading) {
                CentralLoadingView(
                    text = stringResource(id = R.string.activity_details_loading_message)
                )
            }

            if (activityResource is Resource.Error) {
                val errorMessage = stringResource(id = R.string.activity_details_loading_error)
                val retryLabel = stringResource(id = R.string.action_retry)
                coroutineScope.launch {
                    val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                        message = errorMessage,
                        actionLabel = retryLabel,
                        duration = SnackbarDuration.Long
                    )

                    if (snackbarResult == SnackbarResult.ActionPerformed) {
                        activityDetailViewModel.loadActivityDetails()
                    }
                }
            }
        }
        NavigationBarSpacer()
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
    activityName: String?,
    onClickBackButton: () -> Unit,
    onClickExportFile: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = activityName ?: "") },
        actions = {
            if (activityName != null) {
                ShareActionMenu(onClickExportFile)
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

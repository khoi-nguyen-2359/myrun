package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import akio.apps.myrun.feature.activitydetail.ActivityRouteMapActivity
import akio.apps.myrun.feature.base.TrackingValueFormatter
import akio.apps.myrun.feature.base.navigation.HomeNavDestination
import akio.apps.myrun.feature.base.ui.AppColors
import akio.apps.myrun.feature.base.ui.AppDimensions
import akio.apps.myrun.feature.base.ui.AppTheme
import akio.apps.myrun.feature.base.ui.CentralAnnouncementView
import akio.apps.myrun.feature.base.ui.CentralLoadingView
import akio.apps.myrun.feature.base.ui.NavigationBarSpacer
import akio.apps.myrun.feature.base.ui.StatusBarSpacer
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ActivityDetailScreen(
    activityDetailViewModel: ActivityDetailViewModel,
    onClickExportFile: (ActivityModel) -> Unit,
    navController: NavController,
) = AppTheme {
    val screenState by activityDetailViewModel.activityDetailScreenStateFlow.collectAsState(
        initial = ActivityDetailViewModel.ActivityDetailScreenState.FullScreenLoading
    )
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
    screenState: ActivityDetailViewModel.ActivityDetailScreenState,
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
            is ActivityDetailViewModel.ActivityDetailScreenState.FullScreenLoading -> {
                CentralLoadingView(
                    text = stringResource(id = R.string.activity_details_loading_message)
                )
            }
            is ActivityDetailViewModel.ActivityDetailScreenState.ErrorAndRetry -> {
                CentralAnnouncementView(
                    text = stringResource(id = R.string.activity_details_loading_error)
                ) {
                    onActivityDetailLoadRetry()
                }
            }
            is ActivityDetailViewModel.ActivityDetailScreenState.DataAvailable -> {
                ActivityDetailDataContainer(
                    screenState,
                    navController,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White)
                )
            }
            ActivityDetailViewModel.ActivityDetailScreenState.UnknownState -> {
            }
        }
        NavigationBarSpacer()
    }
}

@Composable
private fun ActivityDetailDataContainer(
    screenState: ActivityDetailViewModel.ActivityDetailScreenState.DataAvailable,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Box(modifier = modifier) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
            if (screenState.runSplits.isNotEmpty()) {
                RunSplitsTable(
                    screenState.runSplits,
                    screenState.activityData.distance,
                    modifier = Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)
                )
            }
        }

        if (screenState.isStillLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun RunSplitsTable(
    runSplits: List<Double>,
    totalDistance: Double,
    modifier: Modifier = Modifier,
) {
    val fastestPace = runSplits.minOrNull() ?: return
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(AppDimensions.sectionVerticalSpacing))
        Text(
            text = stringResource(id = R.string.activity_details_run_splits_caption),
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(AppDimensions.rowVerticalPadding))
        val kmColumnWeight = 1.5f
        val paceColumnWeight = 2f
        val progressColumnWeight = 10f
        Row {
            Text(
                text = stringResource(id = R.string.activity_detail_split_km_column).uppercase(),
                modifier = Modifier.weight(kmColumnWeight),
                style = MaterialTheme.typography.overline,
                fontWeight = FontWeight.Bold,
                color = Color.Gray.copy(alpha = 0.5f)
            )
            Text(
                text = stringResource(id = R.string.activity_detail_split_pace_column).uppercase(),
                modifier = Modifier.weight(paceColumnWeight),
                style = MaterialTheme.typography.overline,
                fontWeight = FontWeight.Bold,
                color = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.weight(progressColumnWeight))
        }
        Divider(
            color = Color.Gray.copy(alpha = 0.5f),
            thickness = 0.5.dp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        runSplits.forEachIndexed { index, splitPace ->
            Row(
                modifier = Modifier.padding(vertical = 1.dp)
            ) {
                val kmLabel = if (index == runSplits.size - 1) {
                    val rounded = String.format("%.1f", (totalDistance % 1000) / 1000)
                    if (rounded == "1.0") {
                        "${index + 1}"
                    } else {
                        rounded
                    }
                } else {
                    (index + 1).toString()
                }
                Text(
                    text = kmLabel, modifier = Modifier.weight(kmColumnWeight),
                    style = MaterialTheme.typography.caption
                )
                Text(
                    text = TrackingValueFormatter.PaceMinutePerKm.getFormattedValue(splitPace),
                    modifier = Modifier.weight(paceColumnWeight),
                    style = MaterialTheme.typography.caption
                )
                LinearProgressIndicator(
                    progress = (fastestPace / splitPace).toFloat(),
                    modifier = Modifier
                        .weight(progressColumnWeight)
                        .height(14.dp)
                        .align(Alignment.CenterVertically),
                    color = AppColors.secondary,
                    backgroundColor = Color.Transparent
                )
            }
        }
        Spacer(modifier = Modifier.height(AppDimensions.screenVerticalSpacing))
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
fun PreviewRunSplitsTable() = RunSplitsTable(
    runSplits = listOf(6.4, 6.15, 6.0, 5.8, 5.6, 5.5, 7.0),
    6700.0,
)

fun navigateToActivityMap(context: Context, encodedPolyline: String) {
    val intent = ActivityRouteMapActivity.createLaunchIntent(context, encodedPolyline)
    context.startActivity(intent)
}

private fun NavController.navigateToProfile(userId: String) {
    navigate(HomeNavDestination.Profile.routeWithUserId(userId))
}

@Composable
private fun ActivityDetailTopBar(
    screenState: ActivityDetailViewModel.ActivityDetailScreenState,
    onClickBackButton: () -> Unit,
    onClickExportFile: (ActivityModel) -> Unit,
) {
    val topBarTitle =
        (screenState as? ActivityDetailViewModel.ActivityDetailScreenState.DataAvailable)
            ?.activityData
            ?.name
            ?: ""
    TopAppBar(
        title = { Text(text = topBarTitle) },
        actions = {
            if (screenState is ActivityDetailViewModel.ActivityDetailScreenState.DataAvailable) {
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
                        id = R.string.activity_share_menu_item_export_file
                    )
                )
            }
        }
    }
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
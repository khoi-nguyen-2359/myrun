package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import akio.apps.myrun.feature.activitydetail.ActivityRouteMapActivity
import akio.apps.myrun.feature.activitydetail.di.DaggerActivityDetailFeatureComponent
import akio.apps.myrun.feature.core.measurement.TrackUnitFormatter
import akio.apps.myrun.feature.core.measurement.TrackUnitFormatterSet
import akio.apps.myrun.feature.core.measurement.UnitFormatterSetFactory
import akio.apps.myrun.feature.core.navigation.HomeNavDestination
import akio.apps.myrun.feature.core.ui.AppColors
import akio.apps.myrun.feature.core.ui.AppDimensions
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.core.ui.CentralAnnouncementView
import akio.apps.myrun.feature.core.ui.CentralLoadingView
import akio.apps.myrun.feature.core.ui.NavigationBarSpacer
import akio.apps.myrun.feature.core.ui.StatusBarSpacer
import android.app.Application
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
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Composable
fun ActivityDetailScreen(
    navController: NavController,
    navBackStackEntry: NavBackStackEntry,
    onClickExportFile: (BaseActivityModel) -> Unit,
) {
    val activityDetailViewModel = rememberViewModel(navBackStackEntry)
    ActivityDetailScreen(activityDetailViewModel, onClickExportFile, navController)
}

@Composable
private fun rememberViewModel(navEntry: NavBackStackEntry): ActivityDetailViewModel {
    val application = LocalContext.current.applicationContext as Application
    val activityId =
        HomeNavDestination.ActivityDetail.activityIdRequiredArg.parseValueInBackStackEntry(navEntry)
    return remember {
        DaggerActivityDetailFeatureComponent.factory()
            .create(
                application,
                ActivityDetailViewModel.setInitialSavedState(navEntry.savedStateHandle, activityId)
            )
            .activityDetailsViewModel()
    }
}

@Composable
private fun ActivityDetailScreen(
    activityDetailViewModel: ActivityDetailViewModel,
    onClickExportFile: (BaseActivityModel) -> Unit,
    navController: NavController,
) = AppTheme {
    val screenState by activityDetailViewModel.screenStateFlow.collectAsState(
        initial = ActivityDetailViewModel.ScreenState.FullScreenLoading
    )
    ActivityDetailScreen(
        screenState,
        navController,
        onClickExportFile
    ) {
        activityDetailViewModel.refreshActivityDetails()
    }
}

@Composable
private fun ActivityDetailScreen(
    screenState: ActivityDetailViewModel.ScreenState,
    navController: NavController,
    onClickExportFile: (BaseActivityModel) -> Unit,
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
            is ActivityDetailViewModel.ScreenState.FullScreenLoading -> {
                CentralLoadingView(
                    text = stringResource(id = R.string.activity_details_loading_message)
                )
            }
            is ActivityDetailViewModel.ScreenState.ErrorAndRetry -> {
                CentralAnnouncementView(
                    text = stringResource(id = R.string.activity_details_loading_error)
                ) {
                    onActivityDetailLoadRetry()
                }
            }
            is ActivityDetailViewModel.ScreenState.DataAvailable -> {
                ActivityDetailDataContainer(
                    screenState,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White)
                )
            }
            ActivityDetailViewModel.ScreenState.UnknownState -> {
            }
        }
        NavigationBarSpacer()
    }
}

@Composable
private fun ActivityDetailDataContainer(
    screenState: ActivityDetailViewModel.ScreenState.DataAvailable,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val trackValueFormatterPreference = remember(screenState.preferredSystem) {
        UnitFormatterSetFactory.createUnitFormatterSet(screenState.preferredSystem)
    }
    Box(modifier = modifier) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            ActivityInfoHeaderView(
                screenState.activityData,
                screenState.activityFormattedStartTime,
                screenState.activityPlaceName
            )
            if (screenState.isMapVisible) {
                ActivityRouteImage(screenState.activityData) {
                    navigateToActivityMap(context, screenState.activityData.encodedPolyline)
                }
            }
            WidePerformanceTableComposable(screenState.activityData, trackValueFormatterPreference)
            if (screenState.runSplits.isNotEmpty()) {
                RunSplitsTable(
                    screenState.runSplits,
                    screenState.activityData.distance,
                    trackValueFormatterPreference,
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
    trackUnitFormatterSet: TrackUnitFormatterSet,
    modifier: Modifier = Modifier,
) {
    val fastestPace = runSplits.minOrNull()
        ?: return
    val (distanceFormatter, paceFormatter, _, _) = trackUnitFormatterSet
    val context = LocalContext.current
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
                text = distanceFormatter.getUnit(context).uppercase(),
                modifier = Modifier.weight(kmColumnWeight),
                style = MaterialTheme.typography.overline,
                fontWeight = FontWeight.Bold,
                color = Color.Gray.copy(alpha = 0.5f)
            )
            Text(
                text = stringResource(paceFormatter.labelResId).uppercase(),
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
            Row(modifier = Modifier.padding(vertical = 1.dp)) {
                val distanceLabel =
                    formatDistanceLabel(index, runSplits, distanceFormatter, totalDistance)
                Text(
                    text = distanceLabel,
                    modifier = Modifier.weight(kmColumnWeight),
                    style = MaterialTheme.typography.caption
                )
                Text(
                    text = paceFormatter.getFormattedValue(splitPace),
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

private fun formatDistanceLabel(
    index: Int,
    runSplits: List<Double>,
    distanceFormatter: TrackUnitFormatter.DistanceUnitFormatter,
    totalDistance: Double,
) = if (index == runSplits.size - 1) {
    val converted = distanceFormatter.converter.fromRawValue(totalDistance)
    val rounded = String.format("%.1f", converted - converted.toInt())
    if (rounded == "1.0") {
        "${index + 1}"
    } else {
        rounded
    }
} else {
    (index + 1).toString()
}

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
fun PreviewRunSplitsTable() = RunSplitsTable(
    runSplits = listOf(6.4, 6.15, 6.0, 5.8, 5.6, 5.5, 7.0),
    6700.0,
    UnitFormatterSetFactory.createUnitFormatterSet(MeasureSystem.Default)
)

fun navigateToActivityMap(context: Context, encodedPolyline: String) {
    val intent = ActivityRouteMapActivity.createLaunchIntent(context, encodedPolyline)
    context.startActivity(intent)
}

@Composable
private fun ActivityDetailTopBar(
    screenState: ActivityDetailViewModel.ScreenState,
    onClickBackButton: () -> Unit,
    onClickExportFile: (BaseActivityModel) -> Unit,
) {
    val topBarTitle =
        (screenState as? ActivityDetailViewModel.ScreenState.DataAvailable)
            ?.activityData
            ?.name
            ?: ""
    TopAppBar(
        title = { Text(text = topBarTitle) },
        actions = {
            if (screenState is ActivityDetailViewModel.ScreenState.DataAvailable) {
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

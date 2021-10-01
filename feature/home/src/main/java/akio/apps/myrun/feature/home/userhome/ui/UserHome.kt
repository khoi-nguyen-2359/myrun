package akio.apps.myrun.feature.home.userhome.ui

import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.domain.TrackingValueConverter
import akio.apps.myrun.domain.activity.GetTrainingSummaryDataUsecase
import akio.apps.myrun.feature.base.navigation.HomeNavDestination
import akio.apps.myrun.feature.base.ui.AppColors
import akio.apps.myrun.feature.base.ui.AppDimensions
import akio.apps.myrun.feature.base.ui.CentralLoadingView
import akio.apps.myrun.feature.base.ui.ColumnSpacer
import akio.apps.myrun.feature.base.ui.RowSpacer
import akio.apps.myrun.feature.base.ui.StatusBarSpacer
import akio.apps.myrun.feature.home.R
import akio.apps.myrun.feature.home.userhome.UserHomeViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.size.Scale

@Composable
fun UserHome(
    userHomeViewModel: UserHomeViewModel,
    contentPadding: PaddingValues,
    appNavController: NavController,
) {
    val screenState by userHomeViewModel.screenState.collectAsState(initial = null)
    UserHome(screenState ?: return, contentPadding, appNavController)
}

@Composable
private fun UserHome(
    screenState: UserHomeViewModel.ScreenState = UserHomeViewModel.ScreenState.StatsLoading,
    contentPadding: PaddingValues,
    appNavController: NavController,
) {
    Column {
        StatusBarSpacer()
        UserHomeTopBar()
        when (screenState) {
            UserHomeViewModel.ScreenState.StatsLoading -> {
                CentralLoadingView(text = stringResource(id = R.string.message_loading))
            }
            is UserHomeViewModel.ScreenState.StatsAvailable -> {
                UserHomeContent(screenState, modifier = Modifier.weight(1f), appNavController)
            }
        }
        Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding()))
    }
}

@Composable
fun UserHomeContent(
    screenState: UserHomeViewModel.ScreenState.StatsAvailable,
    modifier: Modifier = Modifier,
    appNavController: NavController,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        ColumnSpacer(height = AppDimensions.screenVerticalSpacing)
        ColumnSpacer(height = AppDimensions.sectionVerticalSpacing)
        UserProfileHeader(screenState, appNavController)
        ColumnSpacer(height = AppDimensions.sectionVerticalSpacing * 2)
        TrainingSummaryTable(screenState)
    }
}

@Composable
fun TrainingSummaryTable(screenState: UserHomeViewModel.ScreenState.StatsAvailable) {
    var selectedActivityType by rememberSaveable { mutableStateOf(ActivityType.Running) }
    val summaryData = screenState.trainingSummaryTableData[selectedActivityType] ?: return
    val thisWeekDistance = TrackingValueConverter.DistanceKm.fromRawValue(
        summaryData.thisWeekSummary.distance
    )
    val lastWeekDistance = TrackingValueConverter.DistanceKm.fromRawValue(
        summaryData.lastWeekSummary.distance
    )
    val thisWeekTime = TrackingValueConverter.TimeHour.fromRawValue(
        summaryData.thisWeekSummary.time
    )
    val lastWeekTime = TrackingValueConverter.TimeHour.fromRawValue(
        summaryData.lastWeekSummary.time
    )
    val thisMonthDistance = TrackingValueConverter.DistanceKm.fromRawValue(
        summaryData.thisMonthSummary.distance
    )
    val lastMonthDistance = TrackingValueConverter.DistanceKm.fromRawValue(
        summaryData.lastMonthSummary.distance
    )
    val thisMonthTime = TrackingValueConverter.TimeHour.fromRawValue(
        summaryData.thisMonthSummary.time
    )
    val lastMonthTime = TrackingValueConverter.TimeHour.fromRawValue(
        summaryData.lastMonthSummary.time
    )

    Column {
        ActivityTypePane(selectedActivityType) { selectedActivityType = it }
        ColumnSpacer(height = AppDimensions.rowVerticalPadding)
        Column(modifier = Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
            TableRow {
                TrainingSummaryLabelCell(text = "\n")
                TrainingSummaryLabelCell(stringResource(R.string.user_home_summary_weekly_label))
                TrainingSummaryLabelCell(stringResource(R.string.user_home_summary_monthly_label))
            }
            TableDivider()
            TableRow {
                TableCell {
                    TrainingSummaryLabel(
                        stringResource(id = R.string.user_home_summary_distance_label),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = " (km)",
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                TrainingSummaryProgress(
                    current = String.format("%.1f", thisWeekDistance),
                    previous = String.format("%.1f", lastWeekDistance)
                )
                TrainingSummaryProgress(
                    current = String.format("%.1f", thisMonthDistance),
                    previous = String.format("%.1f", lastMonthDistance)
                )
            }
            TableDivider()
            TableRow {
                TableCell {
                    TrainingSummaryLabel(
                        stringResource(id = R.string.user_home_summary_total_time_label),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = " (hour)",
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                TrainingSummaryProgress(
                    current = String.format("%.1f", thisWeekTime),
                    previous = String.format("%.1f", lastWeekTime)
                )
                TrainingSummaryProgress(
                    current = String.format("%.1f", thisMonthTime),
                    previous = String.format("%.1f", lastMonthTime)
                )
            }
            TableDivider()
            TableRow {
                TrainingSummaryLabelCell(
                    stringResource(id = R.string.user_home_summary_activities_label),
                    TextAlign.Start
                )
                TrainingSummaryProgress(
                    current = "${summaryData.thisWeekSummary.activityCount}",
                    previous = "${summaryData.lastWeekSummary.activityCount}"
                )
                TrainingSummaryProgress(
                    current = "${summaryData.thisMonthSummary.activityCount}",
                    previous = "${summaryData.lastMonthSummary.activityCount}"
                )
            }
            TableDivider()
        }
        ColumnSpacer(height = AppDimensions.sectionVerticalSpacing)
    }
}

@Composable
private fun ActivityTypePane(
    selectedActivityType: ActivityType,
    selectActivityTypeAction: (ActivityType) -> Unit,
) {
    val allTypes = listOf(ActivityType.Running, ActivityType.Cycling)
    Row(modifier = Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
        allTypes.forEach { type ->
            val backgroundColor: Color
            val contentColor: Color
            if (type == selectedActivityType) {
                backgroundColor = AppColors.primary
                contentColor = Color.White
            } else {
                backgroundColor = Color.White
                contentColor = AppColors.primary
            }
            val activityTypeLabel = when (type) {
                ActivityType.Running -> R.string.user_home_training_summary_run_activity_type
                ActivityType.Cycling -> R.string.user_home_training_summary_ride_activity_type
                else -> 0
            }
            UserHomeOutlinedButton(
                text = stringResource(id = activityTypeLabel),
                onClick = {
                    selectActivityTypeAction(type)
                },
                modifier = Modifier.padding(end = 12.dp),
                colors = ButtonDefaults.outlinedButtonColors(backgroundColor, contentColor),
                width = 65.dp
            )
        }
    }
}

@Composable
private fun TableDivider() = Divider(thickness = 0.5.dp)

@Composable
private fun SectionTitle(text: String) {
    Text(
        modifier = Modifier.padding(start = AppDimensions.screenHorizontalPadding),
        text = text,
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun RowScope.TableCell(
    modifier: Modifier = Modifier,
    content: @Composable (RowScope.() -> Unit),
) = Row(
    modifier = modifier
        .weight(1f)
        .padding(vertical = 12.dp)
        .alignByBaseline(),
    content = content,
//    verticalAlignment = Alignment.Bottom,
)

@Composable
private fun TableRow(content: @Composable (RowScope.() -> Unit)) = Row(
    modifier = Modifier.fillMaxWidth(),
    content = content,
    verticalAlignment = Alignment.CenterVertically
)

@Composable
private fun RowScope.TrainingSummaryLabelCell(
    text: String,
    textAlign: TextAlign = TextAlign.Center,
) = TableCell {
    TrainingSummaryLabel(text, Modifier.fillMaxWidth(), textAlign)
}

@Composable
private fun RowScope.TrainingSummaryLabel(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.subtitle1,
        fontWeight = FontWeight.Bold,
//        fontSize = 16.sp,
        textAlign = textAlign,
        modifier = modifier.alignByBaseline()
    )
}

@Composable
private fun RowScope.TrainingSummaryProgress(
    current: String,
    previous: String,
) = TableCell {
    Text(
        text = current,
        style = MaterialTheme.typography.subtitle1,
//        fontSize = 15.sp,
        modifier = Modifier
            .weight(1f)
            .alignByBaseline(),
        textAlign = TextAlign.End,
        fontWeight = FontWeight.Bold,
//        color = AppColors.primary
    )
    Text(
        text = " / ",
        modifier = Modifier.alignByBaseline(),
        fontSize = 14.sp
    )
    Text(
        text = previous,
        style = MaterialTheme.typography.caption,
//        fontSize = 13.sp,
        modifier = Modifier
            .weight(1f)
            .alignByBaseline(),
        textAlign = TextAlign.Start,
//        fontWeight = FontWeight.Bold,
//        color = AppColors.secondary
    )
}

@Composable
private fun UserProfileHeader(
    screenState: UserHomeViewModel.ScreenState.StatsAvailable,
    appNavController: NavController,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)
    ) {
        UserProfileImage(photoUrl = screenState.userPhotoUrl)
        RowSpacer(width = 10.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = screenState.userName,
                style = MaterialTheme.typography.h6,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )
            ColumnSpacer(height = 4.dp)
            Text(
                text = screenState.userRecentPlace ?: "",
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Normal
            )
        }
        RowSpacer(width = 10.dp)
        UserHomeOutlinedButton(
            text = stringResource(id = R.string.user_home_edit_profile_button)
        ) {
            appNavController.navigate(HomeNavDestination.Profile.routeWithUserId())
        }
    }
}

@Composable
private fun UserHomeOutlinedButton(
    text: String,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    width: Dp = 50.dp,
    onClick: () -> Unit,
) {
    OutlinedButton(
        shape = RoundedCornerShape(3.dp),
        onClick = { onClick() },
        border = BorderStroke(1.dp, AppColors.primary),
        colors = colors,
        modifier = modifier.size(width = width, height = 30.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun UserHomeTopBar() {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.user_home_title)) }
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun UserProfileImage(
    photoUrl: String?,
    imageLoadSizeDp: Dp = 60.dp,
    onClick: (() -> Unit)? = null,
) {
    val imageLoadSizePx = with(LocalDensity.current) { imageLoadSizeDp.roundToPx() }
    Surface(shape = CircleShape, modifier = Modifier.size(imageLoadSizeDp)) {
        Image(
            painter = rememberImagePainter(
                data = photoUrl,
                builder = {
                    size(imageLoadSizePx)
                    placeholder(R.drawable.common_avatar_placeholder_image)
                    error(R.drawable.common_avatar_placeholder_image)
                    scale(Scale.FILL)
                }
            ),
            contentDescription = "Athlete avatar",
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick?.invoke() }
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffff)
@Composable
private fun PreviewUserHome() {
    UserHome(
        screenState = UserHomeViewModel.ScreenState.StatsAvailable(
            "My Name",
            "photoUrl",
            "Saigon, Vietnam",
            mapOf(
                ActivityType.Running to GetTrainingSummaryDataUsecase.TrainingSummaryTableData(
                    thisWeekSummary = GetTrainingSummaryDataUsecase.TrainingSummaryData(
                        distance = 10000.0,
                        time = 10_800_000L,
                        activityCount = 3
                    ),
                    lastWeekSummary = GetTrainingSummaryDataUsecase.TrainingSummaryData(
                        distance = 7_000.0,
                        time = 8_800_000L,
                        activityCount = 2
                    ),
                    thisMonthSummary = GetTrainingSummaryDataUsecase.TrainingSummaryData(
                        distance = 10000.0,
                        time = 10_800_000L,
                        activityCount = 3
                    ),
                    lastMonthSummary = GetTrainingSummaryDataUsecase.TrainingSummaryData(
                        distance = 10000.0,
                        time = 10_800_000L,
                        activityCount = 3
                    )
                ),
                ActivityType.Cycling to GetTrainingSummaryDataUsecase.TrainingSummaryTableData()
            )
        ),
        contentPadding = PaddingValues(),
        rememberNavController()
    )
}
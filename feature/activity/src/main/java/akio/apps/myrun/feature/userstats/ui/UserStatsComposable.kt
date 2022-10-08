package akio.apps.myrun.feature.userstats.ui

import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.data.user.api.model.UserFollowCounter
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.user.GetTrainingSummaryDataUsecase
import akio.apps.myrun.domain.user.GetUserStatsTypeUsecase
import akio.apps.myrun.feature.activity.BuildConfig
import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.core.launchcatching.launchCatching
import akio.apps.myrun.feature.core.measurement.UnitFormatterSetFactory
import akio.apps.myrun.feature.core.ui.AppBarIconButton
import akio.apps.myrun.feature.core.ui.AppColors
import akio.apps.myrun.feature.core.ui.AppDimensions
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.core.ui.ColumnSpacer
import akio.apps.myrun.feature.core.ui.ErrorDialog
import akio.apps.myrun.feature.core.ui.RowSpacer
import akio.apps.myrun.feature.core.ui.StatusBarSpacer
import akio.apps.myrun.feature.userstats.UserStatsViewModel
import akio.apps.myrun.feature.userstats.di.DaggerUserStatsFeatureComponent
import android.app.Application
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale

@Composable
private fun rememberViewModel(arguments: UserStatsViewModel.UserStatsArguments): UserStatsViewModel {
    val application = LocalContext.current.applicationContext as Application
    return remember(arguments) {
        DaggerUserStatsFeatureComponent.factory().create(application, arguments)
            .userStatsViewModel()
    }
}

@Composable
private fun rememberUiState(
    contentPaddingBottom: Dp,
    scrollState: ScrollState = rememberScrollState(),
) = rememberSaveable(contentPaddingBottom, scrollState, saver = UserStatsUiState.Saver) {
    UserStatsUiState(contentPaddingBottom, scrollState)
}

@Composable
private fun rememberNavigator(
    navController: NavController
): UserStatsNavigator = remember(navController) {
    UserStatsNavigator(navController)
}

@Composable
fun UserStatsComposable(
    arguments: UserStatsViewModel.UserStatsArguments,
    contentPaddingBottom: Dp,
    appNavController: NavController,
    openRoutePlanningAction: () -> Unit,
    viewModel: UserStatsViewModel = rememberViewModel(arguments),
) {
    val uiState: UserStatsUiState = rememberUiState(contentPaddingBottom)
    val navigator: UserStatsNavigator = rememberNavigator(appNavController)
    AppTheme {
        Column {
            StatusBarSpacer()
            UserStatsTopBar(
                viewModel.isCurrentUser,
                navigator,
                openRoutePlanningAction
            )
            UserStatsContent(
                viewModel,
                uiState,
                navigator,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.height(uiState.contentPaddingBottom))
        }

        if (uiState.errorMessage != null) {
            ErrorDialog(text = uiState.errorMessage ?: "") {
                uiState.errorMessage = null
            }
        }
    }
}

@Composable
private fun UserStatsContent(
    viewModel: UserStatsViewModel,
    uiState: UserStatsUiState,
    navigator: UserStatsNavigator,
    modifier: Modifier = Modifier,
) {
    val userId = viewModel.userId
    Column(modifier = modifier.verticalScroll(uiState.scrollState)) {
        ColumnSpacer(height = AppDimensions.screenVerticalSpacing)
        ColumnSpacer(height = AppDimensions.sectionVerticalSpacing)
        UserProfileHeader(
            viewModel,
            uiState,
            viewModel.getUserProfile(),
            viewModel.getUserRecentPlaceName(),
            navigator::navigateProfileScreen
        )
        FollowCounterStats(
            viewModel.userId,
            viewModel.isCurrentUser,
            viewModel.getUserFollowCounter(),
            navigator
        )
        Divider(thickness = 4.dp)
        ColumnSpacer(height = AppDimensions.sectionVerticalSpacing * 2)
        TrainingSummaryTable(
            uiState,
            viewModel.getTrainingSummaryData(),
            viewModel.getMeasureSystem()
        )
    }
}

@Composable
private fun FollowCounterStats(
    userId: String,
    isCurrentUser: Boolean,
    userFollowCounter: UserFollowCounter,
    navigator: UserStatsNavigator,
) = Row(
    modifier = Modifier
        .padding(
            start = AppDimensions.screenHorizontalPadding,
            end = AppDimensions.screenHorizontalPadding,
            top = 20.dp,
            bottom = 14.dp
        )
        .clickable(enabled = isCurrentUser) {
            navigator.navigateUserFollowTab(userId)
        }
) {
    arrayOf(
        R.string.user_stats_following_label to userFollowCounter.followingCount,
        R.string.user_stats_follower_label to userFollowCounter.followerCount
    ).forEach { (@StringRes labelResId, counterValue) ->
        Column {
            Text(
                text = stringResource(labelResId),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold
            )
            Text(text = counterValue.toString(), fontSize = 20.sp)
        }
        RowSpacer(24.dp)
    }
}

@Composable
private fun TrainingSummaryTable(
    uiState: UserStatsUiState,
    trainingSummaryData: Map<ActivityType, GetTrainingSummaryDataUsecase.TrainingSummaryTableData>,
    measureSystem: MeasureSystem,
) {
    val summaryData = trainingSummaryData[uiState.selectedActivityType]
        ?: return
    val (distanceFormatter, durationFormatter) =
        UnitFormatterSetFactory.createStatsUnitFormatterSet(measureSystem)
    val thisWeekDistance = distanceFormatter.getFormattedValue(summaryData.thisWeekSummary.distance)
    val lastWeekDistance = distanceFormatter.getFormattedValue(summaryData.lastWeekSummary.distance)
    val thisWeekTime = durationFormatter.getFormattedValue(summaryData.thisWeekSummary.time)
    val lastWeekTime = durationFormatter.getFormattedValue(summaryData.lastWeekSummary.time)
    val thisMonthDist = distanceFormatter.getFormattedValue(summaryData.thisMonthSummary.distance)
    val lastMonthDist = distanceFormatter.getFormattedValue(summaryData.lastMonthSummary.distance)
    val thisMonthTime = durationFormatter.getFormattedValue(summaryData.thisMonthSummary.time)
    val lastMonthTime = durationFormatter.getFormattedValue(summaryData.lastMonthSummary.time)

    val context = LocalContext.current
    Column {
        ActivityTypePane(uiState.selectedActivityType) { uiState.selectedActivityType = it }
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
                        text = " (${distanceFormatter.getUnit(context)})",
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                TrainingSummaryProgress(current = thisWeekDistance, previous = lastWeekDistance)
                TrainingSummaryProgress(current = thisMonthDist, previous = lastMonthDist)
            }
            TableDivider()
            TableRow {
                TableCell {
                    TrainingSummaryLabel(
                        stringResource(id = R.string.user_home_summary_total_time_label),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = " (${durationFormatter.getUnit(context)})",
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                TrainingSummaryProgress(current = thisWeekTime, previous = lastWeekTime)
                TrainingSummaryProgress(current = thisMonthTime, previous = lastMonthTime)
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
            UserStatsOutlinedButton(
                text = stringResource(id = activityTypeLabel),
                onClick = { selectActivityTypeAction(type) },
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
private fun RowScope.TableCell(
    modifier: Modifier = Modifier,
    content: @Composable (RowScope.() -> Unit),
) = Row(
    modifier = modifier
        .weight(1f)
        .padding(vertical = 12.dp)
        .alignByBaseline(),
    content = content
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
        fontWeight = FontWeight.Bold
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
        textAlign = TextAlign.Start
//        fontWeight = FontWeight.Bold,
//        color = AppColors.secondary
    )
}

@Composable
private fun UserProfileHeader(
    viewModel: UserStatsViewModel,
    uiState: UserStatsUiState,
    userProfile: UserProfile,
    userRecentPlace: String?,
    onClickEdit: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)
    ) {
        UserProfileImage(photoUrl = userProfile.photo)
        RowSpacer(width = 10.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userProfile.name,
                style = MaterialTheme.typography.h6,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )
            if (!userRecentPlace.isNullOrBlank()) {
                ColumnSpacer(height = 4.dp)
                Text(
                    text = userRecentPlace,
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        RowSpacer(width = 10.dp)

        UserStatsActionButton(
            viewModel.getUserType(),
            viewModel.userId,
            onClickEdit,
            {
                scope.launchCatching({ uiState.errorMessage = it.message }) {
                    viewModel.followUser(userProfile)
                }
            },
            {
                scope.launchCatching({ uiState.errorMessage = it.message }) {
                    viewModel.unfollowUser()
                }
            }
        )
    }
}

@Composable
private fun UserStatsActionButton(
    userType: GetUserStatsTypeUsecase.UserStatsType,
    userId: String,
    onClickEdit: (String) -> Unit,
    onClickFollow: () -> Unit,
    onClickUnfollow: () -> Unit,
) {
    val (buttonTextRes, onClickAction) = when (userType) {
        GetUserStatsTypeUsecase.UserStatsType.CurrentUser ->
            R.string.user_home_edit_profile_button to { onClickEdit(userId) }
        GetUserStatsTypeUsecase.UserStatsType.FollowedUser ->
            R.string.action_unfollow to onClickUnfollow
        GetUserStatsTypeUsecase.UserStatsType.NotFollowedUser ->
            R.string.action_follow to onClickFollow
        GetUserStatsTypeUsecase.UserStatsType.Invalid -> null to null // "-"
    }

    val buttonText = if (buttonTextRes != null) {
        stringResource(buttonTextRes)
    } else {
        "-"
    }

    UserStatsOutlinedButton(buttonText, width = 65.dp) {
        onClickAction?.invoke()
    }
}

@Composable
private fun UserStatsOutlinedButton(
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
private fun UserStatsTopBar(
    isCurrentUser: Boolean,
    navigator: UserStatsNavigator,
    openRoutePlanningAction: () -> Unit,
) = TopAppBar(
    title = { Text(text = stringResource(id = R.string.home_nav_user_stats_tab_label)) },
    actions = {
        if (isCurrentUser) {
            TopBarActionButtons(openRoutePlanningAction, navigator)
        }
    }
)

@Composable
fun TopBarActionButtons(
    openRoutePlanningAction: () -> Unit,
    navigator: UserStatsNavigator,
) {
    if (BuildConfig.DEBUG) {
        AppBarIconButton(
            iconImageVector = Icons.Rounded.Add,
            onClick = openRoutePlanningAction
        )
    }
    AppBarIconButton(
        iconImageVector = Icons.Rounded.Settings,
        onClick = navigator::navigateUserPreferencesScreen
    )
}

@Composable
private fun UserProfileImage(
    photoUrl: String?,
    imageLoadSizeDp: Dp = 60.dp,
) {
    val imageLoadSizePx = with(LocalDensity.current) { imageLoadSizeDp.roundToPx() }
    Surface(shape = CircleShape, modifier = Modifier.size(imageLoadSizeDp)) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = photoUrl)
                    .apply {
                        size(imageLoadSizePx)
                        placeholder(R.drawable.common_avatar_placeholder_image)
                        error(R.drawable.common_avatar_placeholder_image)
                        scale(Scale.FILL)
                    }
                    .build()
            ),
            contentDescription = "Athlete avatar",
            modifier = Modifier.fillMaxSize()
        )
    }
}

// @Preview(showBackground = true, backgroundColor = 0xffffff)
// @Composable
// private fun PreviewUserStats() {
//    UserStatsScreen(
//        uiState = UserStatsViewModel.ScreenState.StatsAvailable(
//            UserProfile(name = "Super man", photo = "photo Url", accountId = "accountId"),
//            GetUserStatsTypeUsecase.UserStatsType.FollowedUser,
//            "Saigon, Vietnam",
//            mapOf(
//                ActivityType.Running to GetTrainingSummaryDataUsecase.TrainingSummaryTableData(
//                    thisWeekSummary = GetTrainingSummaryDataUsecase.TrainingSummaryData(
//                        distance = 10000.0,
//                        time = 10_800_000L,
//                        activityCount = 3
//                    ),
//                    lastWeekSummary = GetTrainingSummaryDataUsecase.TrainingSummaryData(
//                        distance = 7_000.0,
//                        time = 8_800_000L,
//                        activityCount = 2
//                    ),
//                    thisMonthSummary = GetTrainingSummaryDataUsecase.TrainingSummaryData(
//                        distance = 10000.0,
//                        time = 10_800_000L,
//                        activityCount = 3
//                    ),
//                    lastMonthSummary = GetTrainingSummaryDataUsecase.TrainingSummaryData(
//                        distance = 10000.0,
//                        time = 10_800_000L,
//                        activityCount = 3
//                    )
//                ),
//                ActivityType.Cycling to GetTrainingSummaryDataUsecase.TrainingSummaryTableData()
//            ),
//            measureSystem = MeasureSystem.Default,
//            UserFollowCounter(12, 34)
//        ),
//        contentPadding = PaddingValues(),
//        rememberNavController(),
//        { },
//        { },
//        { },
//        { }
//    )
// }

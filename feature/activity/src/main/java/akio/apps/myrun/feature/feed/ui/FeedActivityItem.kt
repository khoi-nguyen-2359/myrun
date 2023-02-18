package akio.apps.myrun.feature.feed.ui

import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.AthleteInfo
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.activity.ActivityDateTimeFormatter
import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.core.measurement.TrackUnitFormatter
import akio.apps.myrun.feature.core.measurement.TrackUnitFormatterSet
import akio.apps.myrun.feature.core.measurement.UnitFormatterSetFactory
import akio.apps.myrun.feature.core.ui.UserAvatarImage
import akio.apps.myrun.feature.feed.model.FeedActivity
import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun FeedActivityItem(
    feedItem: FeedActivity,
    userProfile: UserProfile?,
    preferredUnitSystem: MeasureSystem,
    navigator: FeedNavigator,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
) {
    val activity = feedItem.activityData
    val uiState = rememberUiState()
    FeedActivityItem(
        uiState,
        feedItem.activityData,
        feedItem.formattedStartTime,
        feedItem.locationName,
        userProfile,
        preferredUnitSystem,
        { navigator.navigateActivityDetail(activity.id) },
        { onClickExportActivityFile(activity) },
        { navigator.navigateUserStats(feedItem.isCurrentUser, activity.athleteInfo.userId) }
    )
}

@Composable
private fun rememberUiState() =
    rememberSaveable(saver = FeedActivityItemUiState.Saver) { FeedActivityItemUiState() }

@Composable
private fun FeedActivityItem(
    uiState: FeedActivityItemUiState,
    activity: BaseActivityModel,
    activityFormattedStartTime: ActivityDateTimeFormatter.Result,
    activityDisplayPlaceName: String,
    userProfile: UserProfile?,
    preferredSystem: MeasureSystem,
    onClickActivityAction: (BaseActivityModel) -> Unit,
    onClickExportFile: () -> Unit,
    onClickUserAvatar: () -> Unit,
) = FeedItem {
    Column(modifier = Modifier.clickable { onClickActivityAction(activity) }) {
        Spacer(modifier = Modifier.height(ActivityFeedDimensions.feedItemVerticalPadding))
        ActivityInformationView(
            uiState,
            activity,
            activityFormattedStartTime,
            activityDisplayPlaceName,
            userProfile,
            onClickExportFile,
            onClickUserAvatar
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActivityRouteImageBox(activity, preferredSystem, uiState)
    }
}

@Composable
private fun ActivityRouteImageBox(
    activity: BaseActivityModel,
    preferredSystem: MeasureSystem,
    uiState: FeedActivityItemUiState,
) = Box(contentAlignment = Alignment.TopStart) {
    ActivityRouteImage(activity)
    ActivityPerformanceRow(
        activity,
        preferredSystem,
        uiState,
        modifier = Modifier.padding(
            horizontal = ActivityFeedDimensions.activityItemHorizontalPadding,
            vertical = ActivityFeedDimensions.feedItemVerticalPadding
        )
    )
}

private fun createActivityFormatterList(
    activityType: ActivityType,
    trackUnitFormatterSet: TrackUnitFormatterSet,
): List<TrackUnitFormatter<*>> =
    when (activityType) {
        ActivityType.Running -> listOf(
            trackUnitFormatterSet.distanceFormatter,
            trackUnitFormatterSet.paceFormatter
        )
        ActivityType.Cycling -> listOf(
            trackUnitFormatterSet.distanceFormatter,
            trackUnitFormatterSet.speedFormatter
        )
        else -> emptyList()
    }

private const val PERFORMANCE_VALUE_DELIM = " - "

@Composable
private fun ActivityPerformanceRow(
    activity: BaseActivityModel,
    measureSystem: MeasureSystem,
    uiState: FeedActivityItemUiState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val performanceValue = remember(uiState.isDetailExpanded, measureSystem) {
        makePerformanceDisplayText(measureSystem, activity, context, uiState.isDetailExpanded)
    }

    OutlinedButton(
        shape = RoundedCornerShape(3.dp),
        onClick = { uiState.isDetailExpanded = !uiState.isDetailExpanded },
        border = null,
        colors = ButtonDefaults.outlinedButtonColors(
            backgroundColor = Color(0xff494949),
            contentColor = Color.White
        ),
        modifier = modifier.height(30.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        elevation = ButtonDefaults.elevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = performanceValue,
            style = MaterialTheme.typography.caption,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.animateContentSize()
        )
    }
}

private fun makePerformanceDisplayText(
    measureSystem: MeasureSystem,
    activity: BaseActivityModel,
    context: Context,
    isExpanded: Boolean,
): String {
    val trackValueFormatterPreference =
        UnitFormatterSetFactory.createUnitFormatterSet(measureSystem)
    val selectedFormatterList = createActivityFormatterList(
        activity.activityType,
        trackValueFormatterPreference
    ).let { formatterList ->
        if (!isExpanded) {
            // show only one stats number when the box is collapsed
            listOfNotNull(formatterList.firstOrNull())
        } else {
            formatterList
        }
    }
    return selectedFormatterList.fold("") { acc, performedResultFormatter ->
        val formattedValue = performedResultFormatter.getFormattedValue(activity)
        val unit = performedResultFormatter.getUnit(context)
        val presentedText = "$formattedValue $unit"
        "$acc$presentedText$PERFORMANCE_VALUE_DELIM"
    }
        .removeSuffix(PERFORMANCE_VALUE_DELIM)
}

@Composable
private fun ActivityInformationView(
    uiState: FeedActivityItemUiState,
    activity: BaseActivityModel,
    activityFormattedStartTime: ActivityDateTimeFormatter.Result,
    activityDisplayPlaceName: String?,
    userProfile: UserProfile?,
    onClickExportFile: () -> Unit,
    onClickUserAvatar: () -> Unit,
) = Column(
    modifier = Modifier.padding(start = ActivityFeedDimensions.activityItemHorizontalPadding)
) {
    val (userName, userAvatar) = if (userProfile?.accountId == activity.athleteInfo.userId) {
        userProfile.name to userProfile.photo
    } else {
        activity.athleteInfo.userName to activity.athleteInfo.userAvatar
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        UserAvatarImage(userAvatar, onClickUserAvatar = onClickUserAvatar)
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            AthleteNameText(userName.orEmpty())
            Spacer(modifier = Modifier.height(2.dp))
            ActivityTimeAndPlaceText(activityFormattedStartTime, activityDisplayPlaceName)
        }
        ActivityShareMenu(uiState, onClickExportFile)
    }
    Spacer(modifier = Modifier.size(12.dp))
    ActivityNameText(activity)
}

@Composable
private fun AthleteNameText(userProfileName: String) = Text(
    text = userProfileName,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    fontWeight = FontWeight.Bold,
    style = MaterialTheme.typography.subtitle1
)

@Composable
private fun ActivityNameText(
    activityDetail: BaseActivityModel,
    modifier: Modifier = Modifier,
) = Text(
    text = activityDetail.name,
    modifier = modifier.fillMaxWidth(),
    fontWeight = FontWeight.Bold,
    style = MaterialTheme.typography.h6
)

@Composable
private fun ActivityShareMenu(
    uiState: FeedActivityItemUiState,
    onClickExportFile: () -> Unit,
) = Box(
    modifier = Modifier.padding(horizontal = 6.dp)
) {
    IconButton(
        onClick = { uiState.isShareMenuExpanded = !uiState.isShareMenuExpanded }
    ) {
        Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = "Share icon"
        )
    }
    DropdownMenu(
        expanded = uiState.isShareMenuExpanded,
        onDismissRequest = { uiState.isShareMenuExpanded = false }
    ) {
        DropdownMenuItem(
            onClick = {
                onClickExportFile()
                uiState.isShareMenuExpanded = false
            }
        ) {
            Text(text = stringResource(id = R.string.activity_share_menu_item_export_file))
        }
    }
}

@Composable
private fun ActivityTimeAndPlaceText(
    activityFormattedStartTime: ActivityDateTimeFormatter.Result,
    activityDisplayPlaceName: String?,
) {
    val context = LocalContext.current
    val startTimeText = remember {
        when (activityFormattedStartTime) {
            is ActivityDateTimeFormatter.Result.WithinToday -> context.getString(
                R.string.item_activity_time_today,
                activityFormattedStartTime.formattedValue
            )
            is ActivityDateTimeFormatter.Result.WithinYesterday -> context.getString(
                R.string.item_activity_time_yesterday,
                activityFormattedStartTime.formattedValue
            )
            is ActivityDateTimeFormatter.Result.FullDateTime ->
                activityFormattedStartTime.formattedValue
        }
    }
    val timeAndPlaceText = remember {
        if (activityDisplayPlaceName.isNullOrEmpty()) {
            startTimeText
        } else {
            "$startTimeText \u00b7 $activityDisplayPlaceName"
        }
    }
    Text(
        text = timeAndPlaceText,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        style = MaterialTheme.typography.caption
    )
}

@Preview
@Composable
private fun PreviewFeedActivityItem() {
    FeedActivityItem(
        rememberUiState(),
        activity = RunningActivityModel(
            activityData = ActivityDataModel(
                id = "id",
                activityType = ActivityType.Running,
                name = "Evening Run",
                routeImage = "http://example.com",
                placeIdentifier = null,
                startTime = System.currentTimeMillis(),
                endTime = 2000L,
                duration = 1000L,
                distance = 1234.0,
                encodedPolyline = "",
                athleteInfo = AthleteInfo(
                    userId = "id",
                    userName = "Khoi Nguyen",
                    userAvatar = "userAvatar"
                )
            ),
            pace = 12.34,
            cadence = 160
        ),
        activityDisplayPlaceName = "activityDisplayPlaceName",
        onClickActivityAction = { },
        onClickExportFile = { },
        onClickUserAvatar = { },
        userProfile = UserProfile(accountId = "userId", photo = null),
        activityFormattedStartTime = ActivityDateTimeFormatter.Result.FullDateTime("dd/mm/yyyy"),
        preferredSystem = MeasureSystem.Default
    )
}

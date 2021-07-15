package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.feature.activitydetail.ActivityDateTimeFormatter
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityData
import akio.apps.myrun.feature.usertimeline.model.RunningActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import timber.log.Timber

@Composable
fun ActivityInfoHeaderView(
    activityDetail: Activity,
    activityDisplayPlaceName: String?,
    onClickExportFile: () -> Unit,
    isShareMenuVisible: Boolean = true
) = Column(
    modifier = Modifier
        .padding(vertical = dimensionResource(id = R.dimen.common_item_vertical_padding))
        .padding(start = dimensionResource(id = R.dimen.common_item_horizontal_padding))
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        UserAvatarImage(activityDetail = activityDetail)
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            AthleteNameText(activityDetail)
            ActivityTimeAndPlaceText(activityDetail, activityDisplayPlaceName)
        }
        if (isShareMenuVisible) {
            ActivityShareMenu(onClickExportFile)
        }
    }
    Spacer(modifier = Modifier.size(6.dp))
    ActivityNameText(activityDetail)
}

@Composable
private fun AthleteNameText(activityDetail: Activity) = Text(
    text = activityDetail.athleteInfo.userName.orEmpty(),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    style = MaterialTheme.typography.subtitle2,
    fontWeight = FontWeight.Bold
)

@Composable
private fun ActivityNameText(activityDetail: Activity) = Text(
    text = activityDetail.name,
    modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, end = dimensionResource(id = R.dimen.common_item_horizontal_padding)),
    style = MaterialTheme.typography.h6,
    fontWeight = FontWeight.Bold
)

@Composable
private fun ActivityShareMenu(
    onClickExportFile: () -> Unit
) = Box(
    modifier = Modifier.padding(horizontal = 4.dp)
) {
    var isExpanded by remember { mutableStateOf(false) }
    IconButton(
        onClick = { isExpanded = !isExpanded }
    ) {
        Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = "Share icon"
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
            Text(text = stringResource(id = R.string.activity_details_share_menu_item_export_file))
        }
    }
}

@Composable
private fun ActivityTimeAndPlaceText(activityDetail: Activity, activityDisplayPlaceName: String?) {
    val activityDateTimeFormatter = remember(::ActivityDateTimeFormatter)
    val activityFormattedStartTime =
        remember { activityDateTimeFormatter.formatActivityDateTime(activityDetail.startTime) }
    val context = LocalContext.current
    val startTimeText = remember(activityDetail.id) {
        Timber.d("making startTimeText")
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
    Timber.d("startTimeText=$startTimeText")
    val timeAndPlaceText = remember {
        if (activityDisplayPlaceName == null) {
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

@Composable
private fun UserAvatarImage(
    activityDetail: Activity
) {
    val avatarDimension = dimensionResource(id = R.dimen.user_timeline_avatar_size)
    val avatarSize = with(LocalDensity.current) { avatarDimension.toPx() }
    Image(
        painter = rememberImagePainter(
            data = activityDetail.athleteInfo.userAvatar.orEmpty(),
            builder = {
               size(avatarSize.toInt())
               placeholder(R.drawable.common_avatar_placeholder_image)
               error(R.drawable.common_avatar_placeholder_image)
            }
        ),
        contentDescription = "Athlete avatar",
        modifier = Modifier
            .size(avatarDimension)
            .clip(CircleShape)
    )
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewActivityInfoHeader() = ActivityInfoHeaderView(
    activityDetail = RunningActivity(
        activityData = ActivityData(
            id = "id",
            activityType = ActivityType.Running,
            name = "Evening Run",
            routeImage = "http://example.com",
            placeIdentifier = null,
            startTime = System.currentTimeMillis(),
            endTime = 2000L,
            duration = 1000L,
            distance = 100.0,
            encodedPolyline = "",
            athleteInfo = Activity.AthleteInfo(
                userId = "id",
                userName = "Khoi Nguyen",
                userAvatar = "userAvatar"
            )
        ),
        pace = 1.0,
        cadence = 160
    ),
    activityDisplayPlaceName = "California, Santa Clara County, San Jose",
    {}
)

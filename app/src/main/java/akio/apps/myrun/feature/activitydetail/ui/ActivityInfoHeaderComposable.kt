package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.feature.activitydetail.ActivityDateTimeFormatter
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityData
import akio.apps.myrun.feature.usertimeline.model.RunningActivity
import android.content.Context
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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.coil.rememberCoilPainter
import java.util.Calendar

@Composable
fun ActivityInfoHeaderView(
    activityDetail: Activity,
    activityDisplayPlaceName: String?
) = Column(
    modifier = Modifier.padding(
        horizontal = dimensionResource(id = R.dimen.common_item_horizontal_padding),
        vertical = dimensionResource(id = R.dimen.common_item_vertical_padding)
    )
) {
    Row {
        UserAvatarImage(activityDetail = activityDetail, modifier = Modifier)
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            Text(
                text = activityDetail.athleteInfo.userName.orEmpty(),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            ActivityTimeAndPlaceText(activityDetail, activityDisplayPlaceName)
        }
        ActivityShareMenu()
    }
    Spacer(modifier = Modifier.size(6.dp))
    Text(
        text = createActivityName(LocalContext.current, activityDetail),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ActivityShareMenu() = Box {
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
            onClick = { }
        ) {
            Text(text = "Export tracklog")
        }
    }
}

@Composable
private fun ActivityTimeAndPlaceText(activityDetail: Activity, activityDisplayPlaceName: String?) {
    val activityDateTimeFormatter = remember(::ActivityDateTimeFormatter)
    val activityFormattedStartTime =
        activityDateTimeFormatter.formatActivityDateTime(activityDetail.startTime)
    val startTimeText = when (activityFormattedStartTime) {
        is ActivityDateTimeFormatter.Result.WithinToday -> LocalContext.current.getString(
            R.string.item_activity_time_today,
            activityFormattedStartTime.formattedValue
        )
        is ActivityDateTimeFormatter.Result.WithinYesterday -> LocalContext.current.getString(
            R.string.item_activity_time_yesterday,
            activityFormattedStartTime.formattedValue
        )
        is ActivityDateTimeFormatter.Result.FullDateTime ->
            activityFormattedStartTime.formattedValue
    }
    val timeAndPlaceText = if (activityDisplayPlaceName == null) {
        startTimeText
    } else {
        "$startTimeText \u00b7 $activityDisplayPlaceName"
    }
    Text(text = timeAndPlaceText, fontSize = 12.sp, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun UserAvatarImage(
    activityDetail: Activity,
    modifier: Modifier
) {
    val avatarDimension = dimensionResource(id = R.dimen.user_timeline_avatar_size)
    val avatarSize = with(LocalDensity.current) { avatarDimension.toPx() }
    Image(
        painter = rememberCoilPainter(
            request = activityDetail.athleteInfo.userAvatar.orEmpty(),
            requestBuilder = {
                size(avatarSize.toInt())
                placeholder(R.drawable.common_avatar_placeholder_image)
                error(R.drawable.common_avatar_placeholder_image)
            }
        ),
        contentDescription = "Athlete avatar",
        modifier = modifier
            .size(avatarDimension)
            .clip(CircleShape)
    )
}

private fun createActivityName(
    context: Context,
    activityDetail: Activity
): String = if (activityDetail.name.isEmpty()) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = activityDetail.startTime
    when (calendar.get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> context.getString(
            R.string.item_activity_title_morning,
            context.getString(activityTypeNameMap[activityDetail.activityType] ?: 0)
        )
        in 12..16 -> context.getString(
            R.string.item_activity_title_afternoon,
            context.getString(activityTypeNameMap[activityDetail.activityType] ?: 0)
        )
        else -> context.getString(
            R.string.item_activity_title_evening,
            context.getString(activityTypeNameMap[activityDetail.activityType] ?: 0)
        )
    }
} else {
    activityDetail.name
}

private val activityTypeNameMap: Map<ActivityType, Int> = mapOf(
    ActivityType.Running to R.string.activity_name_running,
    ActivityType.Cycling to R.string.activity_name_cycling
)

@Composable
@Preview
private fun PreviewActivityInfoHeader() = ActivityInfoHeaderView(
    activityDetail = RunningActivity(
        activityData = ActivityData(
            id = "id",
            activityType = ActivityType.Running,
            name = "activity name",
            routeImage = "http://example.com",
            placeIdentifier = null,
            startTime = 1000L,
            endTime = 2000L,
            duration = 1000L,
            distance = 100.0,
            encodedPolyline = "",
            athleteInfo = Activity.AthleteInfo(
                userId = "id",
                userName = "userName userName userName userName userName userName userName",
                userAvatar = "userAvatar"
            )
        ),
        pace = 1.0,
        cadence = 160
    ),
    activityDisplayPlaceName = "D8 W16"
)

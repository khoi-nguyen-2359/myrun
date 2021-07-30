package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.feature.activitydetail.ActivityDateTimeFormatter
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityData
import akio.apps.myrun.feature.usertimeline.model.RunningActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import coil.compose.rememberImagePainter
import timber.log.Timber

@Composable
fun ActivityInfoHeaderView(
    activityDetail: Activity,
    activityDisplayPlaceName: String?,
    onClickUserAvatar: () -> Unit
) = Column(
    modifier = Modifier
        .padding(
            vertical = dimensionResource(id = R.dimen.common_item_vertical_padding),
            horizontal = dimensionResource(id = R.dimen.common_item_horizontal_padding)
        )
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        UserAvatarImage(activityDetail = activityDetail, onClickUserAvatar)
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            AthleteNameText(activityDetail)
            ActivityTimeAndPlaceText(activityDetail, activityDisplayPlaceName)
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
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp
)

@Composable
private fun ActivityNameText(activityDetail: Activity) = Text(
    text = activityDetail.name,
    modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, end = dimensionResource(id = R.dimen.common_item_horizontal_padding)),
    fontSize = 22.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 0.5.sp
)

@Composable
private fun ActivityTimeAndPlaceText(activityDetail: Activity, activityDisplayPlaceName: String?) {
    val activityDateTimeFormatter = remember(::ActivityDateTimeFormatter)
    val activityFormattedStartTime =
        remember { activityDateTimeFormatter.formatActivityDateTime(activityDetail.startTime) }
    val context = LocalContext.current
    val startTimeText = remember {
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
        fontSize = 13.sp
    )
}

@Composable
private fun UserAvatarImage(
    activityDetail: Activity,
    onClickUserAvatar: () -> Unit
) {
    val avatarDimension = 50.dp
    val avatarSize = with(LocalDensity.current) { avatarDimension.toPx() }
    Image(
        painter = rememberImagePainter(
            data = activityDetail.athleteInfo.userAvatar.orEmpty(),
            builder = {
                size(avatarSize.toInt())
                    .placeholder(R.drawable.common_avatar_placeholder_image)
                    .error(R.drawable.common_avatar_placeholder_image)
            }
        ),
        contentDescription = "Athlete avatar",
        modifier = Modifier
            .size(avatarDimension)
            .clip(CircleShape)
            .clickable { onClickUserAvatar() }
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
    activityDisplayPlaceName = "California, Santa Clara County, San Jose"
) {}

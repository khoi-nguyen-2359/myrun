package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.AthleteInfo
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.domain.activity.ActivityDateTimeFormatter
import akio.apps.myrun.feature.activity.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
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
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import timber.log.Timber

@Composable
internal fun ActivityInfoHeaderView(
    activityDetail: BaseActivityModel,
    activityFormattedStartTime: ActivityDateTimeFormatter.Result,
    activityDisplayPlaceName: String?,
    onClickUserAvatar: () -> Unit,
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
            ActivityTimeAndPlaceText(
                activityFormattedStartTime,
                activityDisplayPlaceName,
            )
        }
    }
    Spacer(modifier = Modifier.size(6.dp))
    ActivityNameText(activityDetail)
}

@Composable
private fun AthleteNameText(activityDetail: BaseActivityModel) = Text(
    text = activityDetail.athleteInfo.userName.orEmpty(),
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    fontWeight = FontWeight.Bold,
    style = MaterialTheme.typography.subtitle1
)

@Composable
private fun ActivityNameText(activityDetail: BaseActivityModel) = Text(
    text = activityDetail.name,
    modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, end = dimensionResource(id = R.dimen.common_item_horizontal_padding)),
    fontWeight = FontWeight.Bold,
    style = MaterialTheme.typography.h6
)

@Composable
private fun ActivityTimeAndPlaceText(
    activityFormattedStartTime: ActivityDateTimeFormatter.Result,
    activityDisplayPlaceName: String?,
) {
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
        style = MaterialTheme.typography.caption
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun UserAvatarImage(
    activityDetail: BaseActivityModel,
    onClickUserAvatar: () -> Unit,
) {
    val avatarDimension = 46.dp
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
    activityDetail = RunningActivityModel(
        activityData = ActivityDataModel(
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
            athleteInfo = AthleteInfo(
                userId = "id",
                userName = "Khoi Nguyen",
                userAvatar = "userAvatar"
            )
        ),
        pace = 1.0,
        cadence = 160
    ),
    activityDisplayPlaceName = "California, Santa Clara County, San Jose",
    activityFormattedStartTime = ActivityDateTimeFormatter.Result.WithinToday("today-string")
) {}

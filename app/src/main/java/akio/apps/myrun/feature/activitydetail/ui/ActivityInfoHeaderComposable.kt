package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.feature.activitydetail.ActivityDateTimeFormatter
import akio.apps.myrun.feature.usertimeline.model.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.coil.rememberCoilPainter
import java.util.Calendar

@Composable
fun ActivityInfoHeaderComposable(
    activityDetail: Activity,
    activityDisplayPlaceName: String?
) = ConstraintLayout(
    modifier = Modifier.padding(
        horizontal = dimensionResource(id = R.dimen.common_item_horizontal_padding),
        vertical = dimensionResource(id = R.dimen.common_item_vertical_padding)
    )
) {
    val (
        layoutRefProfileImage,
        layoutRefUserNameText,
        layoutRefElementSpacer,
        layoutRefActivityDateTime,
        layoutRefActivityName
    ) = createRefs()

    val activityDateTimeFormatter = remember(::ActivityDateTimeFormatter)
    val activityFormattedStartTime =
        activityDateTimeFormatter.formatActivityDateTime(activityDetail.startTime)

    val resources = LocalContext.current.resources
    Image(
        painter = rememberCoilPainter(
            request = activityDetail.athleteInfo.userAvatar.orEmpty(),
            requestBuilder = {
                size(resources.getDimensionPixelSize(R.dimen.user_timeline_avatar_size))
                placeholder(R.drawable.common_avatar_placeholder_image)
                error(R.drawable.common_avatar_placeholder_image)
            }
        ),
        contentDescription = "Athlete avatar",
        modifier = Modifier
            .constrainAs(layoutRefProfileImage) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
            .size(dimensionResource(id = R.dimen.user_timeline_avatar_size))
            .clip(CircleShape)
    )
    Spacer(
        modifier = Modifier
            .size(12.dp)
            .constrainAs(layoutRefElementSpacer) {
                top.linkTo(layoutRefProfileImage.bottom)
                start.linkTo(layoutRefProfileImage.end)
            }
    )
    Text(
        text = activityDetail.athleteInfo.userName.orEmpty(),
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .width(0.dp)
            .constrainAs(layoutRefUserNameText) {
                top.linkTo(layoutRefProfileImage.top)
                start.linkTo(layoutRefElementSpacer.end)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
    )
    val startTimeText = createActivityStartTimeText(activityFormattedStartTime)
    val timeAndPlaceText = if (activityDisplayPlaceName == null) {
        startTimeText
    } else {
        "$startTimeText \u00b7 $activityDisplayPlaceName"
    }
    Text(
        text = timeAndPlaceText,
        fontSize = 12.sp,
        modifier = Modifier.constrainAs(layoutRefActivityDateTime) {
            top.linkTo(layoutRefUserNameText.bottom)
            start.linkTo(layoutRefUserNameText.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
        }
    )
    Text(
        text = createActivityName(LocalContext.current, activityDetail),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .constrainAs(layoutRefActivityName) {
                top.linkTo(layoutRefElementSpacer.bottom)
            }
            .fillMaxWidth()
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

@Composable
private fun createActivityStartTimeText(formatResult: ActivityDateTimeFormatter.Result) =
    when (formatResult) {
        is ActivityDateTimeFormatter.Result.WithinToday -> LocalContext.current.getString(
            R.string.item_activity_time_today,
            formatResult.formattedValue
        )
        is ActivityDateTimeFormatter.Result.WithinYesterday -> LocalContext.current.getString(
            R.string.item_activity_time_yesterday,
            formatResult.formattedValue
        )
        is ActivityDateTimeFormatter.Result.FullDateTime ->
            formatResult.formattedValue
    }

@Composable
private fun UserAvatarPlaceholderComposable() = Image(
    painter = painterResource(R.drawable.common_avatar_placeholder_image),
    contentDescription = "Athlete avatar placeholder",
    modifier = Modifier.size(dimensionResource(id = R.dimen.user_timeline_avatar_size))
)

private val activityTypeNameMap: Map<ActivityType, Int> = mapOf(
    ActivityType.Running to R.string.activity_name_running,
    ActivityType.Cycling to R.string.activity_name_cycling
)

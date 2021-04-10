package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.R
import akio.apps.myrun.feature.activitydetail.ActivityDateTimeFormatter
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.glide.GlideImage

@Composable
fun ActivityInfoHeaderComposable(
    userInfo: Activity.AthleteInfo,
    activityFormattedStartTime: ActivityDateTimeFormatter.Result,
    activityName: String
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

    val resources = LocalContext.current.resources
    GlideImage(
        data = userInfo.userAvatar.orEmpty(),
        contentDescription = "Athlete avatar",
        requestBuilder = {
            override(resources.getDimensionPixelSize(R.dimen.user_timeline_avatar_size))
        },
        loading = { UserAvatarPlaceholderComposable() },
        error = { UserAvatarPlaceholderComposable() },
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
        text = userInfo.userName.orEmpty(),
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
    val dateTimeDisplayValue = when (activityFormattedStartTime) {
        is ActivityDateTimeFormatter.Result.WithinToday -> LocalContext.current.getString(
            R.string.item_activity_time_today,
            activityFormattedStartTime.formattedValue
        )
        is ActivityDateTimeFormatter.Result.WithinYesterday -> LocalContext.current.getString(
            R.string.item_activity_time_yesterday,
            activityFormattedStartTime.formattedValue
        )
        is ActivityDateTimeFormatter.Result.FullDateTime -> {
            activityFormattedStartTime.formattedValue
        }
    }
    Text(
        text = dateTimeDisplayValue,
        fontSize = 12.sp,
        modifier = Modifier.constrainAs(layoutRefActivityDateTime) {
            top.linkTo(layoutRefUserNameText.bottom)
            start.linkTo(layoutRefUserNameText.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
        }
    )
    Text(
        text = activityName,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .constrainAs(layoutRefActivityName) {
                top.linkTo(layoutRefElementSpacer.bottom)
            }
            .fillMaxWidth()
    )
}

@Composable
fun UserAvatarPlaceholderComposable() {
    Image(
        painter = painterResource(R.drawable.common_avatar_placeholder_image),
        contentDescription = "Athlete avatar placeholder",
        modifier = Modifier.size(dimensionResource(id = R.dimen.user_timeline_avatar_size))
    )
}

@Preview
@Composable
private fun ActivityInfoHeaderComposablePreview() {
    ActivityInfoHeaderComposable(
        Activity.AthleteInfo(
            "userId",
            "userName",
            "https://image.shutterstock.com/image-vector/sample-stamp-grunge-texture-vector-260nw-1389188336.jpg"
        ),
        ActivityDateTimeFormatter.Result.FullDateTime("Blah"),
        "activityName"
    )
}

package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Composable
fun ActivityInfoHeaderCompose() = ConstraintLayout(
    modifier = Modifier.padding(
        horizontal = dimensionResource(id = R.dimen.common_item_horizontal_padding),
        vertical = dimensionResource(id = R.dimen.common_item_vertical_padding)
    )
) {
    val (
        profileImage,
        userNameText,
        elementSpacer,
        activityDateTime,
        activityName
    ) = createRefs()

    Image(
        painter = painterResource(id = R.drawable.ic_person),
        contentDescription = "",
        modifier = Modifier
            .constrainAs(profileImage) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
            }
            .size(dimensionResource(id = R.dimen.user_timeline_avatar_size))
    )
    Spacer(
        modifier = Modifier
            .size(12.dp)
            .constrainAs(elementSpacer) {
                top.linkTo(profileImage.bottom)
                start.linkTo(profileImage.end)
            }
    )
    Text(
        text = "User asd asd asd asd asd asd asd asd asd asd asd asd asd as dasdasd asd as dName",
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .width(0.dp)
            .constrainAs(userNameText) {
                top.linkTo(profileImage.top)
                start.linkTo(elementSpacer.end)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
    )
    Text(
        text = "Date Time",
        fontSize = 12.sp,
        modifier = Modifier.constrainAs(activityDateTime) {
            top.linkTo(userNameText.bottom)
            start.linkTo(userNameText.start)
            end.linkTo(parent.end)
            width = Dimension.fillToConstraints
        }
    )
    Text(
        text = "Activity Name",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .constrainAs(activityName) {
                top.linkTo(elementSpacer.bottom)
            }
            .fillMaxWidth()
    )
}

@Preview
@Composable
private fun ActivityInfoHeaderComposePreview() {
    ActivityDetailCompose()
}

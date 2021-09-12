package akio.apps.myrun.feature.userhome.ui

import akio.apps.myrun.feature.base.ui.AppDimensions
import akio.apps.myrun.feature.base.ui.CentralLoadingView
import akio.apps.myrun.feature.base.ui.StatusBarSpacer
import akio.apps.myrun.feature.profile.R
import akio.apps.myrun.feature.userhome.UserHomeViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.size.Scale

@Composable
fun UserHome(
    userHomeViewModel: UserHomeViewModel,
    contentPadding: PaddingValues
) {
    val screenState by userHomeViewModel.screenState.collectAsState(
        initial = UserHomeViewModel.ScreenState.StatsLoading
    )
    UserHome(contentPadding, screenState)
}

@Composable
private fun UserHome(
    contentPadding: PaddingValues,
    screenState: UserHomeViewModel.ScreenState = UserHomeViewModel.ScreenState.StatsLoading,
) {
    Column {
        StatusBarSpacer()
        UserHomeTopBar()
        when (screenState) {
            UserHomeViewModel.ScreenState.StatsLoading -> {
                CentralLoadingView(text = stringResource(id = R.string.message_loading))
            }
            is UserHomeViewModel.ScreenState.StatsAvailable -> {
                UserHomeContent(screenState, modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding()))
    }
}

@Composable
fun UserHomeContent(
    screenState: UserHomeViewModel.ScreenState.StatsAvailable,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(50.dp))
        UserProfileHeader(screenState)
        Spacer(modifier = Modifier.height(AppDimensions.sectionVerticalSpacing))
        TrainingSummaryTable()
    }
}

@Composable
fun TrainingSummaryTable() {
    Column(modifier = Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)) {
        Row {
            OutlinedButton(onClick = { /*TODO*/ }) {
                Text(stringResource(id = R.string.user_home_training_summary_run_activity_type))
            }
            Spacer(modifier = Modifier.width(5.dp))
            OutlinedButton(onClick = { /*TODO*/ }) {
                Text(stringResource(id = R.string.user_home_training_summary_ride_activity_type))
            }
        }
        Spacer(modifier = Modifier.height(AppDimensions.rowVerticalPadding))
        Row {
            Column(modifier = Modifier.weight(1f)) {
                TrainingSummaryLabel(text = "\n")
                TrainingSummaryLabel(stringResource(id = R.string.user_home_summary_distance_label))
                TrainingSummaryLabel(stringResource(id = R.string.user_home_summary_pace_label))
                TrainingSummaryLabel(
                    stringResource(id = R.string.user_home_summary_activities_label)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                TrainingSummaryLabel(stringResource(id = R.string.user_home_summary_weekly_label))
                TrainingSummaryProgress(text = "10/20")
                TrainingSummaryProgress(text = "5:00/7:00")
                TrainingSummaryProgress(text = "3/5")
            }
            Column(modifier = Modifier.weight(1f)) {
                TrainingSummaryLabel(stringResource(id = R.string.user_home_summary_monthly_label))
                TrainingSummaryProgress(text = "10/20")
                TrainingSummaryProgress(text = "5:00/7:00")
                TrainingSummaryProgress(text = "3/5")
            }
        }
    }
}

@Composable
fun TrainingSummaryCell(content: @Composable BoxScope.() -> Unit) = Box(
    modifier = Modifier.padding(vertical = 4.dp),
    content = content
)

@Composable
fun TrainingSummaryLabel(text: String) = TrainingSummaryCell {
    Text(
        text = text,
        style = MaterialTheme.typography.subtitle1,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun TrainingSummaryProgress(text: String) = TrainingSummaryCell {
    Text(
        text = text,
        style = MaterialTheme.typography.subtitle1
    )
}

@Composable
private fun UserProfileHeader(screenState: UserHomeViewModel.ScreenState.StatsAvailable) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)
    ) {
        UserProfileImage(photoUrl = screenState.userPhotoUrl)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = screenState.userName, style = MaterialTheme.typography.h6)
            Text(
                text = screenState.userRecentPlace ?: "",
                style = MaterialTheme.typography.subtitle1
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        OutlinedButton(onClick = { /*TODO*/ }) {
            Text(text = stringResource(id = R.string.user_home_edit_profile_button))
        }
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
fun UserProfileImage(
    photoUrl: String?,
    imageLoadSizeDp: Dp = 75.dp,
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
fun PreviewUserHome() {
    UserHome(
        contentPadding = PaddingValues(),
        screenState = UserHomeViewModel.ScreenState.StatsAvailable(
            "My Name",
            "photoUrl",
            "Saigon, Vietnam",
            listOf(),
            listOf()
        )
    )
}

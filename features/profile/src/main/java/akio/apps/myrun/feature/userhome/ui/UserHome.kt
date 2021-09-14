package akio.apps.myrun.feature.userhome.ui

import akio.apps.myrun.feature.base.navigation.HomeNavDestination
import akio.apps.myrun.feature.base.ui.AppColors
import akio.apps.myrun.feature.base.ui.AppDimensions
import akio.apps.myrun.feature.base.ui.CentralLoadingView
import akio.apps.myrun.feature.base.ui.ColumnSpacer
import akio.apps.myrun.feature.base.ui.RowSpacer
import akio.apps.myrun.feature.base.ui.StatusBarSpacer
import akio.apps.myrun.feature.profile.R
import akio.apps.myrun.feature.userhome.UserHomeViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.Divider
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.size.Scale

@Composable
fun UserHome(
    userHomeViewModel: UserHomeViewModel,
    contentPadding: PaddingValues,
    appNavController: NavController,
) {
    val screenState by userHomeViewModel.screenState.collectAsState(initial = null)
    UserHome(screenState ?: return, contentPadding, appNavController)
}

@Composable
private fun UserHome(
    screenState: UserHomeViewModel.ScreenState = UserHomeViewModel.ScreenState.StatsLoading,
    contentPadding: PaddingValues,
    appNavController: NavController,
) {
    Column {
        StatusBarSpacer()
        UserHomeTopBar()
        when (screenState) {
            UserHomeViewModel.ScreenState.StatsLoading -> {
                CentralLoadingView(text = stringResource(id = R.string.message_loading))
            }
            is UserHomeViewModel.ScreenState.StatsAvailable -> {
                UserHomeContent(screenState, modifier = Modifier.weight(1f), appNavController)
            }
        }
        Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding()))
    }
}

@Composable
fun UserHomeContent(
    screenState: UserHomeViewModel.ScreenState.StatsAvailable,
    modifier: Modifier = Modifier,
    appNavController: NavController,
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(AppDimensions.screenVerticalSpacing))
        UserProfileHeader(screenState, appNavController)
        Spacer(modifier = Modifier.height(AppDimensions.sectionVerticalSpacing))
        TrainingSummaryTable()
    }
}

@Composable
fun TrainingSummaryTable() {
    Column {
        ColumnSpacer(height = AppDimensions.rowVerticalPadding)
        SectionTitle(text = stringResource(id = R.string.user_home_summary_title))
        Row {
            Column(modifier = Modifier.weight(1f)) {
                TrainingSummaryLabel(text = "\n")
                Divider()
                TrainingSummaryLabel(stringResource(id = R.string.user_home_summary_distance_label))
                Divider()
                TrainingSummaryLabel(stringResource(id = R.string.user_home_summary_pace_label))
                Divider()
                TrainingSummaryLabel(
                    stringResource(id = R.string.user_home_summary_activities_label)
                )
                Divider()
            }
            Column(modifier = Modifier.weight(1f)) {
                TrainingSummaryLabel(stringResource(id = R.string.user_home_summary_weekly_label))
                Divider()
                TrainingSummaryProgress(text = "10/20")
                Divider()
                TrainingSummaryProgress(text = "5:00/7:00")
                Divider()
                TrainingSummaryProgress(text = "3/5")
                Divider()
            }
            Column(modifier = Modifier.weight(1f)) {
                TrainingSummaryLabel(stringResource(id = R.string.user_home_summary_monthly_label))
                Divider()
                TrainingSummaryProgress(text = "10/20")
                Divider()
                TrainingSummaryProgress(text = "5:00/7:00")
                Divider()
                TrainingSummaryProgress(text = "3/5")
                Divider()
            }
        }
        ColumnSpacer(height = AppDimensions.sectionVerticalSpacing)
        SectionTitle(text = stringResource(id = R.string.user_home_route_section_title))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        modifier = Modifier.padding(start = AppDimensions.screenHorizontalPadding),
        text = text,
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun TrainingSummaryCell(content: @Composable (BoxScope.() -> Unit)) = Box(
    modifier = Modifier
        .fillMaxWidth()
//        .border(0.5.dp, Color.Gray.copy(alpha = 0.5f))
        .padding(vertical = 12.dp),
    content = content,
    contentAlignment = Alignment.Center
)

@Composable
fun TrainingSummaryLabel(text: String) = TrainingSummaryCell {
    Text(
        text = text,
        style = MaterialTheme.typography.body2,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
fun TrainingSummaryProgress(text: String) = TrainingSummaryCell {
    Text(
        text = text,
        style = MaterialTheme.typography.body2,
        fontSize = 16.sp,
    )
}

@Composable
private fun UserProfileHeader(
    screenState: UserHomeViewModel.ScreenState.StatsAvailable,
    appNavController: NavController,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = AppDimensions.screenHorizontalPadding)
    ) {
        UserProfileImage(photoUrl = screenState.userPhotoUrl)
        RowSpacer(width = 10.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = screenState.userName,
                style = MaterialTheme.typography.h6,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )
            ColumnSpacer(height = 4.dp)
            Text(
                text = screenState.userRecentPlace ?: "",
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Normal
            )
        }
        RowSpacer(width = 10.dp)
        OutlinedButton(
            shape = RoundedCornerShape(3.dp),
            onClick = {
                appNavController.navigate(HomeNavDestination.Profile.routeWithUserId())
            },
            border = BorderStroke(1.dp, AppColors.primary),
            modifier = Modifier.size(width = 50.dp, height = 30.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.user_home_edit_profile_button),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Bold
            )
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
    imageLoadSizeDp: Dp = 60.dp,
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
        screenState = UserHomeViewModel.ScreenState.StatsAvailable(
            "My Name",
            "photoUrl",
            "Saigon, Vietnam",
            listOf(),
            listOf()
        ),
        contentPadding = PaddingValues(),
        rememberNavController()
    )
}

package akio.apps.myrun.feature.activitydetail.ui

import akio.apps.myrun.R
import akio.apps.myrun.feature.activitydetail.impl.ActivityDetailViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.ui.theme.AppTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarData
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Share
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun ActivityDetailScreen(
    activityDetailViewModel: ActivityDetailViewModel,
    onClickRouteImage: (encodedPolyline: String) -> Unit,
    onClickExportFile: (Activity) -> Unit,
    onClickUserAvatar: (String) -> Unit,
    onClickBackButton: () -> Unit,
) = AppTheme {
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val activityResource by activityDetailViewModel.activityDetails
        .collectAsState(akio.apps.common.data.Resource.Loading())
    val activityDisplayPlaceName by activityDetailViewModel.activityPlaceName
        .collectAsState(initial = null)
    val activityDetail = activityResource.data
    val activityName = activityDetail?.name
    Scaffold(
        topBar = {
            ActivityDetailTopBar(
                activityName,
                onClickBackButton,
                { if (activityDetail != null) onClickExportFile(activityDetail) }
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = { hostState ->
            SnackbarHost(hostState) { snackbarData ->
                LoadingErrorSnackbar(snackbarData)
            }
        }
    ) {
        if (activityDetail != null) {
            Column {
                ActivityInfoHeaderView(
                    activityDetail,
                    activityDisplayPlaceName
                ) { onClickUserAvatar(activityDetail.athleteInfo.userId) }
                ActivityRouteImage(activityDetail) {
                    onClickRouteImage(activityDetail.encodedPolyline)
                }
                PerformanceTableComposable(activityDetail)
                if (activityResource is akio.apps.common.data.Resource.Loading) {
                    BottomLoadingIndicator()
                }
            }
        } else if (activityResource is akio.apps.common.data.Resource.Loading) {
            FullscreenLoadingView()
        }

        if (activityResource is akio.apps.common.data.Resource.Error) {
            val errorMessage = stringResource(id = R.string.activity_details_loading_error)
            val retryLabel = stringResource(id = R.string.action_retry)
            coroutineScope.launch {
                val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                    message = errorMessage,
                    actionLabel = retryLabel,
                    duration = SnackbarDuration.Long
                )

                if (snackbarResult == SnackbarResult.ActionPerformed) {
                    activityDetailViewModel.loadActivityDetails()
                }
            }
        }
    }
}

@Composable
private fun ActivityDetailTopBar(
    activityName: String?,
    onClickBackButton: () -> Unit,
    onClickExportFile: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = activityName ?: "") },
        actions = {
            if (activityName != null) {
                ShareActionMenu(onClickExportFile)
            }
        },
        navigationIcon = {
            IconButton(onClick = onClickBackButton) {
                Icon(imageVector = Icons.Sharp.ArrowBack, contentDescription = "Back button")
            }
        }
    )
}

@Composable
private fun ShareActionMenu(onClickExportFile: () -> Unit) {
    Box {
        var isExpanded by remember { mutableStateOf(false) }
        IconButton(
            onClick = {
                isExpanded = !isExpanded
            }
        ) {
            Icon(
                imageVector = Icons.Sharp.Share,
                contentDescription = "Share action menu"
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
                Text(
                    text = stringResource(
                        id = R.string.activity_details_share_menu_item_export_file
                    )
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.BottomLoadingIndicator() = Box(
    modifier = Modifier.weight(1.0f),
    contentAlignment = Alignment.BottomCenter
) {
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun LoadingErrorSnackbar(snackbarData: SnackbarData) {
    val snackbarBackgroundColor = MaterialTheme.colors.error
    val snackbarContentColor = contentColorFor(backgroundColor = snackbarBackgroundColor)
    Snackbar(
        backgroundColor = snackbarBackgroundColor,
        contentColor = snackbarContentColor,
        actionColor = snackbarContentColor,
        snackbarData = snackbarData
    )
}

@Composable
private fun FullscreenLoadingView() = Column(
    modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = stringResource(id = R.string.activity_details_loading_message),
        color = colorResource(id = R.color.user_timeline_instruction_text),
        fontSize = 15.sp
    )
}

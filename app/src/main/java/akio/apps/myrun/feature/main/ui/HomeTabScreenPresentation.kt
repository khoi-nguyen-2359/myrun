package akio.apps.myrun.feature.main.ui

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.tracking.api.RouteTrackingState
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus
import akio.apps.myrun.feature.main.di.HomeTabFeatureComponent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController

internal val LocalRouteTrackingState = staticCompositionLocalOf<RouteTrackingState> { error("") }

@Composable
fun HomeTabScreenPresentation(
    appNavController: NavController,
    onClickFloatingActionButton: () -> Unit,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
    openRoutePlanningAction: () -> Unit,
    homeTabFeatureComponent: HomeTabFeatureComponent,
) {
    CompositionLocalProvider(
        LocalRouteTrackingState provides homeTabFeatureComponent.routeTrackingState()
    ) {
        HomeTabScreen(
            appNavController,
            onClickFloatingActionButton,
            onClickExportActivityFile,
            openRoutePlanningAction
        )
    }
}

@Composable
internal fun isTrackingStarted(): Boolean {
    val routeTrackingState = LocalRouteTrackingState.current
    var isTrackingStarted by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        routeTrackingState.getTrackingStatusFlow().collect {
            isTrackingStarted = it != RouteTrackingStatus.STOPPED
        }
    }
    return isTrackingStarted
}

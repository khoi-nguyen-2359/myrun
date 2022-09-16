package akio.apps.myrun.feature.core.screen

import akio.apps.myrun.feature.core.R
import akio.apps.myrun.feature.core.navigation.HomeNavDestination
import akio.apps.myrun.feature.core.ui.AppBarArrowBackButton
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.core.ui.StatusBarSpacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState

@Composable
fun WebViewContainer(navController: NavHostController, navEntry: NavBackStackEntry) = AppTheme {
    val title =
        HomeNavDestination.WebViewContainer.titleOptionalArg.parseValueInBackStackEntry(navEntry)
    val url =
        HomeNavDestination.WebViewContainer.urlRequiredArg.parseValueInBackStackEntry(navEntry)
            ?: ""
    Column(modifier = Modifier.fillMaxSize()) {
        StatusBarSpacer()
        TopAppBar(
            navigationIcon = { AppBarArrowBackButton(navController = navController) },
            title = {
                Text(text = title ?: stringResource(id = R.string.webview_container_default_title))
            }
        )

        val webViewState = rememberWebViewState(url)

        Box(modifier = Modifier.fillMaxSize()) {
            WebView(
                state = webViewState,
                modifier = Modifier.fillMaxSize(),
                captureBackPresses = true
            )

            if (webViewState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
            }
        }
    }
}

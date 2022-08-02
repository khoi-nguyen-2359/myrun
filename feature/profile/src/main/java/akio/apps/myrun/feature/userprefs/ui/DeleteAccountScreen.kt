package akio.apps.myrun.feature.userprefs.ui

import akio.apps.myrun.feature.core.ui.AppBarArrowBackButton
import akio.apps.myrun.feature.core.ui.AppTheme
import akio.apps.myrun.feature.core.ui.StatusBarSpacer
import akio.apps.myrun.feature.profile.R
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController

@Composable
fun DeleteAccountScreen(
    navController: NavController,
    navEntry: NavBackStackEntry,
) {
    DeleteAccountScreen(navController)
}

@Composable
private fun DeleteAccountScreen(navController: NavController) = AppTheme {
    Column(modifier = Modifier.fillMaxSize()) {
        StatusBarSpacer()
        TopAppBar(
            navigationIcon = { AppBarArrowBackButton(navController) },
            title = { Text(stringResource(id = R.string.user_prefs_delete_account_label)) }
        )

        Icon(
            Icons.Rounded.DeleteForever,
            null,
            modifier = Modifier
                .size(64.dp)
                .align(CenterHorizontally),
            tint = Color.Red
        )
    }
}

package akio.apps.myrun.feature.base.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.statusBarsHeight

@Composable
fun StatusBarSpacer() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsHeight()
            .background(AppColors.primarySurface())
    )
}

@Composable
fun NavigationBarSpacer() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsHeight()
            .background(AppColors.primarySurface())
    )
}

package akio.apps.myrun.feature.core.ui

import akio.apps.myrun.feature.core.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CentralLoadingView(
    text: String,
) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = text,
        color = colorResource(id = R.color.central_loading_text),
        fontSize = 15.sp
    )
}

@Composable
fun CentralAnnouncementView(
    text: String,
    onClick: () -> Unit,
) = Box(
    modifier = Modifier
        .fillMaxSize()
        .clickable { onClick() },
    contentAlignment = Alignment.Center
) {
    Text(
        text = text,
        color = colorResource(id = R.color.central_loading_text),
        fontSize = 15.sp
    )
}

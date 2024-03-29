package akio.apps.myrun.feature.core.ui

import akio.apps.myrun.feature.core.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun UserAvatarImage(
    imageUrl: String?,
    avatarDimension: Dp = 46.dp,
    onClickUserAvatar: (() -> Unit)? = null,
) {
    val avatarSize = with(LocalDensity.current) { avatarDimension.toPx() }
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(data = imageUrl.orEmpty())
                .apply {
                    size(avatarSize.toInt())
                        .placeholder(R.drawable.common_avatar_placeholder_image)
                        .error(R.drawable.common_avatar_placeholder_image)
                }
                .build()
        ),
        contentDescription = "Athlete avatar",
        modifier = Modifier
            .size(avatarDimension)
            .clip(CircleShape)
            .clickable(enabled = onClickUserAvatar != null) { onClickUserAvatar?.invoke() }
    )
}

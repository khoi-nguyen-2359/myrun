package akio.apps.myrun.feature.main.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class HomeTabFabState(
    val coroutineScope: CoroutineScope,
    val fabOffsetYAnimatable: Animatable<Float, AnimationVector1D>,
    val fabBoxHeightDp: Dp,
    val fabBoxHeightPx: Float,
    // FAB is inactive when user selects a tab other than Feed
    val isFabActive: Boolean,
) {
    val fabAnimationScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (!isFabActive) {
                return Offset.Zero
            }
            val delta = available.y
            val targetFabOffsetY = when {
                delta >= REVEAL_ANIM_THRESHOLD -> 0f // reveal (move up)
                delta <= -REVEAL_ANIM_THRESHOLD -> fabBoxHeightPx // go away (move down)
                else -> return Offset.Zero
            }
            coroutineScope.launch {
                fabOffsetYAnimatable.animateTo(targetFabOffsetY)
            }
            return Offset.Zero
        }
    }

    suspend fun toggleFabAnimation() {
        when {
            isFabActive && fabOffsetY != 0f -> {
                fabOffsetYAnimatable.animateTo(0f)
            }
            !isFabActive && fabOffsetY != fabBoxHeightPx -> {
                fabOffsetYAnimatable.animateTo(fabBoxHeightPx)
            }
        }
    }

    private val fabOffsetY: Float
        get() = fabOffsetYAnimatable.value

    companion object {
        private const val REVEAL_ANIM_THRESHOLD = 10
    }
}

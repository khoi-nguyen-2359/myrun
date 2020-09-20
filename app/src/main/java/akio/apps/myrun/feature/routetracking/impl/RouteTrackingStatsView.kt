package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.R
import akio.apps.myrun.databinding.MergeRouteTrackingStatsViewBinding
import akio.apps.myrun.feature._base.utils.StatsPresentations
import akio.apps.myrun.feature.routetracking.RouteTrackingStats
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import timber.log.Timber

class RouteTrackingStatsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val viewBinding: MergeRouteTrackingStatsViewBinding =
        MergeRouteTrackingStatsViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        val verticalPadding = resources.getDimensionPixelSize(R.dimen.common_page_vertical_padding)
        setPadding(paddingLeft, verticalPadding, paddingRight, verticalPadding)
    }

    fun update(stats: RouteTrackingStats) = viewBinding.apply {
        Timber.d(stats.toString())
        distanceTextView.text = StatsPresentations.getDisplayTrackingDistance(stats.distance)
        timeTextView.text = StatsPresentations.getDisplayDuration(stats.duration)
        instantSpeedTextView.text = StatsPresentations.getDisplaySpeed(stats.speed)
    }
}
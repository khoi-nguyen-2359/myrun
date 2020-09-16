package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.R
import akio.apps.myrun.databinding.MergeRouteTrackingStatsViewBinding
import akio.apps.myrun.feature._base.StatsPresentation
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout

class RouteTrackingStatsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val viewBinding: MergeRouteTrackingStatsViewBinding =
        MergeRouteTrackingStatsViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        val verticalPadding = resources.getDimensionPixelSize(R.dimen.common_page_vertical_padding)
        setPadding(paddingLeft, verticalPadding, paddingRight, verticalPadding)
    }

    fun update(distanceInMeter: Double, totalSec: Long) = viewBinding.apply {
        distanceTextView.text = StatsPresentation.getDisplayTrackingDistance(distanceInMeter)

        timeTextView.text = StatsPresentation.getDisplayDuration(totalSec)

        val hours = totalSec / 3600.0
        val distanceInKm = distanceInMeter / 1000
        val speed = if (distanceInKm == 0.0) 0.0 else distanceInKm / hours
        speedTextView.text = StatsPresentation.getDisplaySpeed(speed)
    }
}
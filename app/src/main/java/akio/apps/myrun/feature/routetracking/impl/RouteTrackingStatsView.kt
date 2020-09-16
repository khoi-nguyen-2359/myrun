package akio.apps.myrun.feature.routetracking.impl

import akio.apps.common.view.getDrawableCompat
import akio.apps.myrun.R
import akio.apps.myrun.databinding.MergeRouteTrackingStatsViewBinding
import akio.apps.myrun.feature.common.MapPresentationUtils
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout

class RouteTrackingStatsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val viewBinding: MergeRouteTrackingStatsViewBinding =
        MergeRouteTrackingStatsViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        dividerDrawable =
            context.getDrawableCompat(R.drawable.route_tracking_stats_view_vertical_divider)
        showDividers = SHOW_DIVIDER_MIDDLE

        val verticalPadding = resources.getDimensionPixelSize(R.dimen.common_page_vertical_padding)
        setPadding(paddingLeft, verticalPadding, paddingRight, verticalPadding)
    }

    fun update(distanceInMeter: Double, totalSec: Long) = viewBinding.apply {
        distanceTextView.text = MapPresentationUtils.getDisplayTrackingDistance(distanceInMeter)

        timeTextView.text = MapPresentationUtils.getDisplayDuration(totalSec)

        val hours = totalSec / 3600.0
        val distanceInKm = distanceInMeter / 1000
        val speed = if (distanceInKm == 0.0) 0.0 else distanceInKm / hours
        speedTextView.text = MapPresentationUtils.getDisplaySpeed(speed)
    }
}
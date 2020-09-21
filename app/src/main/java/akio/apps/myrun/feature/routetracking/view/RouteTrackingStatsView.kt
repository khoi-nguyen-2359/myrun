package akio.apps.myrun.feature.routetracking.view

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.databinding.MergeRouteTrackingStatsViewBinding
import akio.apps.myrun.feature._base.utils.StatsPresentations
import akio.apps.myrun.feature.routetracking.model.RouteTrackingStats
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

    private var speedPresenter: ((Double) -> String)? = null

    private val activityTypes = listOf(ActivityType.Running, ActivityType.Cycling)

    init {
        val verticalPadding = resources.getDimensionPixelSize(R.dimen.common_page_vertical_padding)
        setPadding(paddingLeft, verticalPadding, paddingRight, verticalPadding)
        readAttrs(attrs)
    }

    private fun readAttrs(attrs: AttributeSet?) {
        attrs ?: return

        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.RouteTrackingStatsView)
        val activityTypeIndex = styledAttrs.getInteger(R.styleable.RouteTrackingStatsView_activityTypeIndex, 0)
        styledAttrs.recycle()

        setActivityType(activityTypes[activityTypeIndex])
    }

    fun setActivityType(activityType: ActivityType) {
        when (activityType) {
            ActivityType.Running -> {
                viewBinding.speedLabelTextView.setText(R.string.route_tracking_pace_label)
                viewBinding.speedUnitTextView.setText(R.string.common_pace_unit)
                speedPresenter = { speed -> StatsPresentations.getDisplayPace(speed) }
            }
            ActivityType.Cycling -> {
                viewBinding.speedLabelTextView.setText(R.string.route_tracking_speed_label)
                viewBinding.speedUnitTextView.setText(R.string.common_speed_unit)
                speedPresenter = { speed -> StatsPresentations.getDisplaySpeed(speed) }
            }
        }
    }

    fun update(stats: RouteTrackingStats) = viewBinding.apply {
        Timber.d(stats.toString())
        distanceTextView.text = StatsPresentations.getDisplayTrackingDistance(stats.distance)
        timeTextView.text = StatsPresentations.getDisplayDuration(stats.duration)
        speedTextView.text = speedPresenter?.invoke(stats.speed)
    }
}
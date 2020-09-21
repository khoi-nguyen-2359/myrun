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

    var activityType: ActivityType = ActivityType.Running
        set(value) {
            field = value
            onActivityTypeChanged()
        }

    init {
        val verticalPadding = resources.getDimensionPixelSize(R.dimen.common_page_vertical_padding)
        setPadding(paddingLeft, verticalPadding, paddingRight, verticalPadding)
        readAttrs(attrs)
    }

    private fun readAttrs(attrs: AttributeSet?) {
        attrs ?: return

        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.RouteTrackingStatsView)
        val activityTypeValue = styledAttrs.getInteger(R.styleable.RouteTrackingStatsView_activityType, ACTIVITY_TYPE_RUNNING)
        styledAttrs.recycle()

        this.activityType = ActivityType.valueOf(activityTypeValue)
    }

    private fun onActivityTypeChanged() {
        when (this.activityType) {
            ActivityType.Running -> {
                viewBinding.speedLabelTextView.setText(R.string.route_tracking_pace_label)
                viewBinding.speedUnitTextView.setText(R.string.common_pace_unit)
            }
            ActivityType.Cycling -> {
                viewBinding.speedLabelTextView.setText(R.string.route_tracking_speed_label)
                viewBinding.speedUnitTextView.setText(R.string.route_tracking_speed_label)
            }
        }
    }

    fun update(stats: RouteTrackingStats) = viewBinding.apply {
        Timber.d(stats.toString())
        distanceTextView.text = StatsPresentations.getDisplayTrackingDistance(stats.distance)
        timeTextView.text = StatsPresentations.getDisplayDuration(stats.duration)

        when (activityType) {
            ActivityType.Cycling -> speedTextView.text = StatsPresentations.getDisplaySpeed(stats.speed)
            ActivityType.Running -> speedTextView.text = StatsPresentations.getDisplayPace(stats.speed)
        }
    }

    companion object {
        // This maps with RouteTrackingStatsView_activityType
        const val ACTIVITY_TYPE_RUNNING = 0
        const val ACTIVITY_TYPE_CYCLING = 1
    }

    enum class ActivityType(val value: Int) {
        Running(ACTIVITY_TYPE_RUNNING), Cycling(ACTIVITY_TYPE_CYCLING);

        companion object {
            fun valueOf(value: Int): ActivityType {
                return when (value) {
                    ACTIVITY_TYPE_RUNNING -> Running
                    ACTIVITY_TYPE_CYCLING -> Cycling
                    else -> throw IllegalArgumentException("Unknown activity type $value")
                }
            }
        }
    }
}
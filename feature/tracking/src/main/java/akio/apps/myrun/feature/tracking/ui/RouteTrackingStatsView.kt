package akio.apps.myrun.feature.tracking.ui

import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.feature.tracking.R
import akio.apps.myrun.feature.tracking.StatsPresentations
import akio.apps.myrun.feature.tracking.model.RouteTrackingStats
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

internal class RouteTrackingStatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val distanceTextView: TextView by lazy { findViewById(R.id.distance_text_view) }
    private val timeTextView: TextView by lazy { findViewById(R.id.time_text_view) }
    private val speedTextView: TextView by lazy { findViewById(R.id.speed_text_view) }
    private val speedLabelTextView: TextView by lazy { findViewById(R.id.speed_label_text_view) }
    private val speedUnitTextView: TextView by lazy { findViewById(R.id.speed_unit_text_view) }
    private var speedPresenter: ((Double) -> String)? = null

    private val activityTypes = listOf(ActivityType.Running, ActivityType.Cycling)

    init {
        LayoutInflater.from(context).inflate(R.layout.merge_route_tracking_stats_view, this, true)

        readAttrs(attrs)
    }

    private fun readAttrs(attrs: AttributeSet?) {
        attrs ?: return

        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.RouteTrackingStatsView)
        val activityTypeIndex =
            styledAttrs.getInteger(R.styleable.RouteTrackingStatsView_activityTypeIndex, 0)
        styledAttrs.recycle()

        setActivityType(activityTypes[activityTypeIndex])
    }

    fun setActivityType(activityType: ActivityType) {
        when (activityType) {
            ActivityType.Running -> {
                speedLabelTextView.setText(R.string.route_tracking_pace_label)
                speedUnitTextView.setText(R.string.performance_unit_pace_min_per_km)
                speedPresenter = { speed -> StatsPresentations.getDisplayPace(speed) }
            }
            ActivityType.Cycling -> {
                speedLabelTextView.setText(R.string.route_tracking_speed_label)
                speedUnitTextView.setText(R.string.common_speed_unit)
                speedPresenter = { speed -> StatsPresentations.getDisplaySpeed(speed) }
            }
            else -> throw Exception("INvalid activity type")
        }
    }

    fun update(stats: RouteTrackingStats) {
        distanceTextView.text = StatsPresentations.getDisplayTrackingDistance(stats.distance)
        timeTextView.text = StatsPresentations.getDisplayDuration(stats.duration)
        speedTextView.text = speedPresenter?.invoke(stats.speed)
    }
}

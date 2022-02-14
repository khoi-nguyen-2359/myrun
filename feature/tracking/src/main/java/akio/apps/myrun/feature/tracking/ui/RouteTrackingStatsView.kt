package akio.apps.myrun.feature.tracking.ui

import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.feature.core.measurement.TrackValueFormatter
import akio.apps.myrun.feature.core.measurement.TrackValueFormatPreference
import akio.apps.myrun.feature.tracking.R
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
    private val distanceUnitTextView: TextView by lazy { findViewById(R.id.distance_unit_text_view) }
    private val timeTextView: TextView by lazy { findViewById(R.id.time_text_view) }
    private val speedTextView: TextView by lazy { findViewById(R.id.speed_text_view) }
    private val speedLabelTextView: TextView by lazy { findViewById(R.id.speed_label_text_view) }
    private val speedUnitTextView: TextView by lazy { findViewById(R.id.speed_unit_text_view) }
    private var speedPresenter: ((Double) -> String)? = null

    private val activityTypes = listOf(ActivityType.Running, ActivityType.Cycling)

    // TODO: Clean this up when moving this view to compose
    var trackValueFormatPreference: TrackValueFormatPreference =
        TrackValueFormatter.createFormatterPreference(MeasureSystem.Metric)

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

    private fun setActivityType(activityType: ActivityType) {
        val (_, paceFormatter, speedFormatter, _) = trackValueFormatPreference
        speedPresenter = when (activityType) {
            ActivityType.Running -> {
                speedLabelTextView.setText(paceFormatter.labelResId)
                speedUnitTextView.setText(paceFormatter.unitResId ?: 0)
                paceFormatter::getFormattedValue
            }
            ActivityType.Cycling -> {
                speedLabelTextView.setText(speedFormatter.labelResId)
                speedUnitTextView.setText(speedFormatter.unitResId ?: 0)
                speedFormatter::getFormattedValue
            }
            else -> throw Exception("Invalid activity type")
        }
    }

    fun update(stats: RouteTrackingStats) {
        val (distanceFormatter, _, _, durationFormatter) = trackValueFormatPreference
        distanceUnitTextView.text = distanceFormatter.getUnit(context)
        distanceTextView.text = distanceFormatter.getFormattedValue(stats.distance)
        timeTextView.text = durationFormatter.getFormattedValue(stats.duration)
        speedTextView.text = speedPresenter?.invoke(stats.speed)
    }
}

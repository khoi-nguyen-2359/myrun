package akio.apps.myrun.feature.routetracking.view

import akio.apps._base.lifecycle.observe
import akio.apps.myrun.R
import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.databinding.MergeActivitySettingsViewBinding
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner

class ActivitySettingsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val viewBinding = MergeActivitySettingsViewBinding.inflate(LayoutInflater.from(context), this)

    var eventListener: EventListener? = null

    private val activityTypeDisplays = mapOf(
        ActivityType.Running to ActivityTypeDisplay(R.string.activity_type_name_running),
        ActivityType.Cycling to ActivityTypeDisplay(R.string.activity_type_name_cycling)
    )

    init {
        setBackgroundColor(Color.WHITE)
        viewBinding.activityTypeSelectionTextView.setOnClickListener { showActivityTypeSelectionDialog() }
    }

    private fun showActivityTypeSelectionDialog() {
        val activityTypeNames = activityTypeDisplays
            .map { resources.getString(it.value.nameResId) }
            .toTypedArray()
        AlertDialog.Builder(context)
            .setItems(activityTypeNames) { dialog, activityTypeIndex ->
                eventListener?.onActivityTypeSelected(activityTypeDisplays.keys.elementAt(activityTypeIndex))
            }
            .show()
    }

    fun setActivityType(activityType: ActivityType) {
        val activityTypeDisplay = activityTypeDisplays[activityType]
            ?: return

        viewBinding.activityTypeSelectionTextView.text = resources.getString(R.string.route_tracking_activity_settings_activity_selection, resources.getString(activityTypeDisplay.nameResId))
    }

    class ActivityTypeDisplay(@StringRes val nameResId: Int)

    interface EventListener {
        fun onActivityTypeSelected(activityType: ActivityType)
    }
}
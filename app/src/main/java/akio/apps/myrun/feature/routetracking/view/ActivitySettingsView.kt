package akio.apps.myrun.feature.routetracking.view

import akio.apps._base.lifecycle.observe
import akio.apps.myrun.R
import akio.apps.myrun.data.workout.ActivityType
import akio.apps.myrun.databinding.MergeActivitySettingsViewBinding
import akio.apps.myrun.feature.routetracking.ActivitySettingsViewModel
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

    private lateinit var viewModel: ActivitySettingsViewModel

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
                viewModel.onSelectActivityType(activityTypeDisplays.keys.elementAt(activityTypeIndex))
            }
            .show()
    }

    private fun setActivityType(activityType: ActivityType) {
        val activityTypeDisplay = activityTypeDisplays[activityType]
            ?: return

        viewBinding.activityTypeSelectionTextView.text = resources.getString(R.string.route_tracking_activity_settings_activity_selection, resources.getString(activityTypeDisplay.nameResId))
    }

    fun bindViewModel(lifecycleOwner: LifecycleOwner, activitySettingsViewModel: ActivitySettingsViewModel) {
        viewModel = activitySettingsViewModel
        lifecycleOwner.observe(activitySettingsViewModel.activityType, ::setActivityType)
    }

    class ActivityTypeDisplay(@StringRes val nameResId: Int)
}
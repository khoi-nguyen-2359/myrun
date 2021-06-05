package akio.apps.myrun.feature.routetracking.view

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.databinding.MergeActivitySettingsViewBinding
import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

class ActivitySettingsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val viewBinding =
        MergeActivitySettingsViewBinding.inflate(LayoutInflater.from(context), this)

    var eventListener: EventListener? = null

    private val activityTypeDisplays = mapOf(
        ActivityType.Running to ActivityTypeDisplay(
            R.string.activity_type_name_running,
            R.drawable.ic_directions_run
        ),
        ActivityType.Cycling to ActivityTypeDisplay(
            R.string.activity_type_name_cycling,
            R.drawable.ic_directions_bike
        )
    )

    init {
        viewBinding.activityTypeSelectionChip.setOnClickListener {
            showActivityTypeSelectionDialog()
        }
    }

    private fun showActivityTypeSelectionDialog() {
        val activityTypeNames = activityTypeDisplays
            .map { entry ->
                val name = "  " + resources.getString(entry.value.nameRes)
                val builder = SpannableStringBuilder(name)
                val iconSpan = ImageSpan(context, entry.value.iconRes)
                builder.setSpan(iconSpan, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                builder
            }
            .toTypedArray()
        AlertDialog.Builder(context)
            .setItems(activityTypeNames) { _, activityTypeIndex ->
                eventListener?.onActivityTypeSelected(
                    activityTypeDisplays.keys.elementAt(
                        activityTypeIndex
                    )
                )
            }
            .show()
    }

    fun setActivityType(activityType: ActivityType) {
        val activityTypeDisplay = activityTypeDisplays[activityType]
            ?: return

        viewBinding.activityTypeSelectionChip.text =
            resources.getString(activityTypeDisplay.nameRes)
        viewBinding.activityTypeSelectionChip.chipIcon =
            ContextCompat.getDrawable(context, activityTypeDisplay.iconRes)
    }

    class ActivityTypeDisplay(
        @StringRes val nameRes: Int,
        @DrawableRes val iconRes: Int,
    )

    interface EventListener {
        fun onActivityTypeSelected(activityType: ActivityType)
    }
}

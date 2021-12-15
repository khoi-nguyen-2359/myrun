package akio.apps.myrun.feature.tracking.ui

import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.feature.tracking.R
import android.content.Context
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
import com.google.android.material.chip.Chip

class ActivitySettingsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {

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

    private val activityTypeSelectionChip: Chip by lazy {
        findViewById(R.id.activity_type_selection_chip)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.merge_activity_settings_view, this, true)

        activityTypeSelectionChip.setOnClickListener {
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

        activityTypeSelectionChip.text =
            resources.getString(activityTypeDisplay.nameRes)
        activityTypeSelectionChip.chipIcon =
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

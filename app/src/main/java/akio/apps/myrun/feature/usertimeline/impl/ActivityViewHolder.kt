package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.common.feature.ui.getResources
import akio.apps.common.feature.ui.getString
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.circleCenterCrop
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.databinding.ItemUserTimelineActivityBinding
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingStats
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class ActivityViewHolder(
    private val viewBinding: ItemUserTimelineActivityBinding,
    private val timeFormatter: SimpleDateFormat,
    private val dateFormatter: SimpleDateFormat,
    private val activityTypeNameMap: Map<ActivityType, Int>,
    private val selectItemAction: (ActivityModel) -> Unit,
) : RecyclerView.ViewHolder(viewBinding.root) {

    fun bind(activity: ActivityModel) = viewBinding.apply {
        Glide.with(itemView)
            .load(activity.athleteInfo.userAvatar)
            .override(getResources().getDimensionPixelSize(R.dimen.user_timeline_avatar_size))
            .placeholder(R.drawable.common_avatar_placeholder_image)
            .circleCenterCrop()
            .into(athleteAvatarImageView)

        athleteNameTextView.text = activity.athleteInfo.userName

        Glide.with(itemView)
            .load(activity.routeImage)
            .placeholder(R.drawable.ic_run_circle)
            .into(runningActivityImage)

        val speed = activity.distance / activity.duration

        activityStatsView.setActivityType(activity.activityType)
        activityStatsView.update(RouteTrackingStats(activity.distance, speed, activity.duration))

        val calendar = Calendar.getInstance()
        val todayDate = TimeUnit.MILLISECONDS.toDays(calendar.timeInMillis)
        val activityDate = TimeUnit.MILLISECONDS.toDays(activity.startTime)
        when {
            activityDate == todayDate -> {
                activityTimeTextView.text = getString(
                    R.string.item_activity_time_today,
                    timeFormatter.format(Date(activity.startTime))
                )
            }
            todayDate - activityDate == 1L -> {
                activityTimeTextView.text = getString(
                    R.string.item_activity_time_yesterday,
                    timeFormatter.format(Date(activity.startTime))
                )
            }
            else -> {
                val formattedTime = timeFormatter.format(Date(activity.startTime))
                val formattedDate = dateFormatter.format(Date(activity.startTime))
                activityTimeTextView.text = "$formattedDate $formattedTime"
            }
        }

        calendar.timeInMillis = activity.startTime
        when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> activityTitleTextView.text = getString(
                R.string.item_activity_title_morning,
                getString(activityTypeNameMap[activity.activityType] ?: 0)
            )
            in 12..16 -> activityTitleTextView.text = getString(
                R.string.item_activity_title_afternoon,
                getString(activityTypeNameMap[activity.activityType] ?: 0)
            )
            in 17..24, in 0..4 -> activityTitleTextView.text = getString(
                R.string.item_activity_title_evening,
                getString(activityTypeNameMap[activity.activityType] ?: 0)
            )
        }

        root.setOnClickListener { selectItemAction(activity) }
    }
}

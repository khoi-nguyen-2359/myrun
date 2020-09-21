package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.databinding.ItemUserTimelineActivityBinding
import akio.apps.myrun.feature.routetracking.model.RouteTrackingStats
import akio.apps.myrun.feature.usertimeline.Activity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ActivityViewHolder(
    private val viewBinding: ItemUserTimelineActivityBinding
) : RecyclerView.ViewHolder(viewBinding.root) {

    fun bind(activity: Activity) {
        Glide.with(itemView)
            .load(activity.routeImage)
            .placeholder(R.drawable.ic_run_circle)
            .into(viewBinding.runningActivityImage)

        val speed = activity.distance / activity.duration
        viewBinding.runningActivityStatsView.setActivityType(activity.activityType)
        viewBinding.runningActivityStatsView.update(RouteTrackingStats(activity.distance, speed, activity.duration))
    }
}
package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.myrun.R
import akio.apps.myrun.databinding.ItemUserTimelineRunningActivityBinding
import akio.apps.myrun.feature.usertimeline.RunningActivity
import akio.apps.myrun.feature.routetracking.model.RouteTrackingStats
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RunningActivityViewHolder(
    private val viewBinding: ItemUserTimelineRunningActivityBinding
) : RecyclerView.ViewHolder(viewBinding.root) {

    fun bind(runningActivity: RunningActivity) {
        Glide.with(itemView)
            .load(runningActivity.routePhoto)
            .placeholder(R.drawable.ic_run_circle)
            .into(viewBinding.runningActivityImage)

        val speed = runningActivity.distance / runningActivity.duration
        viewBinding.runningActivityStatsView.update(RouteTrackingStats(runningActivity.distance, speed, runningActivity.duration))
    }
}
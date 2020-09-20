package akio.apps.myrun.feature.myworkout.impl

import akio.apps.myrun.R
import akio.apps.myrun.databinding.ItemRunningWorkoutBinding
import akio.apps.myrun.feature.myworkout.model.RunningWorkout
import akio.apps.myrun.feature.routetracking.model.RouteTrackingStats
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RunningWorkoutViewHolder(
    private val viewBinding: ItemRunningWorkoutBinding
) : RecyclerView.ViewHolder(viewBinding.root) {

    fun bind(runningWorkout: RunningWorkout) {
        Glide.with(itemView)
            .load(runningWorkout.routePhoto)
            .placeholder(R.drawable.ic_run_circle)
            .into(viewBinding.runningWorkoutImage)

        val distanceInKm = runningWorkout.distance / 1000
        val duration = runningWorkout.duration
        val durationInHour = duration / (1000.0 * 60 * 60)
        val speed = distanceInKm / durationInHour
        viewBinding.runningWorkoutStatsView.update(RouteTrackingStats(runningWorkout.distance, speed, duration))
    }
}
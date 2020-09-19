package akio.apps.myrun.feature.myworkout.impl

import akio.apps.myrun.data.workout.model.ActivityType
import akio.apps.myrun.databinding.ItemRunningWorkoutBinding
import akio.apps.myrun.feature.myworkout.model.RunningWorkout
import akio.apps.myrun.feature.myworkout.model.Workout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers

class WorkoutPagingAdapter : PagingDataAdapter<Workout, RecyclerView.ViewHolder>(WORKOUT_COMPARATOR, Dispatchers.Main, Dispatchers.Default) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_RUNNING_WORKOUT -> RunningWorkoutViewHolder(ItemRunningWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalArgumentException("[WorkoutAdapter] Unknown activity type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RunningWorkoutViewHolder -> holder.bind(getItem(position) as RunningWorkout)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)?.activityType) {
            ActivityType.Running -> VIEW_TYPE_RUNNING_WORKOUT
            else -> throw IllegalArgumentException("[WorkoutAdapter] Unknown activity type")
        }
    }

    companion object {
        const val VIEW_TYPE_RUNNING_WORKOUT = 1

        private val WORKOUT_COMPARATOR = object : DiffUtil.ItemCallback<Workout>() {
            override fun areItemsTheSame(oldItem: Workout, newItem: Workout): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Workout, newItem: Workout): Boolean {
                return oldItem == newItem
            }
        }
    }
}

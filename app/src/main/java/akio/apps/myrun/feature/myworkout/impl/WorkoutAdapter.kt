package akio.apps.myrun.feature.myworkout.impl

import akio.apps.myrun.data.workout.dto.ActivityType
import akio.apps.myrun.databinding.ItemRunningWorkoutBinding
import akio.apps.myrun.feature.myworkout.model.Workout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class WorkoutAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listDiffer: AsyncListDiffer<Workout> = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_RUNNING_WORKOUT -> RunningWorkoutViewHolder(ItemRunningWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalArgumentException("[WorkoutAdapter] Unknown activity type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RunningWorkoutViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem<Workout>(position).activityType) {
            ActivityType.Running -> VIEW_TYPE_RUNNING_WORKOUT
            else -> throw IllegalArgumentException("[WorkoutAdapter] Unknown activity type")
        }
    }

    private fun <T : Workout> getItem(position: Int): T = listDiffer.currentList[position] as T

    override fun getItemCount(): Int {
        return listDiffer.currentList.size
    }

    fun submitList(list: List<Workout>) {
        listDiffer.submitList(list)
    }

    companion object {
        const val VIEW_TYPE_RUNNING_WORKOUT = 1

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Workout>() {
            override fun areItemsTheSame(oldItem: Workout, newItem: Workout): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Workout, newItem: Workout): Boolean {
                return oldItem == newItem
            }
        }
    }
}

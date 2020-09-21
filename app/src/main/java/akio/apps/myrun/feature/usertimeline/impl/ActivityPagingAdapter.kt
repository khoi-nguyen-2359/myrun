package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.databinding.ItemUserTimelineRunningActivityBinding
import akio.apps.myrun.feature.usertimeline.RunningActivity
import akio.apps.myrun.feature.usertimeline.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers

class ActivityPagingAdapter : PagingDataAdapter<Activity, RecyclerView.ViewHolder>(ACTIVITY_COMPARATOR, Dispatchers.Main, Dispatchers.Default) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_RUNNING -> RunningActivityViewHolder(ItemUserTimelineRunningActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalArgumentException("Unknown activity type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RunningActivityViewHolder -> holder.bind(getItem(position) as RunningActivity)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)?.activityType) {
            ActivityType.Running -> VIEW_TYPE_RUNNING
            else -> throw IllegalArgumentException("Unknown activity type")
        }
    }

    companion object {
        const val VIEW_TYPE_RUNNING = 1

        private val ACTIVITY_COMPARATOR = object : DiffUtil.ItemCallback<Activity>() {
            override fun areItemsTheSame(oldItem: Activity, newItem: Activity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Activity, newItem: Activity): Boolean {
                return oldItem == newItem
            }
        }
    }
}

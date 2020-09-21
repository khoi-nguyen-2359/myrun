package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.myrun.databinding.ItemUserTimelineActivityBinding
import akio.apps.myrun.feature.usertimeline.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.Dispatchers

class ActivityPagingAdapter : PagingDataAdapter<Activity, ActivityViewHolder>(ACTIVITY_COMPARATOR, Dispatchers.Main, Dispatchers.Default) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        return ActivityViewHolder(ItemUserTimelineActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(getItem(position) as Activity)
    }

    companion object {
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

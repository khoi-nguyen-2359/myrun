package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.myrun.databinding.ItemUserTimelineLoadStateBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import timber.log.Timber

class ActivityLoadStateAdapter: LoadStateAdapter<UserTimelineLoadStateViewHolder>() {
    override fun onBindViewHolder(holder: UserTimelineLoadStateViewHolder, loadState: LoadState) {
        Timber.d("bind VH $loadState")
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): UserTimelineLoadStateViewHolder {
        return UserTimelineLoadStateViewHolder(ItemUserTimelineLoadStateBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
}
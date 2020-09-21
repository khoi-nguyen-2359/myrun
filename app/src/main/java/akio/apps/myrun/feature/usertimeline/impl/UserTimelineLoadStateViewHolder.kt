package akio.apps.myrun.feature.usertimeline.impl

import akio.apps._base.ui.setVisibleOrGone
import akio.apps.myrun.databinding.ItemUserTimelineLoadStateBinding
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView

class UserTimelineLoadStateViewHolder(private val viewBinding: ItemUserTimelineLoadStateBinding) : RecyclerView.ViewHolder(viewBinding.root) {

    fun bind(loadState: LoadState) {
        viewBinding.loadingProgressBar.setVisibleOrGone(loadState is LoadState.Loading)
    }
}

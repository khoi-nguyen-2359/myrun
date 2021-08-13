package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.common.feature.ui.setVisibleOrGone
import akio.apps.myrun.databinding.ItemUserTimelineLoadStateBinding
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView

class UserTimelineLoadStateViewHolder(
    private val viewBinding: ItemUserTimelineLoadStateBinding,
    private val retry: () -> Unit
) : RecyclerView.ViewHolder(viewBinding.root) {

    init {
        viewBinding.errorTextView.setOnClickListener { retry() }
    }

    fun bind(loadState: LoadState) {
        viewBinding.loadingProgressBar.setVisibleOrGone(loadState is LoadState.Loading)
        viewBinding.errorTextView.setVisibleOrGone(loadState is LoadState.Error)
    }
}

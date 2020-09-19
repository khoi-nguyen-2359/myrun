package akio.apps.myrun.feature.myworkout.impl

import akio.apps._base.ui.setVisibleOrGone
import akio.apps.myrun.databinding.ItemMyWorkoutLoadStateBinding
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView

class LoadStateViewHolder(private val viewBinding: ItemMyWorkoutLoadStateBinding) : RecyclerView.ViewHolder(viewBinding.root) {

    fun bind(loadState: LoadState) {
        viewBinding.myWorkoutLoadingProgressBar.setVisibleOrGone(loadState is LoadState.Loading)
    }
}

package akio.apps.myrun.feature.myworkout.impl

import akio.apps.myrun.databinding.ItemMyWorkoutLoadStateBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import timber.log.Timber

class WorkoutLoadStateAdapter: LoadStateAdapter<LoadStateViewHolder>() {
    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        Timber.d("WorkoutLoadStateAdapter bind VH $loadState")
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        return LoadStateViewHolder(ItemMyWorkoutLoadStateBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
}
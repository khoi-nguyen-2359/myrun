package akio.apps.myrun.feature.myworkout.impl

import akio.apps.myrun.feature.myworkout.MyWorkoutViewModel
import akio.apps.myrun.feature.myworkout.Workout
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import javax.inject.Inject

class MyWorkoutViewModelImpl @Inject constructor(
    private val workoutPagingSource: WorkoutPagingSource
) : MyWorkoutViewModel() {

    override val myWorkoutList: LiveData<PagingData<Workout>> = Pager(PagingConfig(pageSize = PAGE_SIZE)) { workoutPagingSource }.liveData

    companion object {
        const val PAGE_SIZE = 5
    }
}
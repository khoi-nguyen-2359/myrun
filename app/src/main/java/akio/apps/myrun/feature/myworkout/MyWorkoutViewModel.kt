package akio.apps.myrun.feature.myworkout

import akio.apps._base.viewmodel.BaseViewModel
import akio.apps.myrun.feature.myworkout.model.Workout
import androidx.lifecycle.LiveData
import androidx.paging.PagingData

abstract class MyWorkoutViewModel: BaseViewModel() {
    abstract val myWorkoutList: LiveData<PagingData<Workout>>
    abstract fun fetchMyWorkout(startAfter: Long, perPage: Int)
}
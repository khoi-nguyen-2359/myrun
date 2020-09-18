package akio.apps.myrun.feature.myworkout.impl

import akio.apps.myrun.feature.myworkout.GetWorkoutListUsecase
import akio.apps.myrun.feature.myworkout.MyWorkoutViewModel
import akio.apps.myrun.feature.myworkout.model.Workout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class MyWorkoutViewModelImpl @Inject constructor(
    private val getWorkoutListUsecase: GetWorkoutListUsecase
) : MyWorkoutViewModel() {

    private val _myWorkoutList = MutableLiveData<List<Workout>>()
    override val myWorkoutList: LiveData<List<Workout>> = _myWorkoutList

    init {
        fetchMyWorkout(System.currentTimeMillis(), 10)
    }

    override fun fetchMyWorkout(startAfter: Long, perPage: Int) {
        launchCatching {
            val pageData = getWorkoutListUsecase.getWorkoutList(startAfter, perPage)
            val currentList = _myWorkoutList.value ?: mutableListOf()
            _myWorkoutList.value = mutableListOf<Workout>().apply {
                addAll(currentList)
                addAll(pageData)
            }
        }
    }
}
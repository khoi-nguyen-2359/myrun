package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.activity.ActivityLocalStorage
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.model.ActivityModel
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

class UploadActivitiesUsecase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val activityLocalStorage: ActivityLocalStorage
) {
    suspend fun uploadAll(onUploadActivityStarted: ((ActivityModel) -> Unit)? = null): Boolean {
        var isCompleted = true
        activityLocalStorage.loadAllActivityStorageDataFlow()
            .catch { exception ->
                isCompleted = false
                Timber.e(exception)
            }
            .collect { storageData ->
                val activityModel = storageData.activityModel
                onUploadActivityStarted?.invoke(activityModel)
                activityRepository.saveActivity(
                    activityModel,
                    storageData.routeBitmapFile,
                    speedDataPoints = emptyList(),
                    stepCadenceDataPoints = emptyList(),
                    storageData.locationDataPoints
                )
                activityLocalStorage.deleteActivityData(activityModel.id)
            }

        return isCompleted
    }
}

package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activitysharing.ActivityLocalStorage
import akio.apps.myrun.data.fitness.SingleDataPoint
import akio.apps.myrun.data.location.LocationEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

class UploadActivitiesUsecase @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val activityLocalStorage: ActivityLocalStorage
) {
    suspend fun unloadAll(onUploadActivityStarted: (ActivityModel) -> Unit): Boolean {
        var isCompleted = true
        activityLocalStorage.loadAllActivityStorageDataFlow()
            .catch { isCompleted = false }
            .collect { storageData ->
                val activityModel = storageData.activityModel
                onUploadActivityStarted(activityModel)
                activityRepository.saveActivity(
                    activityModel,
                    storageData.routeBitmapFile,
                    speedDataPoints = emptyList(),
                    stepCadenceDataPoints = emptyList(),
                    storageData.locationDataPoints.map {
                        SingleDataPoint(
                            it.time,
                            LocationEntity(it.latitude, it.longitude, it.altitude)
                        )
                    }
                )
                activityLocalStorage.deleteActivityData(activityModel.id)
            }

        return isCompleted
    }
}

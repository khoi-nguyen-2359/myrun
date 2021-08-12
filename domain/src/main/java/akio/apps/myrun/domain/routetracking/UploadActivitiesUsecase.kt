package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserProfileRepository
import javax.inject.Inject
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

class UploadActivitiesUsecase @Inject constructor(
    private val authenticationState: UserAuthenticationState,
    private val activityRepository: ActivityRepository,
    private val activityLocalStorage: ActivityLocalStorage,
    private val userProfileRepository: UserProfileRepository
) {
    @OptIn(InternalCoroutinesApi::class)
    suspend fun uploadAll(onUploadActivityStarted: ((ActivityModel) -> Unit)? = null): Boolean {
        var isCompleted = true
        activityLocalStorage.loadAllActivityStorageDataFlow()
            .catch { isCompleted = false }
            .collect { storageData ->
                val activityModel = updateAthleteInfo(storageData.activityModel)
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

    private suspend fun updateAthleteInfo(activityModel: ActivityModel): ActivityModel {
        val userId = authenticationState.requireUserAccountId()
        val userProfile = userProfileRepository.getUserProfile(userId)
        return when (activityModel) {
            is RunningActivityModel ->
                activityModel.copy(
                    activityData = activityModel.activityData.copy(
                        athleteInfo = ActivityModel.AthleteInfo(
                            userId,
                            userProfile.name,
                            userProfile.photo
                        )
                    )
                )
            is CyclingActivityModel ->
                activityModel.copy(
                    activityData = activityModel.activityData.copy(
                        athleteInfo = ActivityModel.AthleteInfo(
                            userId,
                            userProfile.name,
                            userProfile.photo
                        )
                    )
                )
            else -> activityModel
        }
    }
}

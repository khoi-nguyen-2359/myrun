package akio.apps.myrun.domain.activity

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.location.api.PlaceDataSource
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.domain.user.PlaceIdentifierConverter
import javax.inject.Inject
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

class UploadActivitiesUsecase @Inject constructor(
    private val authenticationState: UserAuthenticationState,
    private val activityRepository: ActivityRepository,
    private val activityLocalStorage: ActivityLocalStorage,
    private val userProfileRepository: UserProfileRepository,
    private val placeDataSource: PlaceDataSource,
    private val placeIdentifierConverter: PlaceIdentifierConverter,
) {
    @OptIn(InternalCoroutinesApi::class)
    suspend fun uploadAll(onUploadActivityStarted: ((ActivityModel) -> Unit)? = null): Boolean {
        var isCompleted = true
        activityLocalStorage.loadAllActivityStorageDataFlow()
            .catch { isCompleted = false }
            .collect { storageData ->
                val activityModel = fillInMissingActivityInfo(
                    storageData.activityModel,
                    storageData.locationDataPoints.first()
                )
                onUploadActivityStarted?.invoke(activityModel)
                activityRepository.saveActivity(
                    activityModel,
                    storageData.routeBitmapFile,
                    speedDataPoints = emptyList(),
                    locationDataPoints = storageData.locationDataPoints,
                    stepCadenceDataPoints = emptyList()
                )
                activityLocalStorage.deleteActivityData(activityModel.id)
            }

        return isCompleted
    }

    private suspend fun fillInMissingActivityInfo(
        activityModel: ActivityModel,
        startPoint: ActivityLocation,
    ): ActivityModel {
        val userId = authenticationState.requireUserAccountId()
        val userProfile = userProfileRepository.getUserProfile(userId)
        val sortedAddressTexts = placeDataSource.getRecentPlaceAddressFromLocation(
            startPoint.latitude,
            startPoint.longitude
        )
        val placeIdentifier =
            placeIdentifierConverter.fromAddressNameList(sortedAddressTexts.map { it.name })
        return when (activityModel) {
            is RunningActivityModel ->
                activityModel.copy(
                    activityData = activityModel.activityData.copy(
                        athleteInfo = ActivityModel.AthleteInfo(
                            userId,
                            userProfile.name,
                            userProfile.photo
                        ),
                        placeIdentifier = placeIdentifier
                    ),
                )
            is CyclingActivityModel ->
                activityModel.copy(
                    activityData = activityModel.activityData.copy(
                        athleteInfo = ActivityModel.AthleteInfo(
                            userId,
                            userProfile.name,
                            userProfile.photo
                        ),
                        placeIdentifier = placeIdentifier
                    )
                )
            else -> activityModel
        }
    }
}

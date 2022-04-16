package akio.apps.myrun.domain.activity

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.AthleteInfo
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.CyclingActivityModel
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.location.api.PlaceDataSource
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.domain.user.PlaceIdentifierConverter
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber

class UploadActivitiesUsecase @Inject constructor(
    private val authenticationState: UserAuthenticationState,
    private val activityRepository: ActivityRepository,
    private val activityLocalStorage: ActivityLocalStorage,
    private val userProfileRepository: UserProfileRepository,
    private val placeDataSource: PlaceDataSource,
    private val placeIdentifierConverter: PlaceIdentifierConverter,
) {
    @OptIn(InternalCoroutinesApi::class, FlowPreview::class)
    suspend fun uploadAll(): Flow<Resource<BaseActivityModel>> =
        activityLocalStorage.loadAllActivityStorageDataFlow().flatMapConcat { storageData ->
            val activityModel = fulfillActivityInfo(
                storageData.activityModel,
                storageData.locationDataPoints.first()
            )
            activityRepository.saveActivity(
                activityModel,
                storageData.routeBitmapFile,
                speedDataPoints = emptyList(),
                locationDataPoints = storageData.locationDataPoints,
                stepCadenceDataPoints = emptyList()
            )
            activityLocalStorage.deleteActivityData(activityModel.id)
            flowOf<Resource<BaseActivityModel>>(Resource.Success(activityModel))
        }
            .catch { exception ->
                emit(Resource.Error(exception))
                Timber.e(exception)
            }

    /**
     * An activity may miss some data when starting (due to no network). This method
     * gathers all data before uploading.
     */
    private suspend fun fulfillActivityInfo(
        activityModel: BaseActivityModel,
        startPoint: ActivityLocation,
    ): BaseActivityModel {
        val userId = authenticationState.requireUserAccountId()
        val userProfile = userProfileRepository.getUserProfile(userId)
        val sortedAddressTexts = placeDataSource.getRecentPlaceAddressFromLocation(
            startPoint.latitude,
            startPoint.longitude
        )
        val activityData = activityModel.activityData.copy(
            athleteInfo = AthleteInfo(userId, userProfile.name, userProfile.photo),
            placeIdentifier =
            placeIdentifierConverter.fromAddressNameList(sortedAddressTexts.map { it.name })
        )
        return when (activityModel) {
            is RunningActivityModel -> activityModel.copy(activityData)
            is CyclingActivityModel -> activityModel.copy(activityData)
        }
    }
}

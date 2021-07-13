package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.myrun.data.activity.ActivityLocalStorage
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.recentplace.PlaceIdentifier
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.domain.recentplace.CreateActivityDisplayPlaceNameUsecase
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class UserTimelineViewModelImpl @Inject constructor(
    private val activityPagingSourceFactory: ActivityPagingSourceFactory,
    private val createActivityDisplayPlaceNameUsecase: CreateActivityDisplayPlaceNameUsecase,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState,
    activityLocalStorage: ActivityLocalStorage
) : UserTimelineViewModel() {

    override val activityStorageCount: Flow<Int> =
        activityLocalStorage.getActivityStorageDataCount()
            .distinctUntilChanged()
            .onEach {
                Timber.d("activityStorageCount = $it")
                activityPagingSource?.invalidate()
            }

    private var activityPagingSource: ActivityPagingSource? = null

    override val myActivityList: Flow<PagingData<Activity>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
            prefetchDistance = PAGE_SIZE
        ),
        initialKey = System.currentTimeMillis()
    ) { recreateActivityPagingSource() }.flow

    private fun recreateActivityPagingSource(): ActivityPagingSource =
        activityPagingSourceFactory().also {
            activityPagingSource = it
        }

    private val mapActivityIdToPlaceName: MutableMap<String, String?> = mutableMapOf()

    override fun getActivityDisplayPlaceName(activity: Activity): String {
        val activityId = activity.id
        var placeName = mapActivityIdToPlaceName[activityId]
        val activityPlaceIdentifier = activity.placeIdentifier
        if (placeName == null && activityPlaceIdentifier != null) {
            placeName = createActivityDisplayPlaceNameUsecase(
                activityPlaceIdentifier,
                userRecentPlaceIdentifier
            )
            mapActivityIdToPlaceName[activityId] = placeName
        }

        Timber.d("getActivityDisplayPlaceName activityId=${activity.id} result=$placeName")
        return placeName ?: ""
    }

    private var userRecentPlaceIdentifier: PlaceIdentifier? = null
    private val isLoadingInitialDataMutable: MutableStateFlow<Boolean> = MutableStateFlow(true)
    override val isLoadingInitialData: Flow<Boolean> = isLoadingInitialDataMutable

    init {
        loadInitialData()
    }

    private fun loadInitialData() = viewModelScope.launch {
        isLoadingInitialDataMutable.value = true
        val userId = userAuthenticationState.getUserAccountId()
        if (userId != null) {
            userRecentPlaceIdentifier = userRecentPlaceRepository.getRecentPlaceIdentifier(userId)
        }
        isLoadingInitialDataMutable.value = false
    }

    companion object {
        const val PAGE_SIZE = 6
    }
}

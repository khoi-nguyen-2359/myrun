package akio.apps.myrun.feature.usertimeline.impl

import akio.apps.myrun.data.activity.ActivityLocalStorage
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.recentplace.PlaceIdentifier
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.domain.recentplace.CreateActivityDisplayPlaceNameUsecase
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import timber.log.Timber
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach

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
            prefetchDistance = 10
        ),
        initialKey = System.currentTimeMillis()
    ) { recreateActivityPagingSource() }.flow

    private fun recreateActivityPagingSource(): ActivityPagingSource =
        activityPagingSourceFactory().also {
            activityPagingSource = it
        }

    private val mapActivityIdToPlaceName: MutableMap<String, String?> = mutableMapOf()

    override val userRecentPlaceIdentifier: Flow<PlaceIdentifier?> = flow {
        val userId = userAuthenticationState.getUserAccountId() ?: return@flow
        val identifier = userRecentPlaceRepository.getRecentPlaceIdentifier(userId)
        emit(identifier)
    }

    override fun getActivityDisplayPlaceName(
        currentUserPlaceIdentifier: PlaceIdentifier?,
        activityId: String,
        activityPlaceIdentifier: PlaceIdentifier?
    ): String? {
        var placeName = mapActivityIdToPlaceName[activityId]
        if (placeName == null) {
            placeName = createActivityDisplayPlaceNameUsecase(
                activityPlaceIdentifier,
                currentUserPlaceIdentifier
            )
            mapActivityIdToPlaceName[activityId] = placeName
        }

        return placeName
    }

    override fun invalidate() {
        activityPagingSource?.invalidate()
    }

    companion object {
        const val PAGE_SIZE = 4
    }
}

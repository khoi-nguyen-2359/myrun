package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.domain.activity.api.ActivityRepository
import akio.apps.myrun.domain.activity.api.RunSplitsCalculator
import akio.apps.myrun.domain.activity.api.model.ActivityModel
import akio.apps.test.whenBlocking
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalTime
@ExperimentalCoroutinesApi
class ActivityDetailViewModelTest {
    private lateinit var viewModel: ActivityDetailViewModel
    private lateinit var mockedActivityRepository: ActivityRepository
    private lateinit var mockedUserAuthenticationState: UserAuthenticationState
    private lateinit var mockedUserRecentPlaceRepository: UserRecentPlaceRepository

    private val testCoroutineDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    private val defaultActivityId = "defaultActivityId"
    private val defaultUserId = "defaultUserId"

    @Before
    fun setup() {
        Dispatchers.setMain(testCoroutineDispatcher)
        mockedActivityRepository = mock()
        mockedUserAuthenticationState = mock()
        mockedUserRecentPlaceRepository = mock()
    }

    private fun initActivityDetailViewModel(
        viewModelParams: SavedStateHandle,
    ) = ActivityDetailViewModel(
        viewModelParams,
        mockedActivityRepository,
        mockedUserRecentPlaceRepository,
        mockedUserAuthenticationState,
        mock(),
        RunSplitsCalculator()
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun testViewModelInitialization_Success() = testCoroutineDispatcher.runBlockingTest {
        val activityModel = mock<ActivityModel>()

        whenBlocking(mockedActivityRepository) { getActivity(defaultActivityId) }
            .thenReturn(activityModel)
        whenever(mockedUserAuthenticationState.requireUserAccountId()).thenReturn(defaultUserId)

        viewModel =
            initActivityDetailViewModel(
                SavedStateHandle(mapOf("SAVED_STATE_ACTIVITY_ID" to defaultActivityId))
            )
        viewModel.activityDetailScreenStateFlow.test {
            val lastEmittedItem = awaitItem()
            assertTrue(
                lastEmittedItem is ActivityDetailViewModel.ActivityDetailScreenState.DataAvailable
            )
            assertEquals(activityModel, lastEmittedItem.activityData)
            assertNull(lastEmittedItem.activityPlaceName)
        }

        verify(mockedActivityRepository).getActivity(defaultActivityId)

        // only verify for once because we observe the flow for the last item
        // (after view model initialization)
        verify(mockedUserAuthenticationState).requireUserAccountId()
    }

    @Test
    fun testLoadActivityDetails_Success() = testCoroutineDispatcher.runBlockingTest {
        testViewModelInitialization_Success()

        val activityModel = mock<ActivityModel>()

        whenBlocking(mockedActivityRepository) { getActivity(defaultActivityId) }
            .thenReturn(activityModel)
        whenever(mockedUserAuthenticationState.requireUserAccountId()).thenReturn(defaultUserId)

        viewModel.activityDetailScreenStateFlow.test {
            awaitItem() // skip last emitted Item
            viewModel.loadActivityDetails()
            val loadingItem = awaitItem()
            assertTrue(
                loadingItem is ActivityDetailViewModel.ActivityDetailScreenState.FullScreenLoading
            )
            val resultItem = awaitItem()
            assertTrue(
                resultItem is ActivityDetailViewModel.ActivityDetailScreenState.DataAvailable
            )
            assertEquals(activityModel, resultItem.activityData)
            assertNull(resultItem.activityPlaceName)
        }

        verify(mockedActivityRepository, times(2)).getActivity(defaultActivityId)
        verify(mockedUserAuthenticationState, times(4)).requireUserAccountId()
    }

    @Test
    fun testLoadActivityDetails_Failure() = testCoroutineDispatcher.runBlockingTest {
        testViewModelInitialization_Success()

        whenBlocking(mockedActivityRepository) { getActivity(defaultActivityId) }
            .thenReturn(null)

        viewModel.activityDetailScreenStateFlow.test {
            awaitItem() // lastEmittedItem
            viewModel.loadActivityDetails()
            assertTrue(
                awaitItem() is ActivityDetailViewModel.ActivityDetailScreenState.FullScreenLoading
            )
            val failureItem = awaitItem()
            assertTrue(
                failureItem is ActivityDetailViewModel.ActivityDetailScreenState.ErrorAndRetry
            )
        }

        verify(mockedActivityRepository, times(2)).getActivity(defaultActivityId)
    }
}

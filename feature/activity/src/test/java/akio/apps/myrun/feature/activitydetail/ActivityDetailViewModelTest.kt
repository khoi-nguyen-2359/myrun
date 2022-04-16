package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.domain.activity.ActivityDateTimeFormatter
import akio.apps.myrun.domain.activity.RunSplitsCalculator
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
    private lateinit var mockedRunSplitsCalculator: RunSplitsCalculator
    private lateinit var mockedActivityDateTimeFormatter: ActivityDateTimeFormatter

    private val testCoroutineDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    private val defaultActivityId = "defaultActivityId"
    private val defaultUserId = "defaultUserId"

    @Before
    fun setup() {
        Dispatchers.setMain(testCoroutineDispatcher)
        mockedActivityRepository = mock()
        mockedUserAuthenticationState = mock()
        mockedUserRecentPlaceRepository = mock()
        mockedRunSplitsCalculator = mock()
        mockedActivityDateTimeFormatter = mock()
    }

    private fun initActivityDetailViewModel(
        viewModelParams: SavedStateHandle,
    ) = ActivityDetailViewModel(
        viewModelParams,
        mockedActivityRepository,
        mockedUserRecentPlaceRepository,
        mockedUserAuthenticationState,
        mock(),
        mockedRunSplitsCalculator,
        mockedActivityDateTimeFormatter
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun testViewModelInitialization_Success() = testCoroutineDispatcher.runBlockingTest {
        val activityStartTime = 1234L
        val activityModel = mock<BaseActivityModel> {
            on { startTime }.thenReturn(activityStartTime)
        }
        val locationDataPoints = mock<List<ActivityLocation>>()

        whenever(mockedActivityRepository.getActivity(defaultActivityId)).thenReturn(activityModel)
        whenever(mockedActivityRepository.getActivityLocationDataPoints(defaultActivityId))
            .thenReturn(locationDataPoints)
        whenever(mockedUserAuthenticationState.requireUserAccountId()).thenReturn(defaultUserId)
        whenever(mockedRunSplitsCalculator.createRunSplits(locationDataPoints))
            .thenReturn(emptyList())
        whenever(mockedActivityDateTimeFormatter.formatActivityDateTime(activityStartTime))
            .thenReturn(mock())

        viewModel =
            initActivityDetailViewModel(
                SavedStateHandle(mapOf("SAVED_STATE_ACTIVITY_ID" to defaultActivityId))
            )
        viewModel.screenStateFlow.test {
            val lastEmittedItem = awaitItem()
            assertTrue(
                lastEmittedItem is ActivityDetailViewModel.ScreenState.DataAvailable
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

        val activityStartTime = 1234L
        val activityModel = mock<BaseActivityModel> {
            on { startTime }.thenReturn(activityStartTime)
        }

        whenever(mockedActivityRepository.getActivity(defaultActivityId)).thenReturn(activityModel)
        whenever(mockedUserAuthenticationState.requireUserAccountId()).thenReturn(defaultUserId)
        whenever(mockedActivityDateTimeFormatter.formatActivityDateTime(activityStartTime))
            .thenReturn(mock())

        viewModel.screenStateFlow.test {
            awaitItem() // skip last emitted Item
            viewModel.loadActivityDetails()
            val loadingItem = awaitItem()
            assertTrue(
                loadingItem is ActivityDetailViewModel.ScreenState.FullScreenLoading
            )
            val resultItem = awaitItem()
            assertTrue(
                resultItem is ActivityDetailViewModel.ScreenState.DataAvailable
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

        whenever(mockedActivityRepository.getActivity(defaultActivityId)).thenReturn(null)

        viewModel.screenStateFlow.test {
            awaitItem() // lastEmittedItem
            viewModel.loadActivityDetails()
            assertTrue(
                awaitItem() is ActivityDetailViewModel.ScreenState.FullScreenLoading
            )
            val failureItem = awaitItem()
            assertTrue(
                failureItem is ActivityDetailViewModel.ScreenState.ErrorAndRetry
            )
        }

        verify(mockedActivityRepository, times(2)).getActivity(defaultActivityId)
    }
}

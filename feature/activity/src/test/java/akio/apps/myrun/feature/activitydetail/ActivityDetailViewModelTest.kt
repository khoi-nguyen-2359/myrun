package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserPreferences
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.domain.activity.ActivityDateTimeFormatter
import akio.apps.myrun.domain.activity.ActivitySplitCalculator
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
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
    private lateinit var mockedActivitySplitCalculator: ActivitySplitCalculator
    private lateinit var mockedActivityDateTimeFormatter: ActivityDateTimeFormatter
    private lateinit var mockedUserPreferences: UserPreferences

    private val testDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()

    private val defaultActivityId = "defaultActivityId"
    private val defaultUserId = "defaultUserId"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockedActivityRepository = mock()
        mockedUserAuthenticationState = mock()
        mockedUserRecentPlaceRepository = mock()
        mockedActivitySplitCalculator = mock()
        mockedActivityDateTimeFormatter = mock()
        mockedUserPreferences = mock()
    }

    private fun initActivityDetailViewModel(
        viewModelParams: SavedStateHandle,
    ) = ActivityDetailViewModel(
        viewModelParams,
        mockedActivityRepository,
        mockedUserRecentPlaceRepository,
        mockedUserAuthenticationState,
        mock(),
        mockedActivitySplitCalculator,
        mockedActivityDateTimeFormatter,
        mockedUserPreferences,
        testDispatcher
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testViewModelInitialization_Success() = runTest(testDispatcher) {
        val activityStartTime = 1234L
        val activityModel = mock<BaseActivityModel> {
            on { startTime }.thenReturn(activityStartTime)
        }
        val locationDataPoints = mock<List<ActivityLocation>>()

        whenever(mockedActivityRepository.getActivity(defaultActivityId)).thenReturn(activityModel)
        whenever(mockedActivityRepository.getActivityLocationDataPoints(defaultActivityId))
            .thenReturn(locationDataPoints)
        whenever(mockedUserAuthenticationState.requireUserAccountId()).thenReturn(defaultUserId)
        whenever(mockedUserPreferences.getMeasureSystem()).thenReturn(flowOf(MeasureSystem.Metric))
        whenever(
            mockedActivitySplitCalculator.createRunSplits(locationDataPoints, MeasureSystem.Metric)
        ).thenReturn(emptyList())
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
    fun testLoadActivityDetails_Success() = runTest(testDispatcher) {
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
    fun testLoadActivityDetails_Failure() = runTest(testDispatcher) {
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

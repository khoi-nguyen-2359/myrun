package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.AthleteInfo
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.CurrentUserPreferences
import akio.apps.myrun.data.user.api.UserPreferencesRepository
import akio.apps.myrun.data.user.api.UserRecentActivityRepository
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
import kotlinx.coroutines.test.StandardTestDispatcher
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
    private lateinit var mockedUserRecentActivityRepository: UserRecentActivityRepository
    private lateinit var mockedActivitySplitCalculator: ActivitySplitCalculator
    private lateinit var mockedActivityDateTimeFormatter: ActivityDateTimeFormatter
    private lateinit var mockedCurrentUserPreferences: CurrentUserPreferences
    private lateinit var mockedUserPrefRepo: UserPreferencesRepository

    private val testDispatcher: CoroutineDispatcher = StandardTestDispatcher()

    private val defaultActivityId = "defaultActivityId"
    private val defaultUserId = "defaultUserId"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockedActivityRepository = mock()
        mockedUserAuthenticationState = mock()
        mockedUserRecentActivityRepository = mock()
        mockedActivitySplitCalculator = mock()
        mockedActivityDateTimeFormatter = mock()
        mockedCurrentUserPreferences = mock()
        mockedUserPrefRepo = mock()
    }

    private fun initActivityDetailViewModel(
        viewModelParams: SavedStateHandle,
    ) = ActivityDetailViewModel(
        viewModelParams,
        mockedActivityRepository,
        mockedUserRecentActivityRepository,
        mockedUserAuthenticationState,
        mock(),
        mockedActivitySplitCalculator,
        mockedActivityDateTimeFormatter,
        mockedCurrentUserPreferences,
        mockedUserPrefRepo,
        testDispatcher
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testViewModelInitialization_Success() = runTest(testDispatcher) {
        val activityStartTime = 1234L
        val mockAthleteInfo: AthleteInfo = mock {
            on { userId }.thenReturn(defaultUserId)
        }
        val activityModel = mock<BaseActivityModel> {
            on { startTime }.thenReturn(activityStartTime)
            on { athleteInfo }.thenReturn(mockAthleteInfo)
        }
        val locationDataPoints = mock<List<ActivityLocation>>()

        whenever(mockedActivityRepository.getActivity(defaultActivityId)).thenReturn(activityModel)
        whenever(mockedActivityRepository.getActivityLocationDataPoints(defaultActivityId))
            .thenReturn(locationDataPoints)
        whenever(mockedUserAuthenticationState.requireUserAccountId()).thenReturn(defaultUserId)
        whenever(mockedCurrentUserPreferences.getMeasureSystemFlow())
            .thenReturn(flowOf(MeasureSystem.Metric))
        whenever(
            mockedActivitySplitCalculator.createRunSplits(locationDataPoints, MeasureSystem.Metric)
        ).thenReturn(emptyList())
        whenever(mockedActivityDateTimeFormatter.formatActivityDateTime(activityStartTime))
            .thenReturn(mock())

        viewModel = initActivityDetailViewModel(
            SavedStateHandle(mapOf("SAVED_STATE_ACTIVITY_ID" to defaultActivityId))
        )
        viewModel.screenStateFlow.test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ActivityDetailViewModel.ScreenState.FullScreenLoading)
            val dataItem = awaitItem()
            assertTrue(dataItem is ActivityDetailViewModel.ScreenState.DataAvailable)
            assertEquals(activityModel, dataItem.activityData)
            assertTrue(dataItem.isMapVisible)
            assertNull(dataItem.activityPlaceName)
        }

        verify(mockedActivityRepository).getActivity(defaultActivityId)
        verify(mockedUserAuthenticationState, times(3)).requireUserAccountId()
    }

    @Test
    fun testRefreshActivityDetails_Success() = runTest(testDispatcher) {
        testViewModelInitialization_Success()
        val mockAthleteInfo: AthleteInfo = mock {
            on { userId }.thenReturn(defaultUserId)
        }
        val activityStartTime = 1234L
        val activityModel = mock<BaseActivityModel> {
            on { startTime }.thenReturn(activityStartTime)
            on { athleteInfo }.thenReturn(mockAthleteInfo)
        }

        whenever(mockedActivityRepository.getActivity(defaultActivityId)).thenReturn(activityModel)
        whenever(mockedUserAuthenticationState.requireUserAccountId()).thenReturn(defaultUserId)
        whenever(mockedActivityDateTimeFormatter.formatActivityDateTime(activityStartTime))
            .thenReturn(mock())

        viewModel.refreshActivityDetails()
        viewModel.screenStateFlow.test {
            val loadingItem = awaitItem()
            assertTrue(loadingItem is ActivityDetailViewModel.ScreenState.FullScreenLoading)
            val dataItem = awaitItem()
            assertTrue(dataItem is ActivityDetailViewModel.ScreenState.DataAvailable)
            assertEquals(activityModel, dataItem.activityData)
            assertNull(dataItem.activityPlaceName)
        }

        verify(mockedActivityRepository, times(2)).getActivity(defaultActivityId)
        verify(mockedUserAuthenticationState, times(6)).requireUserAccountId()
    }

    @Test
    fun testRefreshActivityDetails_Failure() = runTest(testDispatcher) {
        testViewModelInitialization_Success()

        whenever(mockedActivityRepository.getActivity(defaultActivityId)).thenReturn(null)

        viewModel.screenStateFlow.test {
            viewModel.refreshActivityDetails()
            assertTrue(awaitItem() is ActivityDetailViewModel.ScreenState.FullScreenLoading)
            val failureItem = awaitItem()
            assertTrue(failureItem is ActivityDetailViewModel.ScreenState.ErrorAndRetry)
        }

        verify(mockedActivityRepository, times(2)).getActivity(defaultActivityId)
    }
}

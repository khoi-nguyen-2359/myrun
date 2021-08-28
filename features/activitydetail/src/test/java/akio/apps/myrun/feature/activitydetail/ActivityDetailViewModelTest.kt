package akio.apps.myrun.feature.activitydetail

import akio.apps.common.data.Resource
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.test.whenBlocking
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

@ExperimentalTime
@ExperimentalCoroutinesApi
class ActivityDetailViewModelTest {
    private lateinit var viewModel: ActivityDetailViewModel
    private lateinit var mockedActivityRepository: ActivityRepository

    private val testCoroutineDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    private val defaultActivityId = "defaultActivityId"

    @Before
    fun setup() {
        Dispatchers.setMain(testCoroutineDispatcher)
        mockedActivityRepository = mock()
    }

    private fun initActivityDetailViewModel(
        viewModelParams: ActivityDetailViewModel.Arguments,
        mockedActivityRepository: ActivityRepository,
    ) = ActivityDetailViewModel(
        viewModelParams,
        mockedActivityRepository,
        mock(),
        mock(),
        mock()
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

        viewModel =
            initActivityDetailViewModel(
                ActivityDetailViewModel.Arguments(defaultActivityId),
                mockedActivityRepository
            )
        viewModel.activityDetails.test {
            val lastEmittedItem = awaitItem()
            assertTrue(lastEmittedItem is Resource.Success)
            assertEquals(activityModel, lastEmittedItem.data)
        }

        verify(mockedActivityRepository).getActivity(defaultActivityId)
    }

    @Test
    fun testLoadActivityDetails_Success() = testCoroutineDispatcher.runBlockingTest {
        testViewModelInitialization_Success()

        val activityModel = mock<ActivityModel>()

        whenBlocking(mockedActivityRepository) { getActivity(defaultActivityId) }
            .thenReturn(activityModel)

        viewModel.activityDetails.test {
            awaitItem() // lastEmittedItem
            viewModel.loadActivityDetails()
            val loadingItem = awaitItem()
            assertTrue(loadingItem is Resource.Loading)
            val resultItem = awaitItem()
            assertTrue(resultItem is Resource.Success)
            assertEquals(activityModel, resultItem.data)
        }

        verify(mockedActivityRepository, times(2)).getActivity(defaultActivityId)
    }

    @Test
    fun testLoadActivityDetails_Failure() = testCoroutineDispatcher.runBlockingTest {
        testViewModelInitialization_Success()

        whenBlocking(mockedActivityRepository) { getActivity(defaultActivityId) }
            .thenReturn(null)

        viewModel.activityDetails.test {
            awaitItem() // lastEmittedItem
            viewModel.loadActivityDetails()
            assertTrue(awaitItem() is Resource.Loading)
            val failureItem = awaitItem()
            assertTrue(failureItem is Resource.Error)
            assertTrue(failureItem.exception is ActivityDetailViewModel.ActivityNotFoundException)
            assertNull(failureItem.data)
        }

        verify(mockedActivityRepository, times(2)).getActivity(defaultActivityId)
    }
}

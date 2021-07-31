package akio.apps.myrun.feature.activitydetail

import akio.apps._base.Resource
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.feature.activitydetail.impl.ActivityDetailViewModelImpl
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityModelMapper
import akio.apps.test.wheneverBlocking
import app.cash.turbine.test
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
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

@ExperimentalTime
@ExperimentalCoroutinesApi
class ActivityDetailViewModelImplTest {
    private lateinit var viewModel: ActivityDetailViewModelImpl
    private lateinit var mockedActivityRepository: ActivityRepository
    private lateinit var mockedActivityModelMapper: ActivityModelMapper

    private val testCoroutineDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    private val defaultActivityId = "defaultActivityId"

    @Before
    fun setup() {
        Dispatchers.setMain(testCoroutineDispatcher)
        mockedActivityRepository = mock()
        mockedActivityModelMapper = mock()

        val viewModelParams = ActivityDetailViewModel.Params(defaultActivityId)
        viewModel = ActivityDetailViewModelImpl(
            viewModelParams,
            mockedActivityRepository,
            mockedActivityModelMapper,
            mock(),
            mock(),
            mock()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testCoroutineDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun testLoadActivityDetails_Success() = testCoroutineDispatcher.runBlockingTest {
        val activityModel = mock<ActivityModel>()
        val activity = mock<Activity>()
        wheneverBlocking(mockedActivityRepository) { getActivity(defaultActivityId) }
            .thenReturn(activityModel)
        whenever(mockedActivityModelMapper.map(activityModel)).thenReturn(activity)

        viewModel.activityDetails.test {
            viewModel.loadActivityDetails()
            assertTrue(expectItem() is Resource.Loading)
            assertTrue(expectItem() is Resource.Loading)
            val successItem = expectItem()
            assertTrue(successItem is Resource.Success)
            assertEquals(activity, successItem.data)
        }

        verify(mockedActivityRepository).getActivity(defaultActivityId)
        verify(mockedActivityModelMapper).map(activityModel)
    }

    @Test
    fun testLoadActivityDetails_Failure() = testCoroutineDispatcher.runBlockingTest {
        wheneverBlocking(mockedActivityRepository) { getActivity(defaultActivityId) }
            .thenReturn(null)

        viewModel.activityDetails.test {
            viewModel.loadActivityDetails()
            assertTrue(expectItem() is Resource.Loading)
            assertTrue(expectItem() is Resource.Loading)
            val failureItem = expectItem()
            assertTrue(failureItem is Resource.Error)
            assertTrue(failureItem.exception is ActivityDetailViewModel.ActivityNotFoundException)
            assertNull(failureItem.data)

            verify(mockedActivityRepository).getActivity(defaultActivityId)
        }
    }
}

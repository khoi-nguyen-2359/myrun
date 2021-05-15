package akio.apps.myrun.feature.activitydetail

import akio.apps._base.Resource
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.data.recentplace.entity.PlaceIdentifier
import akio.apps.myrun.feature.activitydetail.impl.ActivityDetailViewModelImpl
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityModelMapper
import akio.apps.test.wheneverBlocking
import app.cash.turbine.test
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Before
import org.junit.Test
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

@ExperimentalTime
@ExperimentalCoroutinesApi
class ActivityDetailViewModelImplTest {
    private lateinit var viewModel: ActivityDetailViewModelImpl
    private lateinit var mockedActivityRepository: ActivityRepository
    private lateinit var mockedActivityModelMapper: ActivityModelMapper
    private lateinit var mockedActivityDateTimeFormatter: ActivityDateTimeFormatter
    private lateinit var mockedUserRecentPlaceRepository: UserRecentPlaceRepository
    private lateinit var mockedUserAuthenticationState: UserAuthenticationState

    private val testCoroutineDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    private val defaultUserId = "defaultUserId"
    private val defaultActivityId = "defaultActivityId"

    @Before
    fun setup() {
        Dispatchers.setMain(testCoroutineDispatcher)
        mockedActivityRepository = mock()
        mockedActivityModelMapper = mock()
        mockedActivityDateTimeFormatter = mock()
        mockedUserRecentPlaceRepository = mock()
        mockedUserAuthenticationState = mock()

        val viewModelParams = ActivityDetailViewModel.Params(defaultActivityId)
        viewModel = ActivityDetailViewModelImpl(
            viewModelParams,
            mockedActivityRepository,
            mockedActivityModelMapper,
            mockedActivityDateTimeFormatter,
            mockedUserRecentPlaceRepository,
            mockedUserAuthenticationState
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
            val failureItem = expectItem()
            assertTrue(failureItem is Resource.Error)
            assertTrue(failureItem.exception is ActivityDetailViewModel.ActivityNotFoundException)
            assertNull(failureItem.data)

            verify(mockedActivityRepository).getActivity(defaultActivityId)
        }
    }

    @Test
    fun testGetActivityPlaceDisplayName_DifferentCountry() =
        testCoroutineDispatcher.runBlockingTest {
            val activity = mock<Activity> {
                on { placeIdentifier }.thenReturn("USA-CA-SJ")
            }
            wheneverBlocking(mockedActivityRepository) { getActivity(defaultActivityId) }
                .thenReturn(mock())
            whenever(mockedActivityModelMapper.map(any())).thenReturn(activity)
            viewModel.loadActivityDetails()

            val userPlaceIdentifier = PlaceIdentifier("VN-HCM-D10-W9")
            whenever(mockedUserRecentPlaceRepository.getRecentPlaceIdentifier(defaultUserId))
                .thenReturn(userPlaceIdentifier)
            whenever(mockedUserAuthenticationState.getUserAccountId()).thenReturn(defaultUserId)

            val placeName = viewModel.getActivityPlaceDisplayName()
            assertEquals("USA, CA", placeName)
            verify(mockedUserAuthenticationState).getUserAccountId()
            verify(mockedUserRecentPlaceRepository).getRecentPlaceIdentifier(defaultUserId)
        }

    @Test
    fun testGetActivityPlaceDisplayName_DifferentCity() = testCoroutineDispatcher.runBlockingTest {
        val activity = mock<Activity> {
            on { placeIdentifier }.thenReturn("VN-HN-CG")
        }
        wheneverBlocking(mockedActivityRepository) { getActivity(defaultActivityId) }
            .thenReturn(mock())
        whenever(mockedActivityModelMapper.map(any())).thenReturn(activity)
        viewModel.loadActivityDetails()

        val userPlaceIdentifier = PlaceIdentifier("VN-HCM-D10-W9")
        whenever(mockedUserRecentPlaceRepository.getRecentPlaceIdentifier(defaultUserId))
            .thenReturn(userPlaceIdentifier)
        whenever(mockedUserAuthenticationState.getUserAccountId()).thenReturn(defaultUserId)

        val placeName = viewModel.getActivityPlaceDisplayName()
        assertEquals("HN, CG", placeName)
        verify(mockedUserAuthenticationState).getUserAccountId()
        verify(mockedUserRecentPlaceRepository).getRecentPlaceIdentifier(defaultUserId)
    }

    @Test
    fun testGetActivityPlaceDisplayName_DifferentLastAddress() =
        testCoroutineDispatcher.runBlockingTest {
            val activity = mock<Activity> {
                on { placeIdentifier }.thenReturn("VN-HCM-D10-W10")
            }
            wheneverBlocking(mockedActivityRepository) { getActivity(defaultActivityId) }
                .thenReturn(mock())
            whenever(mockedActivityModelMapper.map(any())).thenReturn(activity)
            viewModel.loadActivityDetails()

            val userPlaceIdentifier = PlaceIdentifier("VN-HCM-D10-W9")
            whenever(mockedUserRecentPlaceRepository.getRecentPlaceIdentifier(defaultUserId))
                .thenReturn(userPlaceIdentifier)
            whenever(mockedUserAuthenticationState.getUserAccountId()).thenReturn(defaultUserId)

            val placeName = viewModel.getActivityPlaceDisplayName()
            assertEquals("D10, W10", placeName)
            verify(mockedUserAuthenticationState).getUserAccountId()
            verify(mockedUserRecentPlaceRepository).getRecentPlaceIdentifier(defaultUserId)
        }

    @Test
    fun testGetActivityPlaceDisplayName_NoUserRecentPlace() =
        testCoroutineDispatcher.runBlockingTest {
            val activity = mock<Activity> {
                on { placeIdentifier }.thenReturn("VN-HCM-D10-W10")
            }
            wheneverBlocking(mockedActivityRepository) { getActivity(defaultActivityId) }
                .thenReturn(mock())
            whenever(mockedActivityModelMapper.map(any())).thenReturn(activity)
            viewModel.loadActivityDetails()

            whenever(mockedUserRecentPlaceRepository.getRecentPlaceIdentifier(defaultUserId))
                .thenReturn(null)
            whenever(mockedUserAuthenticationState.getUserAccountId()).thenReturn(defaultUserId)

            val placeName = viewModel.getActivityPlaceDisplayName()
            assertEquals("VN, HCM", placeName)
            verify(mockedUserAuthenticationState).getUserAccountId()
            verify(mockedUserRecentPlaceRepository).getRecentPlaceIdentifier(defaultUserId)
        }

    @Test
    fun testGetActivityPlaceDisplayName_ShortActivityPlaceAddressList() =
        testCoroutineDispatcher.runBlockingTest {
            val activity = mock<Activity> {
                on { placeIdentifier }.thenReturn("HCM-D10")
            }
            wheneverBlocking(mockedActivityRepository) { getActivity(defaultActivityId) }
                .thenReturn(mock())
            whenever(mockedActivityModelMapper.map(any())).thenReturn(activity)
            viewModel.loadActivityDetails()

            whenever(mockedUserRecentPlaceRepository.getRecentPlaceIdentifier(defaultUserId))
                .thenReturn(null)
            whenever(mockedUserAuthenticationState.getUserAccountId()).thenReturn(defaultUserId)

            val placeName = viewModel.getActivityPlaceDisplayName()
            assertEquals("HCM, D10", placeName)
            verify(mockedUserAuthenticationState).getUserAccountId()
            verify(mockedUserRecentPlaceRepository).getRecentPlaceIdentifier(defaultUserId)
        }
}

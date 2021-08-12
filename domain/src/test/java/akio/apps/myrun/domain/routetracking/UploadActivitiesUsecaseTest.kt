package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.api.model.ActivityStorageData
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserProfileRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class UploadActivitiesUsecaseTest {

    private lateinit var mockedUserProfileRepository: UserProfileRepository
    private lateinit var mockedAuthenticationState: UserAuthenticationState
    private lateinit var mockedActivityLocalStorage: ActivityLocalStorage
    private lateinit var mockedActivityRepository: ActivityRepository
    private lateinit var uploadActivitiesUsecase: UploadActivitiesUsecase

    @Before
    fun setup() {
        mockedActivityRepository = mock()
        mockedActivityLocalStorage = mock()
        mockedAuthenticationState = mock()
        mockedUserProfileRepository = mock()
        uploadActivitiesUsecase = UploadActivitiesUsecase(
            mockedAuthenticationState,
            mockedActivityRepository,
            mockedActivityLocalStorage,
            mockedUserProfileRepository
        )
    }

    @Test
    fun testUploadAll_LoadDataExceptionCase() = runBlockingTest {
        val storageDataFlow = flow<ActivityStorageData> { throw Exception("crash!") }
        whenever(mockedActivityLocalStorage.loadAllActivityStorageDataFlow())
            .thenReturn(storageDataFlow)

        val result = uploadActivitiesUsecase.uploadAll()
        assertFalse(result)
    }

    @Test(expected = Exception::class)
    fun testUploadAll_UploadExceptionCase() = runBlockingTest {
        val activityStorageData = createActivityStorageData()
        val storageDataFlow = flowOf(activityStorageData)
        whenever(mockedActivityLocalStorage.loadAllActivityStorageDataFlow())
            .thenReturn(storageDataFlow)
        whenever(mockedActivityRepository.saveActivity(any(), any(), any(), any(), any()))
            .then { throw Exception("crash!") }

        val result = uploadActivitiesUsecase.uploadAll()
        assertFalse(result)
    }

    private fun createActivityStorageData(): ActivityStorageData =
        ActivityStorageData(
            RunningActivityModel(
                ActivityDataModel(
                    "id",
                    ActivityType.Cycling,
                    "name",
                    "routeImage",
                    "placeIdentifier",
                    startTime = 1000L,
                    endTime = 1000L,
                    duration = 1000L,
                    distance = 10.0,
                    "encodedPolyline",
                    ActivityModel.AthleteInfo("userId", "userName", "userAvatar")
                ),
                pace = 4.0,
                cadence = 0
            ),
            emptyList(),
            mock()
        )
}

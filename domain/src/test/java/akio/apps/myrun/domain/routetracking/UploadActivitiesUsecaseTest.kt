package akio.apps.myrun.domain.routetracking

import akio.apps.myrun.data.activity.ActivityLocalStorage
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.model.ActivityDataModel
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activity.model.ActivityStorageData
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.activity.model.RunningActivityModel
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest

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

    private fun createActivityStorageData(): ActivityStorageData = ActivityStorageData(
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

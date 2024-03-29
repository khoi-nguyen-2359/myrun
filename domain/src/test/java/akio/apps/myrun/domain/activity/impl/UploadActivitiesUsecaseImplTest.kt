package akio.apps.myrun.domain.activity.impl

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivityStorageData
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.AthleteInfo
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.location.api.PlaceDataSource
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.domain.activity.UploadActivitiesUsecase
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class UploadActivitiesUsecaseImplTest {

    private lateinit var mockedUserProfileRepository: UserProfileRepository
    private lateinit var mockedAuthenticationState: UserAuthenticationState
    private lateinit var mockedActivityLocalStorage: ActivityLocalStorage
    private lateinit var mockedActivityRepository: ActivityRepository
    private lateinit var uploadActivitiesUsecase: UploadActivitiesUsecase
    private lateinit var mockedPlaceDataSource: PlaceDataSource

    @Before
    fun setup() {
        mockedActivityRepository = mock()
        mockedActivityLocalStorage = mock()
        mockedAuthenticationState = mock()
        mockedUserProfileRepository = mock()
        mockedPlaceDataSource = mock()
        uploadActivitiesUsecase = UploadActivitiesUsecase(
            mockedAuthenticationState,
            mockedActivityRepository,
            mockedActivityLocalStorage,
            mockedUserProfileRepository,
            mockedPlaceDataSource
        )
    }

    @Test
    fun testUploadAll_LoadDataExceptionCase() = runTest {
        val storageDataFlow = flow<ActivityStorageData> { throw Exception("crash!") }
        whenever(mockedActivityLocalStorage.loadAllActivityStorageDataFlow())
            .thenReturn(storageDataFlow)

        uploadActivitiesUsecase.uploadAll().test {
            val errorResourceItem = awaitItem()
            assert(errorResourceItem is Resource.Error)
            awaitComplete()
        }
    }

    @Test
    fun testUploadAll_UploadExceptionCase() = runTest {
        val activityStorageData = createActivityStorageData()
        val storageDataFlow = flowOf(activityStorageData)
        whenever(mockedActivityLocalStorage.loadAllActivityStorageDataFlow())
            .thenReturn(storageDataFlow)
        whenever(mockedActivityRepository.saveActivity(any(), any(), any(), any(), any()))
            .then { throw Exception("crash!") }

        uploadActivitiesUsecase.uploadAll().test {
            val errorResourceItem = awaitItem()
            assert(errorResourceItem is Resource.Error)
            awaitComplete()
        }
    }

    private fun createActivityStorageData(): ActivityStorageData =
        ActivityStorageData(
            RunningActivityModel(
                ActivityDataModel(
                    "id",
                    ActivityType.Cycling,
                    "name",
                    "routeImage",
                    null,
                    startTime = 1000L,
                    endTime = 1000L,
                    duration = 1000L,
                    distance = 10.0,
                    "encodedPolyline",
                    AthleteInfo("userId", "userName", "userAvatar")
                ),
                pace = 4.0,
                cadence = 0
            ),
            emptyList(),
            mock()
        )
}

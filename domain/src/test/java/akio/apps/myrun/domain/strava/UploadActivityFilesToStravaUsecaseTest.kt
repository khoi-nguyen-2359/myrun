package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.activity.ActivityLocalStorage
import akio.apps.myrun.data.activity.model.ActivityDataModel
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.activity.model.ActivitySyncData
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.activity.model.RunningActivityModel
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.StravaDataRepository
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.StravaAthlete
import akio.apps.myrun.data.externalapp.model.StravaTokenRefresh
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest

@ExperimentalCoroutinesApi
class UploadActivityFilesToStravaUsecaseTest {

    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
    private lateinit var mockedActivityLocalStorage: ActivityLocalStorage
    private lateinit var mockedStravaDataRepository: StravaDataRepository
    private lateinit var mockedExternalAppProvidersRepository: ExternalAppProvidersRepository
    private lateinit var mockedUserAuthenticateState: UserAuthenticationState
    private lateinit var uploadActivityFilesToStravaUsecase: UploadActivityFilesToStravaUsecase

    @Before
    fun setup() {
        mockedUserAuthenticateState = mock()
        mockedExternalAppProvidersRepository = mock()
        mockedStravaDataRepository = mock()
        mockedActivityLocalStorage = mock()
        uploadActivityFilesToStravaUsecase = UploadActivityFilesToStravaUsecase(
            mockedUserAuthenticateState,
            mockedExternalAppProvidersRepository,
            mockedStravaDataRepository,
            mockedActivityLocalStorage,
            testDispatcher
        )
    }

    @Test
    fun uploadAll_LoadDataExceptionCase() = testDispatcher.runBlockingTest {
        whenever(mockedUserAuthenticateState.requireUserAccountId()).thenReturn("userId")
        val stravaToken = createStravaToken()
        whenever(mockedExternalAppProvidersRepository.getStravaProviderToken("userId"))
            .thenReturn(stravaToken)
        val exceptionFlow = flow<ActivitySyncData> { throw Exception("crash!") }
        whenever(mockedActivityLocalStorage.loadAllActivitySyncDataFlow())
            .thenReturn(exceptionFlow)
        val result = uploadActivityFilesToStravaUsecase.uploadAll()
        assertFalse(result)
    }

    @Test(expected = Exception::class)
    fun uploadAll_UploadDataExceptionCase() = testDispatcher.runBlockingTest {
        whenever(mockedUserAuthenticateState.requireUserAccountId()).thenReturn("userId")
        val stravaToken = createStravaToken()
        whenever(mockedExternalAppProvidersRepository.getStravaProviderToken("userId"))
            .thenReturn(stravaToken)
        val syncData = createActivitySyncData()
        val syncDataFlow = flowOf(syncData)
        whenever(mockedActivityLocalStorage.loadAllActivitySyncDataFlow()).thenReturn(syncDataFlow)
        whenever(mockedStravaDataRepository.saveActivity(any(), any(), any()))
            .then { throw Exception("crash!") }
        val result = uploadActivityFilesToStravaUsecase.uploadAll()
        assertFalse(result)
    }

    private fun createActivitySyncData(): ActivitySyncData = ActivitySyncData(
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
        mock()
    )

    private fun createStravaToken(): ExternalAppToken.StravaToken {
        return ExternalAppToken.StravaToken(
            StravaTokenRefresh("accessToken", "refreshToken"),
            StravaAthlete(id = 1000L)
        )
    }
}
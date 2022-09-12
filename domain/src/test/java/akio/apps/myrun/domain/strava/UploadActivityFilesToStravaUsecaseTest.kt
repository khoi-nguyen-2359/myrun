package akio.apps.myrun.domain.strava

import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.activity.api.model.ActivityDataModel
import akio.apps.myrun.data.activity.api.model.ActivitySyncData
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.activity.api.model.AthleteInfo
import akio.apps.myrun.data.activity.api.model.RunningActivityModel
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.api.StravaDataRepository
import akio.apps.myrun.data.eapps.api.StravaSyncState
import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.api.model.StravaAthlete
import akio.apps.myrun.data.eapps.api.model.StravaTokenRefresh
import akio.apps.myrun.data.user.api.model.PlaceIdentifier
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class UploadActivityFilesToStravaUsecaseTest {

    private lateinit var mockedActivityLocalStorage: ActivityLocalStorage
    private lateinit var mockedStravaDataRepository: StravaDataRepository
    private lateinit var mockedExternalAppProvidersRepository: ExternalAppProvidersRepository
    private lateinit var mockedUserAuthenticateState: UserAuthenticationState
    private lateinit var uploadActivityFilesToStravaUsecase: UploadActivityFilesToStravaUsecase
    private lateinit var mockedstravaSyncState: StravaSyncState

    @Before
    fun setup() {
        mockedUserAuthenticateState = mock()
        mockedExternalAppProvidersRepository = mock()
        mockedStravaDataRepository = mock()
        mockedActivityLocalStorage = mock()
        mockedstravaSyncState = mock()
        uploadActivityFilesToStravaUsecase = UploadActivityFilesToStravaUsecase(
            mockedUserAuthenticateState,
            mockedExternalAppProvidersRepository,
            mockedStravaDataRepository,
            mockedstravaSyncState,
            mockedActivityLocalStorage
        )
    }

    @Test
    fun uploadAll_LoadMultiAsyncItems_ExceptionCase() = runTest {
        whenever(mockedUserAuthenticateState.requireUserAccountId()).thenReturn("userId")
        val stravaToken = createStravaToken()
        whenever(mockedExternalAppProvidersRepository.getStravaProviderToken("userId"))
            .thenReturn(stravaToken)
        val dataItemFlow = flowOf(
            *listOf("id1", "id2", "id3", "id4", "id5").map {
                createActivitySyncData(it)
            }.toTypedArray()
        )
        whenever(mockedActivityLocalStorage.loadAllActivitySyncDataFlow()).thenReturn(dataItemFlow)
        whenever(mockedActivityLocalStorage.deleteActivitySyncData("id3")).then {
            throw Exception("Crash at data item id3!")
        }
        val result = uploadActivityFilesToStravaUsecase.uploadAll()
        assertEquals(false, result)
    }

    @Test
    fun uploadAll_LoadDataExceptionCase() = runTest {
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

    @Test
    fun uploadAll_UploadDataExceptionCase() = runTest {
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

    private fun createActivitySyncData(id: String = "id"): ActivitySyncData =
        ActivitySyncData(
            RunningActivityModel(
                ActivityDataModel(
                    id,
                    ActivityType.Cycling,
                    "name",
                    "routeImage",
                    PlaceIdentifier.fromPlaceIdentifierString("placeIdentifier"),
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
            mock()
        )

    private fun createStravaToken(): ExternalAppToken.StravaToken {
        return ExternalAppToken.StravaToken(
            StravaTokenRefresh("accessToken", "refreshToken"),
            StravaAthlete(id = 1000L)
        )
    }
}

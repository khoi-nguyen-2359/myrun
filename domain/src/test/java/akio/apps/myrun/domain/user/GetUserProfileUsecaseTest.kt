package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.UserProfile
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.Mockito.`when` as whenever

@ExperimentalTime
@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class GetUserProfileUsecaseTest {

    private lateinit var mockedUserProfileRepository: UserProfileRepository
    private lateinit var mockedUserAuthenticationState: UserAuthenticationState

    private val defaultUserId = "defaultUserId"

    lateinit var testee: GetUserProfileUsecase

    @Before
    fun setup() {
        mockedUserProfileRepository = mock()
        mockedUserAuthenticationState = mock()
        MockitoAnnotations.openMocks(this)
        testee = GetUserProfileUsecase(mockedUserProfileRepository, mockedUserAuthenticationState)
    }

    @Test
    fun `given user logged in, when get user profile, then return user profile data`() {
        // given
        val userProfile = createUserProfile()
        whenever(mockedUserProfileRepository.getUserProfileResourceFlow(defaultUserId)).thenReturn(
            flowOf(Resource.Success(userProfile))
        )

        // when
        runTest {
            val userProfileFlow = testee.getUserProfileFlow(defaultUserId)
            userProfileFlow.collect { userProfileResource ->
                assertEquals(userProfile, userProfileResource.data)
            }
        }

        verify(mockedUserProfileRepository).getUserProfileResourceFlow(defaultUserId)
    }

    @Test
    fun `given user not logged in, when get user profile, then exception thrown`() = runTest {
        // given
        val thrownException = IllegalStateException()
        whenever(mockedUserAuthenticationState.requireUserAccountId())
            .thenThrow(thrownException)

        // when
        val resource = testee.getUserProfileResource(null)
        assertTrue(resource is Resource.Error)
        assertNull(resource.data)
        assertEquals(thrownException, (resource as Resource.Error).exception)

        // then
        verify(mockedUserAuthenticationState).requireUserAccountId()
        verify(mockedUserProfileRepository, never()).getUserProfileResourceFlow(anyString())
    }

    private fun createUserProfile(): UserProfile {
        return UserProfile(
            defaultUserId,
            "name",
            Gender.Male,
            65f,
            null
        )
    }
}

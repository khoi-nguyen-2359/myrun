package akio.apps.myrun.domain.user

import akio.apps.myrun.data.Resource
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.authentication.api.model.UserAccount
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.user.impl.GetUserProfileUsecase
import app.cash.turbine.test
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
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
    private val defaultEmail = "defaultEmail"
    private val defaultPhoneNumber = "defaultPhoneNumber"
    private val defaultDisplayName = "defaultDisplayName"
    private val defaultPhotoUrl = "defaultPhotoUrl"

    lateinit var testee: GetUserProfileUsecase

    private val testCoroutineDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        mockedUserProfileRepository = mock()
        mockedUserAuthenticationState = mock()
        MockitoAnnotations.openMocks(this)
        testee = GetUserProfileUsecase(
            mockedUserProfileRepository,
            mockedUserAuthenticationState
        )
    }

    @Test
    fun `given user logged in, when get user profile, then return user profile data`() {
        // given
        val userProfile = createUserProfile()
        whenever(mockedUserAuthenticationState.requireUserAccountId()).thenReturn(defaultUserId)
        whenever(mockedUserProfileRepository.getUserProfileFlow(defaultUserId)).thenReturn(
            flowOf(Resource.Success(userProfile))
        )

        // when
        runBlockingTest {
            val userProfileFlow = testee.getUserProfileFlow(null)
            userProfileFlow.collect { userProfileResource ->
                assertEquals(userProfile, userProfileResource.data)
            }
        }

        verify(mockedUserAuthenticationState).requireUserAccountId()
        verify(mockedUserProfileRepository).getUserProfileFlow(defaultUserId)
    }

    private fun createUserAccount(): UserAccount {
        return UserAccount(
            defaultUserId,
            defaultEmail,
            defaultDisplayName,
            defaultPhotoUrl,
            defaultPhoneNumber
        )
    }

    @Test
    fun `given user not logged in, when get user profile, then exception thrown`() =
        testCoroutineDispatcher.runBlockingTest {
            // given
            val thrownException = IllegalStateException()
            whenever(mockedUserAuthenticationState.requireUserAccountId())
                .thenThrow(thrownException)

            // when
            testee.getUserProfileFlow(null).test {
                val resource = awaitItem()
                assertTrue(resource is Resource.Error)
                assertNull(resource.data)
                assertEquals(thrownException, (resource as Resource.Error).exception)
                awaitComplete()
            }

            // then
            verify(mockedUserAuthenticationState).requireUserAccountId()
            verify(mockedUserProfileRepository, never()).getUserProfileFlow(anyString())
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

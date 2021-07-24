package akio.apps.myrun.domain.user

import akio.apps._base.Resource
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.authentication.model.UserAccount
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.Gender
import akio.apps.myrun.data.userprofile.model.UserProfile
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.mockito.Mockito.`when` as whenever

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

    @Test(expected = IllegalStateException::class)
    fun `given user not logged in, when get user profile, then exception thrown`() {
        // given
        whenever(mockedUserAuthenticationState.requireUserAccountId()).thenThrow(
            IllegalStateException()
        )

        // when
        testee.getUserProfileFlow(null)

        // then
        verify(mockedUserAuthenticationState).getUserAccountId()
        verify(mockedUserProfileRepository, never()).getUserProfileFlow(anyString())
    }

    private fun createUserProfile(): UserProfile {
        return UserProfile(
            defaultUserId,
            "name",
            defaultEmail,
            defaultPhoneNumber,
            Gender.male,
            1.70f,
            65f,
            null
        )
    }
}

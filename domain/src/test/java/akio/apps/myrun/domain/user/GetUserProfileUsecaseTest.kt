package akio.apps.myrun.domain.user

import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.authentication.model.UserAccount
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.Gender
import akio.apps.myrun.data.userprofile.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when` as whenever

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class GetUserProfileUsecaseTest {

    @Mock
    lateinit var userProfileRepository: UserProfileRepository

    @Mock
    lateinit var userAuthenticationState: UserAuthenticationState

    private val defaultUserId = "defaultUserId"
    private val defaultEmail = "defaultEmail"
    private val defaultPhoneNumber = "defaultPhoneNumber"
    private val defaultDisplayName = "defaultDisplayName"
    private val defaultPhotoUrl = "defaultPhotoUrl"

    lateinit var testee: GetUserProfileUsecase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testee = GetUserProfileUsecase(userProfileRepository, userAuthenticationState)
    }

    @Test
    fun `given user logged in, when get user profile, then return user profile data`() {
        // given
        val userProfile = createUserProfile()
        whenever(userAuthenticationState.getUserAccountId()).thenReturn(defaultUserId)
        whenever(userProfileRepository.getUserProfileFlow(defaultUserId)).thenReturn(
            flowOf(
                Resource.Success(
                    userProfile
                )
            )
        )

        // when
        runBlockingTest {
            val userProfileFlow = testee.getUserProfileFlow()
            userProfileFlow.collect {
                assertEquals(userProfile, it.data)
            }
        }

        verify(userAuthenticationState).getUserAccountId()
        verify(userProfileRepository).getUserProfileFlow(defaultUserId)
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

    @Test(expected = UnauthorizedUserError::class)
    fun `given user not logged in, when get user profile, then exception thrown`() {
        // given
        whenever(userAuthenticationState.getUserAccountId()).thenReturn(null)

        // when
        testee.getUserProfileFlow()

        // then
        verify(userAuthenticationState).getUserAccountId()
        verify(userProfileRepository, never()).getUserProfileFlow(anyString())
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

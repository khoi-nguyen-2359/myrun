package akio.apps.myrun.feature.userprofile.impl

import akio.apps.MockAsynchronousTest
import akio.apps._base.data.Resource
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.authentication.impl.UserAccount
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.Gender
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.feature.userprofile.GetUserProfileUsecase
import akio.apps.myrun.feature.userprofile.usecase.GetUserProfileUsecaseImpl
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when` as whenever

class GetUserProfileUsecaseImplTest: MockAsynchronousTest() {

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
        testee = GetUserProfileUsecaseImpl(userProfileRepository, userAuthenticationState)
    }

    @Test
    fun `given user logged in, when get user profile, then return user profile data`() {
        // given
        val userProfile = createUserProfile()
        val userAccount = createUserAccount()
        whenever(userAuthenticationState.getUserAccountFlow()).thenReturn(flowOf(userAccount))
        whenever(userProfileRepository.getUserProfileFlow(defaultUserId)).thenReturn(flowOf(Resource.Success(userProfile)))

        // when
        val userProfileLiveData = testee.getUserProfile()
        userProfileLiveData.observeForever { value ->
            // then
            assertEquals(userProfile, value.data)
        }

        verify(userAuthenticationState).getUserAccountFlow()
        verify(userProfileRepository).getUserProfileFlow(defaultUserId)
    }

    private fun createUserAccount(): UserAccount {
        return UserAccount(defaultUserId, defaultEmail, defaultDisplayName, defaultPhotoUrl, defaultPhoneNumber)
    }

    @Test
    fun `given user not logged in, when get user profile, then return empty live data`() {
        // given
        whenever(userAuthenticationState.getUserAccountFlow()).thenReturn(emptyFlow())

        // when
        testee.getUserProfile().observeForever { value ->

            //then
            assertEquals(null, value.data)
        }

        // then
        verify(userAuthenticationState).getUserAccountFlow()
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


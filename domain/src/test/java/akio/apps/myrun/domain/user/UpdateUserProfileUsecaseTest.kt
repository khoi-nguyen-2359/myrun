package akio.apps.myrun.domain.user

import akio.apps._base.any
import akio.apps._base.eq
import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.userprofile.UserProfileRepository
import akio.apps.myrun.data.userprofile.model.Gender
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import android.net.Uri
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when` as whenever

@ExperimentalCoroutinesApi
class UpdateUserProfileUsecaseTest {

    @Mock
    lateinit var userAuthenticationState: UserAuthenticationState

    @Mock
    lateinit var userProfileRepository: UserProfileRepository

    lateinit var testee: UpdateUserProfileUsecase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testee = UpdateUserProfileUsecase(userProfileRepository, userAuthenticationState)
    }

    @Test
    fun `given user logged in, when update user profile, then update call success`() {
        runBlockingTest {
            // given
            val userId = "userId"
            whenever(userAuthenticationState.getUserAccountId()).thenReturn(userId)
            val editData = createProfileEditData()
            whenever(userProfileRepository.updateUserProfile(userId, editData)).thenReturn(Unit)

            // when
            testee.updateUserProfile(editData)

            // then
            verify(userAuthenticationState).getUserAccountId()
            verify(userProfileRepository).updateUserProfile(userId, editData)
        }
    }

    @Test
    fun `given user not logged in, when update user profile, then update call return InvalidUserState error`() {
        runBlockingTest {
            // given
            whenever(userAuthenticationState.getUserAccountId()).thenReturn(null)

            // when
            val editData = createProfileEditData()
            try {
                testee.updateUserProfile(editData)
            } catch (ex: Throwable) {
                assertTrue(ex is UnauthorizedUserError)
            }

            // then
            verify(userAuthenticationState).getUserAccountId()
            verify(userProfileRepository, never()).updateUserProfile(any(), eq(editData))
        }
    }

    private fun createProfileEditData(
        phoneNumber: String? = "edit phone number",
        displayName: String = "displayName",
        avatarUri: Uri? = null
    ): ProfileEditData {
        return ProfileEditData(
            displayName,
            Gender.male,
            170f,
            65f,
            avatarUri,
            phoneNumber
        )
    }
}

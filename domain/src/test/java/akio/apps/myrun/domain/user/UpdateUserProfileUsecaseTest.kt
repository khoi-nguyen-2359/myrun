package akio.apps.myrun.domain.user

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.ProfileEditData
import akio.apps.myrun.domain.common.error.UnauthorizedUserError
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
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
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
        testee = UpdateUserProfileUsecase(
            userProfileRepository,
            userAuthenticationState
        )
    }

    @Test
    fun testUpdateProfileSuccess() {
        runBlockingTest {
            // given
            val userId = "userId"
            whenever(userAuthenticationState.requireUserAccountId()).thenReturn(userId)
            val editData = createProfileEditData()

            // when
            testee.updateUserProfile(editData)

            // then
            verify(userAuthenticationState).requireUserAccountId()
            verify(userProfileRepository).updateUserProfile(userId, editData)
        }
    }

    @Test
    fun testUpdateProfileWhenUserIsNotLoggedIn() {
        runBlockingTest {
            // given
            whenever(userAuthenticationState.requireUserAccountId()).thenReturn(null)

            // when
            val editData = createProfileEditData()
            try {
                testee.updateUserProfile(editData)
            } catch (ex: Throwable) {
                assertTrue(ex is UnauthorizedUserError)
            }

            // then
            verify(userAuthenticationState).requireUserAccountId()
            verify(userProfileRepository, never()).updateUserProfile(any(), eq(editData))
        }
    }

    private fun createProfileEditData(
        displayName: String = "displayName",
        birthdate: Long = 0,
        avatarUri: Uri? = null,
    ): ProfileEditData {
        return ProfileEditData(
            displayName,
            birthdate,
            Gender.Male,
            65f,
            avatarUri,
        )
    }
}

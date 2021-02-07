package akio.apps.myrun.feature.editprofile.impl

import akio.apps._base.MockAsynchronousTest
import akio.apps._base.Resource
import akio.apps._base.error.UnauthorizedUserError
import akio.apps.myrun.data.userprofile.model.Gender
import akio.apps.myrun.data.userprofile.model.ProfileEditData
import akio.apps.myrun.data.userprofile.model.UserProfile
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.UpdateUserProfileUsecase
import akio.apps.myrun.feature.editprofile.EditProfileViewModel
import akio.apps.myrun.feature.editprofile.UserPhoneNumberDelegate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when` as whenever

@ExperimentalCoroutinesApi
class EditProfileViewModelImplTest : MockAsynchronousTest() {

    private val originalPhoneNumber: String = "original phone number"

    @Mock
    lateinit var updateUserPhoneDelegate: UserPhoneNumberDelegate

    @Mock
    lateinit var updateUserProfileUsecase: UpdateUserProfileUsecase

    @Mock
    lateinit var getUserProfileUsecase: GetUserProfileUsecase

    lateinit var testee: EditProfileViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `given user profile, when create view model, user profile live data has correct value`() {
        // given
        val userProfile = createUserProfile()
        val liveDataUserProfile = flowOf(Resource.Success(userProfile))
        whenever(getUserProfileUsecase.getUserProfileFlow()).thenReturn(liveDataUserProfile)

        // when
        testee = createEditUserProfileViewModel()

        // then
        assertNull(testee.error.value)
        assertNull(testee.isInProgress.value)
        assertEquals(userProfile, testee.userProfile.value)
        verify(getUserProfileUsecase).getUserProfileFlow()
    }

    @Test
    fun `given view model created, when select update profile with same phone number, then profile update as expected`() {
        // given
        `given user profile, when create view model, user profile live data has correct value`()

        runBlocking {
            val editData = createProfileEditData(phoneNumber = originalPhoneNumber)
            whenever(updateUserProfileUsecase.updateUserProfile(editData)).thenReturn(Unit)

            // when
            testee.updateProfile(editData)

            // then
            assertNotNull(testee.updateProfileSuccess.value)
            verify(updateUserProfileUsecase).updateUserProfile(editData)
        }
    }

    @Test
    fun `given view model created, when select update profile with different phone number, then otp event triggered`() {
        // given
        `given user profile, when create view model, user profile live data has correct value`()

        runBlocking {
            val editData = createProfileEditData(phoneNumber = originalPhoneNumber + "999")
            whenever(updateUserProfileUsecase.updateUserProfile(editData)).thenReturn(Unit)

            // when
            testee.updateProfile(editData)

            // then
            assertNull(testee.updateProfileSuccess.value)
            assertNotNull(testee.openOtp.value)
            verify(updateUserProfileUsecase).updateUserProfile(editData)
        }
    }

    @Test
    fun `given view model created, when select update profile with blank user name, then blank user name error triggered`() {
        // given
        `given user profile, when create view model, user profile live data has correct value`()

        runBlocking {
            val editData =
                createProfileEditData(phoneNumber = originalPhoneNumber, displayName = "")

            // when
            testee.updateProfile(editData)

            // then
            assertNull(testee.updateProfileSuccess.value)
            assertNotNull(testee.blankEditDisplayNameError.value)
            verify(updateUserProfileUsecase, never()).updateUserProfile(editData)
        }
    }

    @Test
    fun `given view model created and user login state is invalid, when select update profile, then login required error triggered`() {
        // given
        `given user profile, when create view model, user profile live data has correct value`()

        runBlockingTest {
            val editData = createProfileEditData()

            whenever(updateUserProfileUsecase.updateUserProfile(editData))
                .thenAnswer { throw UnauthorizedUserError() }

            // when
            testee.updateProfile(editData)

            // then
            assertNull(testee.updateProfileSuccess.value)
            assertNotNull(testee.error.value)
            assertTrue(testee.error.value?.peekContent() is UnauthorizedUserError)
            verify(updateUserProfileUsecase).updateUserProfile(editData)
        }
    }

    private fun createProfileEditData(
        phoneNumber: String? = "edit phone number",
        displayName: String = "displayName"
    ): ProfileEditData {
        return ProfileEditData(
            displayName,
            Gender.male,
            170f,
            65f,
            null,
            phoneNumber
        )
    }

    private fun createEditUserProfileViewModel(): EditProfileViewModel {
        return EditProfileViewModelImpl(
            getUserProfileUsecase,
            updateUserProfileUsecase,
            updateUserPhoneDelegate
        )
    }

    private fun createUserProfile(phoneNumber: String? = originalPhoneNumber): UserProfile {
        return UserProfile(
            "accountId",
            "name",
            "email",
            phoneNumber,
            Gender.male,
            1.70f,
            65f,
            null
        )
    }
}

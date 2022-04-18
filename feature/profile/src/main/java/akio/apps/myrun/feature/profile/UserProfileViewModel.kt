package akio.apps.myrun.feature.profile

import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.eapps.api.model.ExternalProviders
import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.ProfileEditData
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.strava.DeauthorizeStravaUsecase
import akio.apps.myrun.domain.user.GetProviderTokensUsecase
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.UpdateUserProfileUsecase
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import akio.apps.myrun.worker.UploadStravaFileWorker
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

internal class UserProfileViewModel @Inject constructor(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val getProviderTokensUsecase: GetProviderTokensUsecase,
    private val deauthorizeStravaUsecase: DeauthorizeStravaUsecase,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
    private val updateUserProfileUsecase: UpdateUserProfileUsecase,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    /**
     * Presents the data of editing values in input fields. Null means no initial data fetched,
     * screen hasn't ever entered editing state.
     */
    private val editingFormDataMutableStateFlow: MutableStateFlow<UserProfileFormData?> =
        MutableStateFlow(savedStateHandle.getFormData())

    val screenStateFlow: Flow<ScreenState> = combine(
        getUserProfileUsecase.getUserProfileFlow(savedStateHandle.getUserId()),
        getProviderTokensUsecase.getProviderTokensFlow(),
        editingFormDataMutableStateFlow,
        ScreenState::create
    )

    fun deauthorizeStrava() {
        viewModelScope.launchCatching {
            deauthorizeStravaUsecase.deauthorizeStrava()

            UploadStravaFileWorker.clear(application)
        }
    }

    fun updateUserProfile() {
        val editingFormData = editingFormDataMutableStateFlow.value
        if (editingFormData?.isValid() != true)
            return
        updateUserProfileUsecase.updateUserProfile(editingFormData.makeProfileEditData())
    }

    fun onFormDataChanged(formData: UserProfileFormData) {
        editingFormDataMutableStateFlow.value = formData
        savedStateHandle.saveFormData(formData)
    }

    /**
     * Reads form data from saved state.
     */
    private fun SavedStateHandle.getFormData(): UserProfileFormData? {
        val name = get<String>(SAVED_STATE_USER_NAME) ?: return null
        val birthdate = get<Long>(SAVED_STATE_USER_BIRTHDATE) ?: return null
        val genderIdentity = get<Int>(SAVED_STATE_USER_GENDER) ?: return null
        val weight = get<String>(SAVED_STATE_USER_WEIGHT) ?: return null
        val photoUrl = get<String>(SAVED_STATE_USER_PHOTO_URL) ?: return null
        return UserProfileFormData(
            name,
            photoUrl,
            birthdate,
            Gender.create(genderIdentity),
            weight
        )
    }

    private fun SavedStateHandle.saveFormData(formData: UserProfileFormData) {
        this[SAVED_STATE_USER_NAME] = formData.name
        this[SAVED_STATE_USER_BIRTHDATE] = formData.birthdate
        this[SAVED_STATE_USER_GENDER] = formData.gender.identity
        this[SAVED_STATE_USER_WEIGHT] = formData.weight
        this[SAVED_STATE_USER_PHOTO_URL] = formData.photoUrl
    }

    private fun SavedStateHandle.getUserId(): String? = this.get<String>(SAVED_STATE_USER_ID)

    enum class StravaLinkingState { Linked, NotLinked, Unknown }

    sealed class ScreenState {
        object Loading : ScreenState()
        class ErrorRetry(val exception: Throwable) : ScreenState()
        class FormState(
            /**
             * Data for values that are editing in input fields.
             */
            val editingFormData: UserProfileFormData,

            /**
             * Status of Strava account link.
             */
            val stravaLinkingState: StravaLinkingState,
        ) : ScreenState()

        companion object {
            fun create(
                userProfileRes: Resource<UserProfile>,
                eappTokensRes: Resource<out ExternalProviders>,
                editingFormData: UserProfileFormData?,
            ): ScreenState = when (userProfileRes) {
                is Resource.Loading -> Loading
                is Resource.Error -> ErrorRetry(userProfileRes.exception)
                is Resource.Success -> {
                    val stravaLinkingState = when {
                        eappTokensRes is Resource.Error -> StravaLinkingState.Unknown
                        eappTokensRes.data?.strava != null -> StravaLinkingState.Linked
                        eappTokensRes.data?.strava == null -> StravaLinkingState.NotLinked
                        else -> StravaLinkingState.Unknown
                    }
                    FormState(
                        editingFormData ?: UserProfileFormData.create(userProfileRes.data),
                        stravaLinkingState
                    )
                }
            }
        }
    }

    data class UserProfileFormData constructor(
        val name: String,
        val photoUrl: String?,
        val birthdate: Long,
        val gender: Gender,
        val weight: String,
    ) {
        fun makeProfileEditData(): ProfileEditData {
            return ProfileEditData(
                displayName = name,
                birthdate = birthdate,
                gender = gender,
                weight = weight.toFloatOrNull()
            )
        }

        fun isNameValid(): Boolean = name.isNotBlank()

        fun isValid(): Boolean = isNameValid()

        companion object {
            fun create(userProfile: UserProfile): UserProfileFormData = UserProfileFormData(
                name = userProfile.name,
                photoUrl = userProfile.photo,
                birthdate = userProfile.birthdate,
                gender = userProfile.gender,
                weight = userProfile.weight.toString()
            )
        }
    }

    companion object {
        private const val SAVED_STATE_USER_ID = "SAVED_STATE_USER_ID"
        private const val SAVED_STATE_USER_NAME = "SAVED_STATE_USER_NAME"
        private const val SAVED_STATE_USER_BIRTHDATE = "SAVED_STATE_USER_BIRTHDATE"
        private const val SAVED_STATE_USER_GENDER = "SAVED_STATE_USER_GENDER"
        private const val SAVED_STATE_USER_WEIGHT = "SAVED_STATE_USER_WEIGHT"
        private const val SAVED_STATE_USER_PHOTO_URL = "SAVED_STATE_USER_PHOTO_URL"

        fun setInitialSavedState(handle: SavedStateHandle, userId: String?): SavedStateHandle {
            handle[SAVED_STATE_USER_ID] = userId
            return handle
        }
    }
}

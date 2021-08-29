package akio.apps.myrun.feature.profile

import akio.apps.common.data.LaunchCatchingDelegate
import akio.apps.common.data.Resource
import akio.apps.myrun.data.activity.api.ActivityLocalStorage
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.eapps.api.model.ExternalProviders
import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.ProfileEditData
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.domain.strava.DeauthorizeStravaUsecase
import akio.apps.myrun.domain.strava.RemoveStravaTokenUsecase
import akio.apps.myrun.domain.user.GetProviderTokensUsecase
import akio.apps.myrun.domain.user.GetUserProfileUsecase
import akio.apps.myrun.domain.user.UpdateUserProfileUsecase
import akio.apps.myrun.worker.UploadStravaFileWorker
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class UserProfileViewModel @Inject constructor(
    private val application: Application,
    private val arguments: Arguments,
    private val getUserProfileUsecase: GetUserProfileUsecase,
    private val getProviderTokensUsecase: GetProviderTokensUsecase,
    private val deauthorizeStravaUsecase: DeauthorizeStravaUsecase,
    private val removeStravaTokenUsecase: RemoveStravaTokenUsecase,
    private val userAuthenticationState: UserAuthenticationState,
    private val activityLocalStorage: ActivityLocalStorage,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
    private val updateUserProfileUsecase: UpdateUserProfileUsecase,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    val userProfileResourceFlow: Flow<Resource<UserProfile>> =
        flow { emit(getUserProfileUsecase.getUserProfileResource(arguments.userId)) }

    val tokenProvidersFlow: Flow<Resource<out ExternalProviders>> =
        getProviderTokensUsecase.getProviderTokensFlow()

    /**
     * Presents the data of editing values in input fields. Null means no initial data fetched,
     * screen hasn't ever entered editing state.
     */
    private val editingUserProfileFormDataMutableStateFlow: MutableStateFlow<UserProfileFormData?> =
        MutableStateFlow(null)
    val editingUserProfileFormDataFlow: Flow<UserProfileFormData?> =
        editingUserProfileFormDataMutableStateFlow

    val userProfileScreenStateFlow: Flow<UserProfileScreenState> =
        combine(
            flow { emit(getUserProfileUsecase.getUserProfileResource(arguments.userId)) },
            getProviderTokensUsecase.getProviderTokensFlow(),
            editingUserProfileFormDataMutableStateFlow
        ) { initialUserProfileData, eappTokensResource, editingUserProfileFormData ->
            UserProfileScreenState.create(
                initialUserProfileData,
                editingUserProfileFormData,
                eappTokensResource
            )
        }

    suspend fun getActivityUploadCount(): Int =
        activityLocalStorage.getActivityStorageDataCountFlow().first()

    suspend fun getUserProfileResource(): Resource<UserProfile> =
        getUserProfileUsecase.getUserProfileResource(arguments.userId)

    fun isCurrentUser(): Boolean =
        arguments.userId == userAuthenticationState.getUserAccountId() ||
            arguments.userId == null

    fun deauthorizeStrava() {
        viewModelScope.launchCatching {
            deauthorizeStravaUsecase.deauthorizeStrava()
            removeStravaTokenUsecase.removeStravaToken()

            UploadStravaFileWorker.clear(application)
        }
    }

    fun updateUserProfile() {
        val editingFormData = editingUserProfileFormDataMutableStateFlow.value
        if (editingFormData?.isValid() != true)
            return
        updateUserProfileUsecase.updateUserProfile(editingFormData.makeProfileEditData())
    }

    fun onFormDataChanged(formData: UserProfileFormData) {
        editingUserProfileFormDataMutableStateFlow.value = formData
    }

    data class Arguments(val userId: String?)

    enum class StravaLinkingState { Linked, NotLinked, Unknown }

    sealed class UserProfileScreenState {
        object Loading : UserProfileScreenState()
        class ErrorRetry(val exception: Throwable) : UserProfileScreenState()
        class FormState(
            /**
             * Data for values that are editing in input fields.
             */
            val editingFormData: UserProfileFormData,

            /**
             * Status of Strava account link.
             */
            val stravaLinkingState: StravaLinkingState,
        ) : UserProfileScreenState()

        companion object {
            fun create(
                userProfileRes: Resource<UserProfile>,
                editingFormData: UserProfileFormData?,
                eappTokensRes: Resource<out ExternalProviders>,
            ): UserProfileScreenState = when (userProfileRes) {
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

    data class UserProfileFormData private constructor(
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
}

package akio.apps.myrun.feature.editprofile.impl

import akio.apps._base.error.LoginSessionExpiredError
import akio.apps._base.error.UnauthorizedUserError
import akio.apps._base.lifecycle.Event
import akio.apps.myrun.data.userprofile.entity.FirestoreUserProfileUpdateMapEntity
import akio.apps.myrun.data.userprofile.impl.UserProfileRepositoryImpl.Companion.FIRESTORE_USERS_DOCUMENT
import akio.apps.myrun.feature.editprofile.UserPhoneNumberDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class FirebaseUserPhoneNumberDelegate @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
): UserPhoneNumberDelegate, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val _isUpdatePhoneNumberSuccess = MutableLiveData<Event<Unit>>()
    override val isUpdatePhoneNumberSuccess: LiveData<Event<Unit>> = _isUpdatePhoneNumberSuccess

    private val _isUpdatingPhoneNumber = MutableLiveData<Boolean>()
    override val isUpdatingPhoneNumber: LiveData<Boolean> = _isUpdatingPhoneNumber

    private val _updatePhoneError = MutableLiveData<Event<Throwable>>()
    override val updatePhoneError: LiveData<Event<Throwable>> = _updatePhoneError

    private val _phoneNumberReauthenticateError = MutableLiveData<Event<Throwable>>()
    override val phoneNumberReauthenticateError: LiveData<Event<Throwable>> = _phoneNumberReauthenticateError

    private fun getUserInfoDocument(userId: String): DocumentReference {
        return firebaseFirestore.collection(FIRESTORE_USERS_DOCUMENT)
            .document(userId)
    }

    override fun updatePhoneNumber(phoneAuthCredential: PhoneAuthCredential) {
        launch {
            _isUpdatingPhoneNumber.postValue(true)
            val currentUser = firebaseAuth.currentUser

            if (currentUser == null) {
                _updatePhoneError.postValue(Event(UnauthorizedUserError()))
                return@launch
            }

            try {
                currentUser.updatePhoneNumber(phoneAuthCredential).await()
            } catch (expiredException: FirebaseAuthRecentLoginRequiredException) {
                _phoneNumberReauthenticateError.postValue(Event(LoginSessionExpiredError()))
                return@launch
            }

            currentUser.phoneNumber ?. let { currentNumber ->
                val updateMap = FirestoreUserProfileUpdateMapEntity()
                    .apply {
                        phoneNumber(currentNumber)
                    }

                getUserInfoDocument(currentUser.uid).set(updateMap.profile, SetOptions.merge()).await()
            }

            _isUpdatePhoneNumberSuccess.postValue(Event(Unit))
        }
            .invokeOnCompletion {
                _isUpdatingPhoneNumber.postValue(false)
            }
    }
}
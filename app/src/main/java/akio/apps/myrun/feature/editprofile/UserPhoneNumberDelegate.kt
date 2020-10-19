package akio.apps.myrun.feature.editprofile

import akio.apps._base.lifecycle.Event
import androidx.lifecycle.LiveData
import com.google.firebase.auth.PhoneAuthCredential

interface UserPhoneNumberDelegate {
    val isUpdatingPhoneNumber: LiveData<Boolean>
    val isUpdatePhoneNumberSuccess: LiveData<Event<Unit>>
    val updatePhoneError: LiveData<Event<Throwable>>
    val phoneNumberReauthenticateError: LiveData<Event<Throwable>>

    fun updatePhoneNumber(phoneAuthCredential: PhoneAuthCredential)
}
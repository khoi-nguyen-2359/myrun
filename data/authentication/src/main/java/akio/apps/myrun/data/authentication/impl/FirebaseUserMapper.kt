package akio.apps.myrun.data.authentication.impl

import akio.apps.myrun.data.authentication.api.model.UserAccount
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

class FirebaseUserMapper @Inject constructor() {
    fun map(input: FirebaseUser): UserAccount {
        return UserAccount(
            input.uid,
            input.email,
            input.displayName,
            input.photoUrl?.toString(),
            input.phoneNumber
        )
    }
}
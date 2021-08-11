package akio.apps.myrun.data.user.impl.mapper

import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.data.user.impl.model.FirestoreUserProfile
import javax.inject.Inject

class FirestoreUserProfileMapper @Inject constructor() {
    fun map(input: FirestoreUserProfile): UserProfile {
        return input.run {
            UserProfile(
                uid,
                displayName,
                email,
                phoneNumber,
                Gender.parse(gender),
                height,
                weight,
                photoUrl
            )
        }
    }
}

package akio.apps.myrun.data.userprofile.mapper

import akio.apps.myrun.data.userprofile.entity.FirestoreUserProfileEntity
import akio.apps.myrun.data.userprofile.model.Gender
import akio.apps.myrun.data.userprofile.model.UserProfile
import javax.inject.Inject

class FirestoreUserProfileMapper @Inject constructor() {
    fun map(input: FirestoreUserProfileEntity): UserProfile {
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

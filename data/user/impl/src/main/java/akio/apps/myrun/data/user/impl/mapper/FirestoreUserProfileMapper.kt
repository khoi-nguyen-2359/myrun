package akio.apps.myrun.data.user.impl.mapper

import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.data.user.impl.model.FirestoreUserGender
import akio.apps.myrun.data.user.impl.model.FirestoreUserProfile
import javax.inject.Inject

class FirestoreUserProfileMapper @Inject constructor() {
    fun map(input: FirestoreUserProfile): UserProfile {
        val gender = when (FirestoreUserGender.parse(input.genderId)) {
            FirestoreUserGender.Male -> Gender.Male
            FirestoreUserGender.Female -> Gender.Female
            FirestoreUserGender.Others -> Gender.Others
        }
        return input.run {
            UserProfile(
                uid,
                displayName,
                gender,
                weight,
                photoUrl,
                birthdate = input.birthdate
            )
        }
    }
}

package akio.apps.myrun.data.user.impl.mapper

import akio.apps.myrun.data.user.api.model.Gender
import akio.apps.myrun.data.user.api.model.UserProfile
import akio.apps.myrun.data.user.impl.model.FirestoreUser
import akio.apps.myrun.data.user.impl.model.FirestoreUserGender
import javax.inject.Inject

class FirestoreUserProfileMapper @Inject constructor() {
    /**
     * [userPhotoUrl] is generated value of the url.
     */
    fun map(input: FirestoreUser): UserProfile {
        val (profile, recentActivity) = input
        val gender = when (FirestoreUserGender.parse(profile.genderId)) {
            FirestoreUserGender.Male -> Gender.Male
            FirestoreUserGender.Female -> Gender.Female
            FirestoreUserGender.Others -> Gender.Others
        }
        return profile.run {
            UserProfile(
                uid,
                displayName,
                gender,
                weight,
                photoUrl,
                birthdate,
                lastActiveTime = recentActivity.activeTime ?: 0
            )
        }
    }
}

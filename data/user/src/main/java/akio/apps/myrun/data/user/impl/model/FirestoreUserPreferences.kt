package akio.apps.myrun.data.user.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreUserPreferences(
    @PropertyName(FirestoreUserPreferencesFields.SHOW_ACTIVITY_MAP_ON_FEED)
    val showActivityMapOnFeed: Boolean = DEFAULT_MAP_VISIBILITY_VALUE,
) {
    companion object {
        const val DEFAULT_MAP_VISIBILITY_VALUE = true
    }
}

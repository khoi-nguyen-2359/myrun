package akio.apps.myrun.data.user.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreUserPreferencesUpdateMap(
    @PropertyName("preferences")
    val preferences: MutableMap<String, Any> = mutableMapOf(),
) {
    fun isMapVisible(value: Boolean) {
        preferences[FirestoreUserPreferencesFields.SHOW_ACTIVITY_MAP_ON_FEED] = value
    }
}

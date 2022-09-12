package akio.apps.myrun.data.user.impl.model

import com.google.firebase.firestore.PropertyName

data class FirestoreUserRecentActivityUpdateMap(
    @PropertyName("recentActivity")
    val recentActivity: MutableMap<String, Any> = mutableMapOf(),
) {
    fun place(value: List<String>) {
        recentActivity[FirestoreUserRecentActivityFields.FIELD_PLACE] = value
    }

    fun activeTime(value: Long) {
        recentActivity[FirestoreUserRecentActivityFields.FIELD_ACTIVE_TIME] = value
    }
}

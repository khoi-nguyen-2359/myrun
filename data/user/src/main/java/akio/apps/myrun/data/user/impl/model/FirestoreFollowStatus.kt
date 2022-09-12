package akio.apps.myrun.data.user.impl.model

enum class FirestoreFollowStatus(val rawValue: Int) {
    Requested(1), Accepted(2);

    companion object {
        fun from(value: Int): FirestoreFollowStatus = when (value) {
            1 -> Requested
            else -> Accepted
        }
    }
}

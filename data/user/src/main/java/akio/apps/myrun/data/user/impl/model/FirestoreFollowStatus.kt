package akio.apps.myrun.data.user.impl.model

enum class FirestoreFollowStatus(val value: Int) {
    Requested(1), Followed(2);

    companion object {
        fun from(value: Int): FirestoreFollowStatus = when (value) {
            1 -> Requested
            else -> Followed
        }
    }
}

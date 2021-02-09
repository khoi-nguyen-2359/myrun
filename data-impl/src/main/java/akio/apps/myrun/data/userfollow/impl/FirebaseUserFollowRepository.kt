package akio.apps.myrun.data.userfollow.impl

import akio.apps.myrun.data.userfollow.UserFollowRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class FirebaseUserFollowRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserFollowRepository {
    private val userFollowCollection
        get() = firestore.collection("userfollow")

    override fun getUserFollowings(userId: String): List<String> {
        return emptyList()
    }

    override fun getUserFollowers(userId: String): List<String> {
        return emptyList()
    }

    override fun followUser(userId: String, followUserId: String) {
        TODO("Not yet implemented")
    }

    override fun unfollowUser(userId: String, unfollowUserId: String) {
        TODO("Not yet implemented")
    }
}

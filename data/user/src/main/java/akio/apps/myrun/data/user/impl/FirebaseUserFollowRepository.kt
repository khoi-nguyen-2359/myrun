package akio.apps.myrun.data.user.impl

import akio.apps.myrun.data.authentication.di.AuthenticationDataScope
import akio.apps.myrun.data.user.api.UserFollowRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ContributesBinding(AuthenticationDataScope::class)
class FirebaseUserFollowRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
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

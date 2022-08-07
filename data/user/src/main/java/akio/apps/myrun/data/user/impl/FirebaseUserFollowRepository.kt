package akio.apps.myrun.data.user.impl

import akio.apps.myrun.data.authentication.di.AuthenticationDataScope
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.model.UserFollow
import akio.apps.myrun.data.user.impl.model.FirestoreUserFollow
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@Singleton
@ContributesBinding(AuthenticationDataScope::class)
class FirebaseUserFollowRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserFollowRepository {
    private fun getUserFollowingsCollection(userAccountId: String): CollectionReference {
        return firestore.collection("$USERS_COLLECTION/$userAccountId/$USER_FOLLOWINGS_COLLECTION")
    }

    override fun getUserFollowingsFlow(userId: String): Flow<List<UserFollow>> = callbackFlow {
        val userFollowingsCollection = getUserFollowingsCollection(userId)
        try {
            userFollowingsCollection.get(Source.CACHE).await().documents
                .mapNotNull {
                    it.toObject(FirestoreUserFollow::class.java)?.toUserFollow(it.id)
                }.let { cachedFollowings ->
                    send(cachedFollowings)
                    Timber.d("getUserFollowings from cache. size = ${cachedFollowings.size}")
                }
        } catch (ex: Exception) {
            // handle cache not found
            Timber.d("getUserFollowings cache not found.")
        }

        val listener = userFollowingsCollection.addSnapshotListener { snapshot, error ->
            if (error == null) {
                snapshot?.documents
                    ?.mapNotNull {
                        it.toObject(FirestoreUserFollow::class.java)?.toUserFollow(it.id)
                    }
                    ?.let(::trySendBlocking)
            }
        }

        awaitClose {
            runBlocking(Dispatchers.Main.immediate) {
                listener.remove()
            }
        }
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

    private fun FirestoreUserFollow.toUserFollow(uid: String) =
        UserFollow(uid, displayName, photoUrl)

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USER_FOLLOWINGS_COLLECTION = "userFollowings"
        private const val USER_FOLLOWERS_COLLECTION = "userFollowers"
    }
}

package akio.apps.myrun.data.user.impl

import akio.apps.myrun.data.authentication.di.AuthenticationDataScope
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.model.FollowStatus
import akio.apps.myrun.data.user.api.model.UserFollow
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion
import akio.apps.myrun.data.user.impl.model.FirestoreFollowStatus
import akio.apps.myrun.data.user.impl.model.FirestoreUser
import akio.apps.myrun.data.user.impl.model.FirestoreUserFollow
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
@ContributesBinding(AuthenticationDataScope::class)
class FirebaseUserFollowRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserFollowRepository {

    private fun getUserFollowingsCollection(userAccountId: String): CollectionReference {
        return firestore.collection("$USERS_COLLECTION/$userAccountId/$USER_FOLLOWINGS_COLLECTION")
    }

    override suspend fun getUserFollowings(userId: String): List<UserFollow> =
        getUserFollowingsCollection(userId).get().await().documents
            .mapNotNull {
                it.toObject(FirestoreUserFollow::class.java)?.toUserFollow(it.id)
            }

    override suspend fun getUserFollowByRecentActivity(
        userId: String,
        placeComponent: String,
        limit: Long,
        startAfterActiveTime: Long,
    ): List<UserFollowSuggestion> = firestore.collection(USERS_COLLECTION)
        .whereArrayContains(USER_RECENT_PLACE_FIELD, placeComponent)
        .orderBy(USER_RECENT_ACTIVE_TIME_FIELD, Query.Direction.DESCENDING)
        .startAfter(startAfterActiveTime)
        .limit(limit)
        .get().await()
        .documents.mapNotNull { doc ->
            if (doc.id == userId) {
                return@mapNotNull null
            }
            val (userProfile, recentActivity) = doc.toObject(FirestoreUser::class.java)
                ?: return@mapNotNull null
            UserFollowSuggestion(
                userProfile.uid,
                userProfile.displayName,
                userProfile.photoUrl,
                recentActivity.activeTime ?: 0
            )
        }

    override fun getUserFollowers(userId: String): List<String> {
        return emptyList()
    }

    override suspend fun followUser(userId: String, followSuggestion: UserFollowSuggestion) {
        val followUserDoc = getUserFollowingsCollection(userId).document(followSuggestion.uid)
        val entry = FirestoreUserFollow(
            displayName = followSuggestion.displayName,
            photoUrl = followSuggestion.photoUrl,
            status = FirestoreFollowStatus.Requested.value
        )
        followUserDoc.set(entry).await()
    }

    override fun unfollowUser(userId: String, unfollowUserId: String) {
        TODO("Not yet implemented")
    }

    private fun FirestoreUserFollow.toUserFollow(uid: String) =
        UserFollow(uid, displayName, photoUrl, toUserFollowStatus(status))

    private fun toUserFollowStatus(status: Int): FollowStatus =
        when (FirestoreFollowStatus.from(status)) {
            FirestoreFollowStatus.Requested -> FollowStatus.Requested
            FirestoreFollowStatus.Accepted -> FollowStatus.Accepted
        }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USER_FOLLOWINGS_COLLECTION = "followings"
        private const val USER_FOLLOWERS_COLLECTION = "followers"
        private const val USER_RECENT_PLACE_FIELD = "recentActivity.placeComponents"
        private const val USER_RECENT_ACTIVE_TIME_FIELD = "recentActivity.activeTime"
    }
}

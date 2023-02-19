package akio.apps.myrun.data.user.impl

import akio.apps.myrun.data.authentication.di.AuthenticationDataScope
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.model.FollowStatus
import akio.apps.myrun.data.user.api.model.UserFollow
import akio.apps.myrun.data.user.api.model.UserFollowCounter
import akio.apps.myrun.data.user.api.model.UserFollowPagingToken
import akio.apps.myrun.data.user.api.model.UserFollowSuggestion
import akio.apps.myrun.data.user.api.model.UserFollowType
import akio.apps.myrun.data.user.impl.model.FirestoreCounter
import akio.apps.myrun.data.user.impl.model.FirestoreFollowStatus
import akio.apps.myrun.data.user.impl.model.FirestoreUser
import akio.apps.myrun.data.user.impl.model.FirestoreUserFollow
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

@Singleton
@ContributesBinding(AuthenticationDataScope::class)
class FirebaseUserFollowRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserFollowRepository {

    private fun getUserFollowingsCollection(userAccountId: String): CollectionReference {
        return firestore.collection("$USERS_COLLECTION/$userAccountId/$USER_FOLLOWINGS_COLLECTION")
    }

    private fun getUserFollowersCollection(userAccountId: String): CollectionReference {
        return firestore.collection("$USERS_COLLECTION/$userAccountId/$USER_FOLLOWERS_COLLECTION")
    }

    private fun getCounterDoc(userId: String, counterName: String) =
        firestore.document("$USERS_COLLECTION/$userId/$COUNTERS_COLLECTION/$counterName")

    override suspend fun getUserFollows(
        userId: String,
        followType: UserFollowType,
        limit: Int,
        pagingToken: UserFollowPagingToken?,
    ): List<UserFollow> {
        val collectionRef = when (followType) {
            UserFollowType.Following -> getUserFollowingsCollection(userId)
            UserFollowType.Follower -> getUserFollowersCollection(userId)
        }
        return collectionRef
            .orderBy(USER_FOLLOW_STATUS_FIELD)
            .orderBy(USER_FOLLOW_NAME_FIELD)
            .orderBy(USER_FOLLOW_UID_FIELD)
            .run {
                if (pagingToken != null) {
                    startAfter(
                        pagingToken.status.toFirestoreFollowStatus().rawValue,
                        pagingToken.userName,
                        pagingToken.userId
                    )
                } else {
                    this
                }
            }
            .limit(limit.toLong())
            .get().await()
            .documents.mapNotNull { it.toObject(FirestoreUserFollow::class.java)?.toUserFollow() }
    }

    override suspend fun getUserFollowings(userId: String, useCache: Boolean): List<UserFollow> {
        val cache = if (useCache) {
            getUserFollowingsFromSource(userId, Source.CACHE).takeIf { it.isNotEmpty() }
        } else {
            null
        }

        return cache ?: getUserFollowingsFromSource(userId)
    }

    /**
     * Using [Source.CACHE] for [source] returns empty list in case no data in cache.
     */
    private suspend fun getUserFollowingsFromSource(
        userId: String,
        source: Source = Source.DEFAULT
    ) = getUserFollowingsCollection(userId).get(source).await().documents
        .mapNotNull { it.toObject(FirestoreUserFollow::class.java)?.toUserFollow() }

    override fun getUserFollowingsFlow(userId: String): Flow<List<UserFollow>> = callbackFlow {
        val listenerReg = getUserFollowingsCollection(userId).addSnapshotListener { value, _ ->
            val userFollowings =
                value?.mapNotNull { it.toObject(FirestoreUserFollow::class.java).toUserFollow() }
                    ?: emptyList()
            trySend(userFollowings)
        }

        awaitClose {
            runBlocking(Dispatchers.Main.immediate) {
                listenerReg.remove()
            }
        }
    }

    override suspend fun getIsFollowing(userId: String, targetId: String): Boolean? = try {
        val value = getUserFollowingsCollection(userId).document(targetId).get().await()
        value?.exists() ?: false
    } catch (ex: Exception) {
        null
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

    override suspend fun getUserFollowers(userId: String): List<UserFollow> =
        getUserFollowersCollection(userId).get().await().documents
            .mapNotNull {
                it.toObject(FirestoreUserFollow::class.java)?.toUserFollow()
            }

    override fun getUserFollowCounterFlow(userId: String): Flow<UserFollowCounter> = callbackFlow {
        val followerCounterDoc = getCounterDoc(userId, USER_FOLLOWERS_COLLECTION)
        var followerCounter = 0
        var followingCounter = 0
        val followerListener = followerCounterDoc.addSnapshotListener { snapshot, _ ->
            followerCounter =
                snapshot?.toObject(FirestoreCounter::class.java)?.count ?: followerCounter
            trySend(UserFollowCounter(followingCounter, followerCounter))
        }
        val followingCounterDoc = getCounterDoc(userId, USER_FOLLOWINGS_COLLECTION)
        val followingListener = followingCounterDoc.addSnapshotListener { snapshot, _ ->
            followingCounter =
                snapshot?.toObject(FirestoreCounter::class.java)?.count ?: followingCounter
            trySend(UserFollowCounter(followingCounter, followerCounter))
        }

        awaitClose {
            runBlocking(Dispatchers.Main.immediate) {
                followerListener.remove()
                followingListener.remove()
            }
        }
    }

    override suspend fun followUser(userId: String, followSuggestion: UserFollowSuggestion) {
        val followUserDoc = getUserFollowingsCollection(userId).document(followSuggestion.uid)
        val entry = FirestoreUserFollow(
            followSuggestion.uid,
            followSuggestion.displayName,
            followSuggestion.photoUrl,
            FirestoreFollowStatus.Requested.rawValue
        )
        val followingCounterDoc = getCounterDoc(userId, USER_FOLLOWINGS_COLLECTION)
        val counterUpdateMap = mapOf(COUNTERS_COUNT_FIELD to FieldValue.increment(1))
        firestore.batch()
            .set(followUserDoc, entry)
            .set(followingCounterDoc, counterUpdateMap, SetOptions.merge())
            .commit()
            .await()
    }

    override suspend fun acceptFollower(userId: String, followerId: String) {
        getUserFollowersCollection(userId)
            .document(followerId)
            .set(
                mapOf(USER_FOLLOW_STATUS_FIELD to FirestoreFollowStatus.Accepted.rawValue),
                SetOptions.merge()
            )
            .await()
    }

    override suspend fun deleteFollower(userId: String, followerId: String) {
        val followerDoc = getUserFollowersCollection(userId).document(followerId)
        val counterUpdateMap = mapOf(COUNTERS_COUNT_FIELD to FieldValue.increment(-1))
        val counterDoc = getCounterDoc(userId, USER_FOLLOWERS_COLLECTION)
        firestore.batch()
            .delete(followerDoc)
            .set(counterDoc, counterUpdateMap, SetOptions.merge())
            .commit()
            .await()
    }

    override suspend fun unfollowUser(userId: String, unfollowUserId: String) {
        val followingsCollection = getUserFollowingsCollection(userId)
        val unfollowDoc = followingsCollection.document(unfollowUserId)
        val followingCounterDoc =
            firestore.document(
                "$USERS_COLLECTION/$userId/$COUNTERS_COLLECTION/$USER_FOLLOWINGS_COLLECTION"
            )
        val counterUpdateMap = mapOf(COUNTERS_COUNT_FIELD to FieldValue.increment(-1))
        firestore.batch()
            .delete(unfollowDoc)
            .set(followingCounterDoc, counterUpdateMap, SetOptions.merge())
            .commit()
            .await()
    }

    private fun FirestoreUserFollow.toUserFollow() =
        UserFollow(uid, displayName, photoUrl, toUserFollowStatus(status))

    private fun FollowStatus.toFirestoreFollowStatus() = when (this) {
        FollowStatus.Accepted -> FirestoreFollowStatus.Accepted
        FollowStatus.Requested -> FirestoreFollowStatus.Requested
    }

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

        private const val COUNTERS_COLLECTION = "counters"
        private const val COUNTERS_COUNT_FIELD = "count"

        private const val USER_FOLLOW_UID_FIELD = "uid"
        private const val USER_FOLLOW_NAME_FIELD = "displayName"
        private const val USER_FOLLOW_STATUS_FIELD = "status"
    }
}

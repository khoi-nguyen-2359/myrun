package akio.apps.myrun.data.user.impl

import akio.apps.myrun.data.authentication.di.AuthenticationDataScope
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.UserRecentActivityRepository
import akio.apps.myrun.data.user.api.model.PlaceIdentifier
import akio.apps.myrun.data.user.impl.model.FirestoreUser
import akio.apps.myrun.data.user.impl.model.FirestoreUserRecentActivityUpdateMap
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.squareup.anvil.annotations.ContributesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ContributesBinding(AuthenticationDataScope::class)
class FirebaseUserRecentActivityRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserRecentActivityRepository {

    private val usersCollection: CollectionReference
        get() = firestore.collection(USERS_COLLECTION)

    override suspend fun saveRecentPlace(userId: String, areaIdentifier: PlaceIdentifier) {
        val updateEntry = FirestoreUserRecentActivityUpdateMap().apply {
            place(areaIdentifier.addressComponents)
            activeTime(System.currentTimeMillis())
        }
        usersCollection.document(userId)
            .set(updateEntry, SetOptions.merge())
            .await()
    }

    override suspend fun getRecentPlaceIdentifier(
        userId: String,
        useCache: Boolean,
    ): PlaceIdentifier? {
        val cache = if (useCache) {
            try {
                getRecentPlaceIdentifierFromSource(userId)
            } catch (ex: Exception) {
                null
            }
        } else {
            null
        }

        return cache ?: getRecentPlaceIdentifierFromSource(userId, Source.DEFAULT)
    }

    /**
     * Using [Source.CACHE] for [source] will throw exception if no data in cache.
     */
    @Throws(Exception::class)
    private suspend fun getRecentPlaceIdentifierFromSource(
        userId: String,
        source: Source = Source.DEFAULT,
    ) = usersCollection.document(userId)
        .get(source)
        .await()
        .toObject(FirestoreUser::class.java)
        ?.recentActivity
        ?.placeComponents?.let(PlaceIdentifier::fromAddressComponents)

    override fun getRecentPlaceIdentifierFlow(
        userId: String,
    ): Flow<Resource<out PlaceIdentifier?>> = callbackFlow {
        val listener = withContext(Dispatchers.Main.immediate) {
            usersCollection.document(userId).addSnapshotListener { snapshot, error ->
                val placeIdentifier =
                    snapshot?.toObject(FirestoreUser::class.java)
                        ?.recentActivity?.placeComponents?.let(
                            PlaceIdentifier::fromAddressComponents
                        )
                        ?: return@addSnapshotListener
                trySendBlocking(Resource.Success(placeIdentifier))
                error?.let {
                    trySendBlocking(Resource.Error<PlaceIdentifier>(it))
                    close(it)
                }
            }
        }

        awaitClose {
            runBlocking(Dispatchers.Main.immediate) {
                listener.remove()
            }
        }
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val USER_RECENT_PLACE_FIELD = "recentActivity.placeComponents"
        private const val USER_RECENT_ACTIVE_TIME_FIELD = "recentActivity.activeTime"
    }
}

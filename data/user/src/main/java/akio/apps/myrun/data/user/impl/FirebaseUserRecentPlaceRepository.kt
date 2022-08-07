package akio.apps.myrun.data.user.impl

import akio.apps.myrun.data.authentication.di.AuthenticationDataScope
import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.PlaceIdentifier
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.data.user.impl.model.FirestoreUser
import akio.apps.myrun.data.user.impl.model.FirestoreUserRecentActivityUpdateMap
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
import kotlinx.coroutines.withContext

@Singleton
@ContributesBinding(AuthenticationDataScope::class)
class FirebaseUserRecentPlaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserRecentPlaceRepository {

    private val usersCollection: CollectionReference
        get() = firestore.collection(USERS_COLLECTION)

    override suspend fun saveRecentPlace(userId: String, areaIdentifier: PlaceIdentifier) {
        val updateEntry = FirestoreUserRecentActivityUpdateMap().apply {
            place(areaIdentifier)
            activeTime(System.currentTimeMillis())
        }
        usersCollection.document(userId)
            .set(updateEntry, SetOptions.merge())
            .await()
    }

    override suspend fun getRecentPlaceIdentifier(userId: String): PlaceIdentifier? =
        usersCollection.document(userId)
            .get()
            .await()
            .toObject(FirestoreUser::class.java)
            ?.recentActivity
            ?.place

    override fun getRecentPlaceIdentifierFlow(
        userId: String,
    ): Flow<Resource<out PlaceIdentifier?>> = callbackFlow {
        val listener = withContext(Dispatchers.Main.immediate) {
            usersCollection.document(userId).addSnapshotListener { snapshot, error ->
                val placeIdentifier =
                    snapshot?.toObject(FirestoreUser::class.java)?.recentActivity?.place
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
    }
}

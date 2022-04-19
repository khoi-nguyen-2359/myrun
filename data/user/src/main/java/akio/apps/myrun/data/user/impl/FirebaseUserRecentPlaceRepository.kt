package akio.apps.myrun.data.user.impl

import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.user.api.PlaceIdentifier
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.data.user.impl.model.FirestorePlaceIdentifier
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Singleton
class FirebaseUserRecentPlaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserRecentPlaceRepository {

    private val recentPlaceCollection: CollectionReference
        get() = firestore.collection("recent_place")

    override suspend fun saveRecentPlace(userId: String, areaIdentifier: PlaceIdentifier) {
        val firestoreRecentPlace = FirestorePlaceIdentifier(areaIdentifier)
        recentPlaceCollection.document(userId)
            .set(firestoreRecentPlace)
            .await()
    }

    override suspend fun getRecentPlaceIdentifier(userId: String): PlaceIdentifier? =
        recentPlaceCollection.document(userId)
            .get()
            .await()
            .toObject(FirestorePlaceIdentifier::class.java)
            ?.placeIdentifier

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getRecentPlaceIdentifierFlow(
        userId: String,
    ): Flow<Resource<out PlaceIdentifier?>> =
        callbackFlow {
            val listener = withContext(Dispatchers.Main.immediate) {
                recentPlaceCollection.document(userId).addSnapshotListener { snapshot, error ->
                    val placeIdentifier =
                        snapshot?.toObject(FirestorePlaceIdentifier::class.java)?.placeIdentifier
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
}

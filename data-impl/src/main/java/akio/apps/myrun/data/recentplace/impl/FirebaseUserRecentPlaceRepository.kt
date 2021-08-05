package akio.apps.myrun.data.recentplace.impl

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.recentplace.PlaceIdentifier
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.data.recentplace.model.FirestorePlaceIdentifier
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseUserRecentPlaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserRecentPlaceRepository {

    private val recentPlaceCollection: CollectionReference
        get() = firestore.collection("recent_place")

    override suspend fun saveRecentPlace(userId: String, areaIdentifier: PlaceIdentifier): Unit =
        withContext(ioDispatcher) {
            val firestoreRecentPlace = FirestorePlaceIdentifier(areaIdentifier)
            recentPlaceCollection.document(userId)
                .set(firestoreRecentPlace)
                .await()
        }

    override suspend fun getRecentPlaceIdentifier(userId: String): PlaceIdentifier? =
        withContext(ioDispatcher) {
            recentPlaceCollection.document(userId)
                .get()
                .await()
                .toObject(FirestorePlaceIdentifier::class.java)
                ?.placeIdentifier
        }
}

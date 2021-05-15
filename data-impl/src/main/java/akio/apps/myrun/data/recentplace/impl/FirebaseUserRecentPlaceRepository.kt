package akio.apps.myrun.data.recentplace.impl

import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.data.recentplace.entity.FirestorePlaceIdentifier
import akio.apps.myrun.data.recentplace.entity.PlaceIdentifier
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserRecentPlaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRecentPlaceRepository {

    private val recentPlaceCollection: CollectionReference
        get() = firestore.collection("recent_place")

    override suspend fun saveRecentPlace(userId: String, areaIdentifier: String) {
        val firestoreRecentPlace = FirestorePlaceIdentifier(areaIdentifier)
        recentPlaceCollection.document(userId)
            .set(firestoreRecentPlace)
            .await()
    }

    override suspend fun getRecentPlaceIdentifier(userId: String): PlaceIdentifier? {
        val identifier = recentPlaceCollection.document(userId)
            .get()
            .await()
            .toObject(FirestorePlaceIdentifier::class.java)
            ?.placeIdentifier
            ?: return null

        return PlaceIdentifier(identifier)
    }
}

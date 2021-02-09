package akio.apps.myrun.data.recentplace.impl

import akio.apps.myrun.data.recentplace.RecentPlaceRepository
import akio.apps.myrun.data.recentplace.entity.FirestoreRecentPlace
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseRecentPlaceRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : RecentPlaceRepository {

    private val recentPlaceCollection: CollectionReference
        get() = firestore.collection("recent_place")

    override suspend fun saveRecentPlace(userId: String, addressComponents: List<String>) {
        val areaIdentifier = StringBuilder()
        addressComponents.forEach { areaIdentifier.append("$it-") }
        areaIdentifier.deleteCharAt(areaIdentifier.length - 1) // remove the last hyphen
        val firestoreRecentPlace = FirestoreRecentPlace(areaIdentifier.toString())
        recentPlaceCollection.document(userId)
            .set(firestoreRecentPlace)
            .await()
    }

    override suspend fun getRecentPlaceIdentifier(userId: String): String? {
        return recentPlaceCollection.document(userId)
            .get()
            .await()
            .toObject(FirestoreRecentPlace::class.java)
            ?.areaIdentifier
    }
}

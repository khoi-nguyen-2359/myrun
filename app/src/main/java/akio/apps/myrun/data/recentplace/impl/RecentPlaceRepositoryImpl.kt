package akio.apps.myrun.data.recentplace.impl

import akio.apps.myrun.data.recentplace.RecentPlaceRepository
import akio.apps.myrun.data.recentplace.entity.FirestoreRecentPlace
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecentPlaceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RecentPlaceRepository {

    private val recentPlaceCollection: CollectionReference
    get() = firestore.collection("recentplace")

    override suspend fun saveRecentPlace(userId: String, addressComponents: List<String>) {
        val firestoreRecentPlace = FirestoreRecentPlace(addressComponents)
        recentPlaceCollection.document(userId)
            .set(firestoreRecentPlace)
            .await()
    }

    override fun getNearBy(userId: String) {
        TODO("Not yet implemented")
    }
}
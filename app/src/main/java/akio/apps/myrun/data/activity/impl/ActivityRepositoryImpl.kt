package akio.apps.myrun.data.activity.impl

import akio.apps._base.utils.FirebaseStorageUtils
import akio.apps.myrun.data.activity.*
import akio.apps.myrun.data.activity.entity.FirestoreActivity
import akio.apps.myrun.data.activity.entity.FirestoreActivityMapper
import akio.apps.myrun.data.activity.entity.FirestoreCyclingData
import akio.apps.myrun.data.activity.entity.FirestoreRunningData
import android.graphics.Bitmap
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val firestoreActivityMapper: FirestoreActivityMapper
) : ActivityRepository {

    private val activityCollection: CollectionReference
        get() = firestore.collection("activity")

    private val activityStotage: StorageReference
        get() = firebaseStorage.getReference("activity")

    override suspend fun getActivitiesByStartTime(userId: String, startAfterTime: Long, limit: Int): List<ActivityEntity> = withContext(Dispatchers.IO) {
        val query = activityCollection.whereEqualTo("userId", userId)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .startAfter(startAfterTime)
            .limit(limit.toLong())

        val snapshot = query.get().await()

        snapshot.documents.mapNotNull { doc ->
            val firestoreActivity = doc.toObject(FirestoreActivity::class.java)
                ?: return@mapNotNull null

            firestoreActivityMapper.map(firestoreActivity)
        }
    }

    override suspend fun saveActivity(activity: ActivityEntity, routeMapImage: Bitmap): Unit = withContext(Dispatchers.IO) {
        val uploadedUri = FirebaseStorageUtils.uploadBitmap(activityStotage, routeMapImage, THUMBNAIL_SCALED_SIZE)

        val docRef = activityCollection.document()
        val firestoreActivity = firestoreActivityMapper.mapRev(activity, docRef.id, uploadedUri)
        docRef.set(firestoreActivity).await()
    }

    companion object {
        const val THUMBNAIL_SCALED_SIZE = 1024 //px
    }
}
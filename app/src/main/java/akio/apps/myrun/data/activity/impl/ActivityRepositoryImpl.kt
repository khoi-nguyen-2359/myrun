package akio.apps.myrun.data.activity.impl

import akio.apps._base.utils.FirebaseStorageUtils
import akio.apps.myrun.data.activity.*
import akio.apps.myrun.data.activity.entity.FirestoreActivity
import akio.apps.myrun.data.activity.entity.FirestoreActivityMapper
import android.graphics.Bitmap
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val firestoreActivityMapper: FirestoreActivityMapper
) : ActivityRepository {

    private val activityCollection: CollectionReference
        get() = firestore.collection("activity")

    private fun getActivityImageStorage(userId: String): StorageReference
            = firebaseStorage.getReference("activity_image/$userId")

    override suspend fun getActivitiesByStartTime(userIds: List<String>, startAfterTime: Long, limit: Int): List<ActivityEntity> = withContext(Dispatchers.IO) {
        val query = activityCollection.whereIn("userId", userIds)
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
        val docRef = activityCollection.document()

        val uploadedUri = FirebaseStorageUtils.uploadBitmap(getActivityImageStorage(activity.userId), docRef.id, routeMapImage, THUMBNAIL_SCALED_SIZE)

        val firestoreActivity = firestoreActivityMapper.mapRev(activity, docRef.id, uploadedUri)
        docRef.set(firestoreActivity).await()
    }

    companion object {
        const val THUMBNAIL_SCALED_SIZE = 1024 //px
    }
}
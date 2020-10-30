package akio.apps.myrun.data.activity.impl

import akio.apps._base.utils.FirebaseStorageUtils
import akio.apps.myrun.data.activity.*
import akio.apps.myrun.data.activity.entity.FirestoreActivity
import akio.apps.myrun.data.activity.entity.FirestoreActivityMapper
import akio.apps.myrun.data.activity.entity.FirestoreDataPointArray
import akio.apps.myrun.data.fitness.SingleDataPoint
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

    override suspend fun saveActivity(
        activity: ActivityEntity,
        routeMapImage: Bitmap,
        speedDataPoints: List<SingleDataPoint<Float>>,
        stepCadenceDataPoints: List<SingleDataPoint<Int>>?
    ): Unit = withContext(Dispatchers.IO) {
        val docRef = activityCollection.document()

        val uploadedUri = FirebaseStorageUtils.uploadBitmap(getActivityImageStorage(activity.userId), docRef.id, routeMapImage, THUMBNAIL_SCALED_SIZE)

        val firestoreActivity = firestoreActivityMapper.mapRev(activity, docRef.id, uploadedUri)

        val speedDocRef = docRef.collection(PATH_DATA_POINTS).document(PATH_DATA_POINTS_SPEED)
        val stepCadenceDocRef = docRef.collection(PATH_DATA_POINTS).document(PATH_DATA_POINTS_STEP_CADENCE)
        firestore.runBatch { batch ->
            batch.set(docRef, firestoreActivity)
            batch.set(speedDocRef, FirestoreDataPointArray(speedDataPoints.map { dp -> listOf(dp.timestamp.toDouble(), dp.value.toDouble()) }))

            if (stepCadenceDataPoints != null) {
                batch.set(stepCadenceDocRef, FirestoreDataPointArray(stepCadenceDataPoints.map { dp -> listOf(dp.timestamp.toDouble(), dp.value.toDouble()) }))
            }
        }.await()
    }

    companion object {
        const val PATH_DATA_POINTS = "dataPoints"
        const val PATH_DATA_POINTS_SPEED = "speed"
        const val PATH_DATA_POINTS_STEP_CADENCE = "stepCadence"
        const val PATH_DATA_POINTS_PEDALING_CADENCE = "pedalingCadence"
        const val THUMBNAIL_SCALED_SIZE = 1024 //px
    }
}
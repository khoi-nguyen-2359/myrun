package akio.apps.myrun.data.activity.impl

import akio.apps._base.utils.FirebaseStorageUtils
import akio.apps.myrun.data.activity.RunningActivityEntity
import akio.apps.myrun.data.activity.ActivityDataEntity
import akio.apps.myrun.data.activity.ActivityEntity
import akio.apps.myrun.data.activity.ActivityRepository
import android.graphics.Bitmap
import android.net.Uri
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
    private val firebaseStorage: FirebaseStorage
) : ActivityRepository {

    private val activityCollection: CollectionReference
        get() = firestore.collection("activity")

    private val activityStotage: StorageReference
        get() = firebaseStorage.getReference("activity")

    override suspend fun getActivitiesByStartTime(startAfterTime: Long, limit: Int): List<ActivityEntity> = withContext(Dispatchers.IO) {
        val query = activityCollection
            .orderBy("startTime", Query.Direction.DESCENDING)
            .startAfter(startAfterTime)
            .limit(limit.toLong())

        val snapshot = query.get().await()

        snapshot.documents.mapNotNull { doc ->
            val firestoreActivity = doc.toObject(FirestoreActivity::class.java)
                ?: return@mapNotNull null

            val activityData = firestoreActivity.toActivityDataEntity(doc.id)
            if (firestoreActivity.runData != null) {
                RunningActivityEntity(
                    activityData = activityData,
                    routePhoto = firestoreActivity.runData.routePhoto,
                    averagePace = firestoreActivity.runData.averagePace,
                    distance = firestoreActivity.runData.distance,
                    encodedPolyline = firestoreActivity.runData.encodedPolyline
                )
            } else throw IllegalArgumentException("Unknown activity type")
        }
    }

    override suspend fun saveActivity(activity: ActivityEntity, routeMapImage: Bitmap): Unit = withContext(Dispatchers.IO) {
        val uploadedUri = FirebaseStorageUtils.uploadBitmap(activityStotage, routeMapImage, THUMBNAIL_SCALED_SIZE)

        val runData: FirestoreRunData? = (activity as? RunningActivityEntity)
            ?.toFirestoreRunData(uploadedUri)

        val firestoreActivity = FirestoreActivity(
            activityType = activity.activityType,
            startTime = activity.startTime,
            endTime = activity.endTime,
            duration = activity.duration,
            runData = runData
        )
        activityCollection.add(firestoreActivity).await()
    }

    private fun RunningActivityEntity.toFirestoreRunData(routePhotoUri: Uri? = null) = FirestoreRunData(
        routePhotoUri?.toString() ?: routePhoto, averagePace, distance, encodedPolyline
    )

    private fun FirestoreActivity.toActivityDataEntity(activityId: String) = ActivityDataEntity(
        activityId, activityType, startTime, endTime, duration
    )

    companion object {
        const val THUMBNAIL_SCALED_SIZE = 1024 //px
    }
}
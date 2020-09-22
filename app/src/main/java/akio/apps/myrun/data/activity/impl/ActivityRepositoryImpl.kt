package akio.apps.myrun.data.activity.impl

import akio.apps._base.utils.FirebaseStorageUtils
import akio.apps.myrun.data.activity.*
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
    private val firebaseStorage: FirebaseStorage
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

            val activityData = firestoreActivity.toActivityDataEntity(doc.id)
            firestoreActivity.run {
                runningData?.run {
                    RunningActivityEntity(activityData = activityData, pace = pace)
                } ?: cyclingData?.run {
                    CyclingActivityEntity(activityData = activityData, speed = speed)
                }
            }
        }
    }

    override suspend fun saveActivity(activity: ActivityEntity, routeMapImage: Bitmap): Unit = withContext(Dispatchers.IO) {
        val uploadedUri = FirebaseStorageUtils.uploadBitmap(activityStotage, routeMapImage, THUMBNAIL_SCALED_SIZE)

        val runData: FirestoreRunningData? = (activity as? RunningActivityEntity)
            ?.toFirestoreRunData()

        val cyclingData: FirestoreCyclingData? = (activity as? CyclingActivityEntity)
            ?.toFirestoreCyclingData()

        val firestoreActivity = FirestoreActivity(
            userId = activity.userId,
            activityType = activity.activityType,
            name = activity.name,
            routeImage = uploadedUri.toString(),
            startTime = activity.startTime,
            endTime = activity.endTime,
            duration = activity.duration,
            distance = activity.distance,
            encodedPolyline = activity.encodedPolyline,
            runningData = runData,
            cyclingData = cyclingData
        )
        activityCollection.add(firestoreActivity).await()
    }

    private fun RunningActivityEntity.toFirestoreRunData() = FirestoreRunningData(pace)

    private fun FirestoreActivity.toActivityDataEntity(activityId: String) = ActivityDataEntity(
        activityId, userId, activityType, name, routeImage, startTime, endTime, duration, distance, encodedPolyline
    )

    private fun CyclingActivityEntity.toFirestoreCyclingData() = FirestoreCyclingData(speed)

    companion object {
        const val THUMBNAIL_SCALED_SIZE = 1024 //px
    }
}
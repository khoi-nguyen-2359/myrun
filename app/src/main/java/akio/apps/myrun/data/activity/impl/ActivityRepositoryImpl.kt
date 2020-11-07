package akio.apps.myrun.data.activity.impl

import akio.apps._base.utils.FirebaseStorageUtils
import akio.apps.myrun.data.activity.*
import akio.apps.myrun.data.activity.entity.FirestoreActivity
import akio.apps.myrun.data.activity.entity.FirestoreActivityMapper
import akio.apps.myrun.data.activity.entity.FirestoreDataPointArray
import akio.apps.myrun.data.fitness.SingleDataPoint
import akio.apps.myrun.data.location.LocationEntity
import android.graphics.Bitmap
import android.location.Location
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

    private val userActivitiesCollectionGroup: Query
        get() = firestore.collectionGroup(PATH_USER_ACTIVITIES_COLLECTION_GROUP)

    private fun getUserActivitiesCollection(userId: String): CollectionReference {
        return firestore.collection("$PATH_USERS/$userId/$PATH_USER_ACTIVITIES")
    }

    private fun getActivityImageStorage(userId: String): StorageReference
            = firebaseStorage.getReference("activity_image/$userId")

    override suspend fun getActivitiesByStartTime(userIds: List<String>, startAfterTime: Long, limit: Int): List<ActivityEntity> = withContext(Dispatchers.IO) {
        val query = userActivitiesCollectionGroup.whereIn("userId", userIds)
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
        stepCadenceDataPoints: List<SingleDataPoint<Int>>?,
        locationDataPoints: List<SingleDataPoint<LocationEntity>>
    ): Unit = withContext(Dispatchers.IO) {
        val docRef = getUserActivitiesCollection(activity.userId).document()

        val userActivityImageStorage = getActivityImageStorage(activity.userId)
        val uploadedUri = FirebaseStorageUtils.uploadBitmap(userActivityImageStorage, docRef.id, routeMapImage, THUMBNAIL_SCALED_SIZE)

        val firestoreActivity = firestoreActivityMapper.mapRev(activity, docRef.id, uploadedUri)

        val dataPointCollections = docRef.collection(PATH_DATA_POINTS)
        val speedDocRef = dataPointCollections.document(PATH_DATA_POINTS_SPEED)
        val stepCadenceDocRef = dataPointCollections.document(PATH_DATA_POINTS_STEP_CADENCE)
        val locationDocRef = dataPointCollections.document(PATH_DATA_POINTS_LOCATIONS)
        firestore.runBatch { batch ->
            batch.set(docRef, firestoreActivity)
            batch.set(speedDocRef, FirestoreDataPointArray(speedDataPoints.map { dp -> listOf(dp.timestamp.toDouble(), dp.value.toDouble()) }))
            batch.set(locationDocRef, FirestoreDataPointArray(locationDataPoints.map { listOf(it.timestamp.toDouble(), it.value.latitude, it.value.longitude, it.value.altitude) }))

            if (stepCadenceDataPoints != null) {
                batch.set(stepCadenceDocRef, FirestoreDataPointArray(stepCadenceDataPoints.map { dp -> listOf(dp.timestamp.toDouble(), dp.value.toDouble()) }))
            }
        }.await()
    }

    companion object {
        const val PATH_USER_ACTIVITIES_COLLECTION_GROUP = "userActivities"
        const val PATH_USERS = "users"
        const val PATH_USER_ACTIVITIES = "userActivities"
        const val PATH_DATA_POINTS = "dataPoints"
        const val PATH_DATA_POINTS_SPEED = "speed"
        const val PATH_DATA_POINTS_STEP_CADENCE = "stepCadence"
        const val PATH_DATA_POINTS_LOCATIONS = "location"
        const val THUMBNAIL_SCALED_SIZE = 1024 //px
    }
}
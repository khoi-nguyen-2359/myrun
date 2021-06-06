package akio.apps.myrun.data.activity.impl

import akio.apps.myrun._di.NamedIoDispatcher
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.activity.entity.FirestoreActivity
import akio.apps.myrun.data.activity.entity.FirestoreActivityMapper
import akio.apps.myrun.data.activity.entity.FirestoreDataPointDeserializer
import akio.apps.myrun.data.activity.entity.FirestoreDataPointList
import akio.apps.myrun.data.activity.entity.FirestoreDataPointSerializer
import akio.apps.myrun.data.activity.entity.FirestoreFloatDataPointParser
import akio.apps.myrun.data.activity.entity.FirestoreIntegerDataPointParser
import akio.apps.myrun.data.activity.entity.FirestoreLocationDataPointParser
import akio.apps.myrun.data.activity.model.ActivityModel
import akio.apps.myrun.data.fitness.SingleDataPoint
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.utils.FirebaseStorageUtils
import android.graphics.Bitmap
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import timber.log.Timber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseActivityRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val firestoreActivityMapper: FirestoreActivityMapper,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ActivityRepository {

    private val userActivityCollectionGroup: Query
        get() = firestore.collectionGroup(PATH_USER_ACTIVITIES_COLLECTION_GROUP)

    private fun getUserActivityCollection(userId: String): CollectionReference {
        return firestore.collection("$PATH_USERS/$userId/$PATH_USER_ACTIVITIES")
    }

    private fun getActivityImageStorage(userId: String): StorageReference =
        firebaseStorage.getReference("activity_image/$userId")

    override suspend fun getActivitiesByStartTime(
        userIds: List<String>,
        startAfterTime: Long,
        limit: Int
    ): List<ActivityModel> = withContext(ioDispatcher) {
        val query = userActivityCollectionGroup.whereIn("athleteInfo.userId", userIds)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .startAfter(startAfterTime)
            .limit(limit.toLong())

        val snapshot = query.get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            val firestoreActivity = doc.toObject(FirestoreActivity::class.java)
                ?: return@mapNotNull null

            firestoreActivityMapper.map(firestoreActivity)
        }
    }

    override suspend fun saveActivity(
        activity: ActivityModel,
        routeMapImage: Bitmap,
        speedDataPoints: List<SingleDataPoint<Float>>,
        stepCadenceDataPoints: List<SingleDataPoint<Int>>?,
        locationDataPoints: List<SingleDataPoint<LocationEntity>>
    ): String = withContext(ioDispatcher) {
        Timber.d("=== SAVING ACTIVITY ===")
        val docRef = getUserActivityCollection(activity.athleteInfo.userId).document()
        Timber.d("created activity document id=${docRef.id}")

        Timber.d("uploading activity route image ...")
        val userActivityImageStorage = getActivityImageStorage(activity.athleteInfo.userId)
        val uploadedUri = FirebaseStorageUtils.uploadBitmap(
            userActivityImageStorage,
            docRef.id,
            routeMapImage,
            THUMBNAIL_SCALED_SIZE
        )
        Timber.d("[DONE] uploading activity route image url=$uploadedUri")

        val firestoreActivity = firestoreActivityMapper.mapRev(activity, docRef.id, uploadedUri)

        val dataPointCollections = docRef.collection(PATH_DATA_POINTS)
        val speedDocRef = dataPointCollections.document(PATH_DATA_POINTS_SPEED)
        val stepCadenceDocRef = dataPointCollections.document(PATH_DATA_POINTS_STEP_CADENCE)
        val locationDocRef = dataPointCollections.document(PATH_DATA_POINTS_LOCATIONS)
        Timber.d("writing activity and data points to firebase ...")
        firestore.runBatch { batch ->
            batch.set(docRef, firestoreActivity)
            batch.set(
                speedDocRef,
                FirestoreDataPointSerializer(FirestoreFloatDataPointParser()).serialize(
                    speedDataPoints
                )
            )
            batch.set(
                locationDocRef,
                FirestoreDataPointSerializer(FirestoreLocationDataPointParser()).serialize(
                    locationDataPoints
                )
            )

            if (stepCadenceDataPoints != null) {
                batch.set(
                    stepCadenceDocRef,
                    FirestoreDataPointSerializer(FirestoreIntegerDataPointParser()).serialize(
                        stepCadenceDataPoints
                    )
                )
            }
        }
            .await()
        Timber.d("=== [DONE] SAVING ACTIVITY ===")

        docRef.id
    }

    override suspend fun getActivityLocationDataPoints(
        activityId: String
    ): List<SingleDataPoint<LocationEntity>> = withContext(ioDispatcher) {
        val firebaseActivity =
            userActivityCollectionGroup.whereEqualTo(FIELD_ACTIVITY_ID, activityId).get().await()
        val firestoreLocationDataPoints = firebaseActivity.documents.getOrNull(0)
            ?.reference
            ?.collection(PATH_DATA_POINTS)
            ?.document(PATH_DATA_POINTS_LOCATIONS)
            ?.get()
            ?.await()
            ?.toObject(FirestoreDataPointList::class.java)
            ?: return@withContext emptyList()

        val firestoreLocationDataPointDeserializer =
            FirestoreDataPointDeserializer(FirestoreLocationDataPointParser())

        return@withContext firestoreLocationDataPointDeserializer.deserialize(
            firestoreLocationDataPoints
        )
    }

    override suspend fun getActivity(
        activityId: String
    ): ActivityModel? = withContext(ioDispatcher) {
        val snapshot = userActivityCollectionGroup.whereEqualTo("id", activityId).get().await()
        snapshot.documents
            .getOrNull(0)
            ?.toObject(FirestoreActivity::class.java)
            ?.let(firestoreActivityMapper::map)
    }

    companion object {
        const val PATH_USER_ACTIVITIES_COLLECTION_GROUP = "userActivities"
        const val PATH_USERS = "users"
        const val PATH_USER_ACTIVITIES = "userActivities"
        const val PATH_DATA_POINTS = "dataPoints"
        const val PATH_DATA_POINTS_SPEED = "speed"
        const val PATH_DATA_POINTS_STEP_CADENCE = "stepCadence"
        const val PATH_DATA_POINTS_LOCATIONS = "location"

        const val FIELD_ACTIVITY_ID = "id"

        const val THUMBNAIL_SCALED_SIZE = 1024 // px
    }
}

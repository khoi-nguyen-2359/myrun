package akio.apps.myrun.data.activity.impl

import akio.apps.common.wiring.NamedIoDispatcher
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.data.activity.impl.model.FirestoreActivity
import akio.apps.myrun.data.activity.impl.model.FirestoreActivityMapper
import akio.apps.myrun.data.activity.impl.model.FirestoreDataPointList
import akio.apps.myrun.data.activity.impl.model.FirestoreDataPointSerializer
import akio.apps.myrun.data.activity.impl.model.FirestoreFloatDataPointParser
import akio.apps.myrun.data.activity.impl.model.FirestoreIntegerDataPointParser
import akio.apps.myrun.data.activity.impl.model.FirestoreLocationDataPointParser
import akio.apps.myrun.data.base.FirebaseStorageUtils
import akio.apps.myrun.data.fitness.DataPoint
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class FirebaseActivityRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val firestoreActivityMapper: FirestoreActivityMapper,
    @NamedIoDispatcher private val ioDispatcher: CoroutineDispatcher,
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
        limit: Int,
    ): List<ActivityModel> = withContext(ioDispatcher) {
        val query = userActivityCollectionGroup.whereIn("athleteInfo.userId", userIds)
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
        activity: ActivityModel,
        routeBitmapFile: File,
        speedDataPoints: List<DataPoint<Float>>,
        stepCadenceDataPoints: List<DataPoint<Int>>?,
        locationDataPoints: List<ActivityLocation>,
    ): String = withContext(ioDispatcher) {
        Timber.d("=== SAVING ACTIVITY ===")
        val docRef = if (activity.id.isNotEmpty()) {
            getUserActivityCollection(activity.athleteInfo.userId).document(activity.id)
        } else {
            getUserActivityCollection(activity.athleteInfo.userId).document()
        }
        Timber.d("created activity document id=${docRef.id}")

        Timber.d("uploading activity route image ...")
        val userActivityImageStorage = getActivityImageStorage(activity.athleteInfo.userId)
        val uploadedUri = FirebaseStorageUtils.uploadLocalBitmap(
            userActivityImageStorage,
            docRef.id,
            routeBitmapFile.absolutePath,
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
                FirestoreDataPointList(
                    data = FirestoreLocationDataPointParser().flatten(locationDataPoints)
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
        activityId: String,
    ): List<ActivityLocation> = withContext(ioDispatcher) {
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

        FirestoreLocationDataPointParser().build(activityId, firestoreLocationDataPoints.data)
    }

    override suspend fun getActivity(
        activityId: String,
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

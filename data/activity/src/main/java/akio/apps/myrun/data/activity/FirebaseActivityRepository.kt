package akio.apps.myrun.data.activity

import akio.apps.myrun.data.Resource
import akio.apps.myrun.data.activity.model.FirestoreActivity
import akio.apps.myrun.data.activity.model.FirestoreActivityMapper
import akio.apps.myrun.data.activity.model.FirestoreActivityType
import akio.apps.myrun.data.activity.model.FirestoreDataPointList
import akio.apps.myrun.data.activity.model.FirestoreDataPointSerializer
import akio.apps.myrun.data.activity.model.FirestoreFloatDataPointParser
import akio.apps.myrun.data.activity.model.FirestoreIntegerDataPointParser
import akio.apps.myrun.data.activity.model.FirestoreLocationDataPointParser
import akio.apps.myrun.data.base.FirebaseStorageUtils
import akio.apps.myrun.data.wiring.NamedIoDispatcher
import akio.apps.myrun.domain.activity.api.model.ActivityLocation
import akio.apps.myrun.domain.activity.api.model.ActivityModel
import akio.apps.myrun.domain.activity.api.model.ActivityType
import akio.apps.myrun.domain.activity.api.model.DataPoint
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException
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
) : akio.apps.myrun.domain.activity.api.ActivityRepository {

    private val userActivityCollectionGroup: Query
        get() = firestore.collectionGroup(PATH_USER_ACTIVITIES_COLLECTION_GROUP)

    private fun getUserActivityCollection(userId: String): CollectionReference {
        return firestore.collection("$PATH_USERS/$userId/$PATH_USER_ACTIVITIES")
    }

    private fun getActivityImageStorage(userId: String): StorageReference =
        firebaseStorage.getReference("activity_image/$userId")

    override suspend fun getActivitiesByStartTime(
        fixUserId: String,
        userIds: List<String>,
        startAfterTime: Long,
        limit: Int,
    ): List<ActivityModel> = withContext(ioDispatcher) {
        val query = userActivityCollectionGroup.whereIn("athleteInfo.userId", userIds)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .startAfter(startAfterTime)
            .limit(limit.toLong())

        val snapshot = query.get().await()

        snapshot.documents.mapNotNull { it.toObject(FirestoreActivity::class.java) }
            .map(firestoreActivityMapper::map)
    }

    override suspend fun getActivitiesInTimeRange(
        userId: String,
        startTime: Long,
        endTime: Long,
    ): List<ActivityModel> = withContext(ioDispatcher) {
        val query = userActivityCollectionGroup.whereEqualTo("athleteInfo.userId", userId)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .startAt(endTime)
            .endAt(startTime)

        val snapshot = query.get().await()
        snapshot.documents.mapNotNull { it.toObject(FirestoreActivity::class.java) }
            .map(firestoreActivityMapper::map)
    }

    override suspend fun saveActivity(
        activity: ActivityModel,
        routeBitmapFile: File,
        speedDataPoints: List<DataPoint<Float>>,
        locationDataPoints: List<ActivityLocation>,
        stepCadenceDataPoints: List<DataPoint<Int>>?,
    ): String = withContext(ioDispatcher) {
        Timber.d("=== SAVING ACTIVITY ===")
        val activityDocRef = if (activity.id.isNotEmpty()) {
            getUserActivityCollection(activity.athleteInfo.userId).document(activity.id)
        } else {
            getUserActivityCollection(activity.athleteInfo.userId).document()
        }
        Timber.d("created activity document id=${activityDocRef.id}")

        Timber.d("uploading activity route image ...")
        val userActivityImageStorage = getActivityImageStorage(activity.athleteInfo.userId)
        val uploadedUri = FirebaseStorageUtils.uploadLocalBitmap(
            userActivityImageStorage,
            activityDocRef.id,
            routeBitmapFile.absolutePath,
            THUMBNAIL_SCALED_SIZE
        )
        Timber.d("[DONE] uploading activity route image url=$uploadedUri")

        val firestoreActivity =
            firestoreActivityMapper.mapRev(activity, activityDocRef.id, uploadedUri)

        Timber.d("writing activity and data points to firebase ...")
        firestore.runBatch { batch ->
            batch.set(activityDocRef, firestoreActivity)
            batchWriteActivityDataPoints(
                batch,
                activityDocRef,
                speedDataPoints,
                locationDataPoints,
                stepCadenceDataPoints,
            )
        }.await()
        Timber.d("=== [DONE] SAVING ACTIVITY ===")

        activityDocRef.id
    }

    private fun batchWriteActivityDataPoints(
        batch: WriteBatch,
        docRef: DocumentReference,
        speedDataPoints: List<DataPoint<Float>>,
        locationDataPoints: List<ActivityLocation>,
        stepCadenceDataPoints: List<DataPoint<Int>>?,
    ) {
        val dataPointCollections = docRef.collection(PATH_DATA_POINTS)
        val speedDocRef = dataPointCollections.document(PATH_DATA_POINTS_SPEED)
        val stepCadenceDocRef = dataPointCollections.document(PATH_DATA_POINTS_STEP_CADENCE)
        val locationDocRef = dataPointCollections.document(PATH_DATA_POINTS_LOCATIONS)
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

        FirestoreLocationDataPointParser().build(firestoreLocationDataPoints.data)
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

    override suspend fun getActivityResource(activityId: String): Resource<ActivityModel?> = try {
        val activityData = getActivity(activityId)
        Resource.Success(activityData)
    } catch (ioEx: IOException) {
        Resource.Error(ioEx)
    }

    private fun ActivityType.toFsActivityType() = when (this) {
        ActivityType.Running -> FirestoreActivityType.Running
        ActivityType.Cycling -> FirestoreActivityType.Cycling
        ActivityType.Unknown -> FirestoreActivityType.Unknown
    }

    companion object {
        private const val PATH_USER_ACTIVITIES_COLLECTION_GROUP = "userActivities"
        private const val PATH_USERS = "users"
        private const val PATH_USER_ACTIVITIES = "userActivities"
        private const val PATH_DATA_POINTS = "dataPoints"
        private const val PATH_DATA_POINTS_SPEED = "speed"
        private const val PATH_DATA_POINTS_STEP_CADENCE = "stepCadence"
        private const val PATH_DATA_POINTS_LOCATIONS = "location"

        private const val FIELD_ACTIVITY_ID = "id"

        private const val THUMBNAIL_SCALED_SIZE = 1024 // px

        /**
         * Data version of activity stored on firestore.
         */
        internal const val ACTIVITY_DATA_VERSION = 2
    }
}

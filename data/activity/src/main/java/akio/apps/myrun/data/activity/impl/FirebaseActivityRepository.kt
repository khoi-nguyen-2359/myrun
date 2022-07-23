package akio.apps.myrun.data.activity.impl

import akio.apps.myrun.base.firebase.FirebaseStorageUtils
import akio.apps.myrun.data.activity.api.ActivityRepository
import akio.apps.myrun.data.activity.api.locationparser.LocationDataPointParserFactory
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.activity.api.model.DataPoint
import akio.apps.myrun.data.activity.di.ActivityDataScope
import akio.apps.myrun.data.activity.impl.model.FirestoreActivity
import akio.apps.myrun.data.activity.impl.model.FirestoreActivityMapper
import akio.apps.myrun.data.activity.impl.model.FirestoreDataPointList
import akio.apps.myrun.data.activity.impl.model.FirestoreDataPointSerializer
import akio.apps.myrun.data.activity.impl.model.FirestoreFloatDataPointParser
import akio.apps.myrun.data.activity.impl.model.FirestoreIntegerDataPointParser
import akio.apps.myrun.data.common.Resource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.anvil.annotations.ContributesBinding
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@Singleton
@ContributesBinding(ActivityDataScope::class)
class FirebaseActivityRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val firestoreActivityMapper: FirestoreActivityMapper,
) : ActivityRepository {

    private val userActivityCollectionGroup: Query
        get() = firestore.collectionGroup(PATH_USER_ACTIVITIES_COLLECTION_GROUP)

    private fun getUserActivityCollection(userId: String): CollectionReference {
        return firestore.collection("$PATH_USERS/$userId/$PATH_USER_ACTIVITIES")
    }

    private fun getActivityImageStorage(userId: String): StorageReference =
        firebaseStorage.getReference("$PATH_ACTIVITY_IMAGE/$userId")

    override suspend fun getActivitiesByStartTime(
        fixUserId: String,
        userIds: List<String>,
        startAfterTime: Long,
        limit: Int,
    ): List<BaseActivityModel> {
        val query = userActivityCollectionGroup.whereIn(FIELD_ATHLETE_USERID, userIds)
            .orderBy(FIELD_ACTIVITY_START_TIME, Query.Direction.DESCENDING)
            .startAfter(startAfterTime)
            .limit(limit.toLong())

        val snapshot = query.get().await()

        return snapshot.documents.mapNotNull { it.toObject(FirestoreActivity::class.java) }
            .map(firestoreActivityMapper::map)
    }

    override suspend fun getActivitiesInTimeRange(
        userId: String,
        startTime: Long,
        endTime: Long,
    ): List<BaseActivityModel> {
        val query = getUserActivityCollection(userId)
            .orderBy(FIELD_ACTIVITY_START_TIME, Query.Direction.DESCENDING)
            .startAt(endTime)
            .endAt(startTime)

        val snapshot = query.get().await()
        return snapshot.documents.mapNotNull { it.toObject(FirestoreActivity::class.java) }
            .map(firestoreActivityMapper::map)
    }

    override suspend fun saveActivity(
        activity: BaseActivityModel,
        routeBitmapFile: File,
        speedDataPoints: List<DataPoint<Float>>,
        locationDataPoints: List<ActivityLocation>,
        stepCadenceDataPoints: List<DataPoint<Int>>?,
    ): String {
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
                stepCadenceDataPoints
            )
        }.await()
        Timber.d("=== [DONE] SAVING ACTIVITY ===")

        return activityDocRef.id
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
            FirestoreDataPointSerializer(FirestoreFloatDataPointParser()).serialize(speedDataPoints)
        )
        val parser = LocationDataPointParserFactory.getWriteParser()
        batch.set(locationDocRef, FirestoreDataPointList(parser.flatten(locationDataPoints)))

        if (stepCadenceDataPoints != null) {
            batch.set(
                stepCadenceDocRef,
                FirestoreDataPointSerializer(FirestoreIntegerDataPointParser())
                    .serialize(stepCadenceDataPoints)
            )
        }
    }

    override suspend fun getActivityLocationDataPoints(
        activityId: String,
    ): List<ActivityLocation> {
        val firebaseActivity =
            userActivityCollectionGroup.whereEqualTo(FIELD_ID, activityId).get().await()
        val firestoreLocationDataPoints = firebaseActivity.documents.getOrNull(0)
            ?.reference
            ?.collection(PATH_DATA_POINTS)
            ?.document(PATH_DATA_POINTS_LOCATIONS)
            ?.get()
            ?.await()
            ?.toObject(FirestoreDataPointList::class.java)
            ?: return emptyList()

        return LocationDataPointParserFactory.getParser(firestoreLocationDataPoints.version)
            .build(firestoreLocationDataPoints.data)
    }

    override suspend fun getActivity(
        activityId: String,
    ): BaseActivityModel? {
        val snapshot = userActivityCollectionGroup.whereEqualTo(FIELD_ID, activityId).get().await()
        return snapshot.documents
            .getOrNull(0)
            ?.toObject(FirestoreActivity::class.java)
            ?.let(firestoreActivityMapper::map)
    }

    override suspend fun getActivityResource(
        activityId: String,
    ): Resource<BaseActivityModel?> = try {
        val activityData = getActivity(activityId)
        Resource.Success(activityData)
    } catch (ioEx: IOException) {
        Resource.Error(ioEx)
    }

    companion object {
        private const val PATH_USER_ACTIVITIES_COLLECTION_GROUP = "userActivities"
        private const val PATH_USERS = "users"
        private const val PATH_USER_ACTIVITIES = "userActivities"
        private const val PATH_DATA_POINTS = "dataPoints"
        private const val PATH_DATA_POINTS_SPEED = "speed"
        private const val PATH_DATA_POINTS_STEP_CADENCE = "stepCadence"
        private const val PATH_ACTIVITY_IMAGE = "activity_image"
        private const val PATH_DATA_POINTS_LOCATIONS = "location"

        private const val FIELD_ID = "id"
        private const val FIELD_ACTIVITY_START_TIME = "startTime"
        private const val FIELD_ATHLETE_USERID = "athleteInfo.userId"

        private const val THUMBNAIL_SCALED_SIZE = 1024 // px
    }
}

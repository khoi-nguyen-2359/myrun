package akio.apps.myrun.data.workout.impl

import akio.apps._base.utils.FirebaseStorageUtils
import akio.apps.myrun.data.workout.WorkoutRepository
import akio.apps.myrun.data.workout.model.*
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
import timber.log.Timber
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage
) : WorkoutRepository {

    private val workoutCollection: CollectionReference
        get() = firestore.collection("_workout_")

    private val workoutStorage: StorageReference
        get() = firebaseStorage.getReference("_workout_")

    override suspend fun getWorkoutsByStartTime(startAfterTime: Long, limit: Int): List<WorkoutEntity> = withContext(Dispatchers.IO) {
        Timber.d("getWorkoutsByStartTime")
        val query = workoutCollection
            .orderBy("startTime", Query.Direction.DESCENDING)
            .startAfter(startAfterTime)
            .limit(limit.toLong())

        val snapshot = query.get().await()

        snapshot.documents.mapNotNull { doc ->
            val firestoreWorkout = doc.toObject(FirestoreWorkout::class.java)
                ?: return@mapNotNull null

            val workoutData = firestoreWorkout.toWorkoutDataEntity(doc.id)
            if (firestoreWorkout.runData != null) {
                RunningWorkoutEntity(
                    workoutData = workoutData,
                    routePhoto = firestoreWorkout.runData.routePhoto,
                    averagePace = firestoreWorkout.runData.averagePace,
                    distance = firestoreWorkout.runData.distance,
                    encodedPolyline = firestoreWorkout.runData.encodedPolyline
                )
            } else throw IllegalArgumentException("[Firestore Workout] Unknown activity type")
        }
    }

    override suspend fun saveWorkout(workout: WorkoutEntity, routeMapImage: Bitmap): Unit = withContext(Dispatchers.IO) {
        val uploadedUri = FirebaseStorageUtils.uploadBitmap(workoutStorage, routeMapImage, THUMBNAIL_SCALED_SIZE)

        val runData: FirestoreRunData? = (workout as? RunningWorkoutEntity)
            ?.toFirestoreRunData(uploadedUri)

        val firestoreWorkout = FirestoreWorkout(
            activityType = workout.activityType,
            startTime = workout.startTime,
            endTime = workout.endTime,
            duration = workout.duration,
            runData = runData
        )
        workoutCollection.add(firestoreWorkout).await()
    }

    private fun RunningWorkoutEntity.toFirestoreRunData(routePhotoUri: Uri? = null) = FirestoreRunData(
        routePhotoUri?.toString() ?: routePhoto, averagePace, distance, encodedPolyline
    )

    private fun FirestoreWorkout.toWorkoutDataEntity(workoutId: String) = WorkoutDataEntity(
        workoutId, activityType, startTime, endTime, duration
    )

    companion object {
        const val THUMBNAIL_SCALED_SIZE = 1024 //px
    }
}
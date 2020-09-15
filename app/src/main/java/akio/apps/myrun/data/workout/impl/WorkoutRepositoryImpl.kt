package akio.apps.myrun.data.workout.impl

import akio.apps.myrun.data.workout.WorkoutRepository
import akio.apps.myrun.data.workout.dto.FirestoreRunData
import akio.apps.myrun.data.workout.dto.FirestoreWorkout
import akio.apps.myrun.data.workout.dto.RunDataEntity
import akio.apps.myrun.data.workout.dto.WorkoutEntity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WorkoutRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : WorkoutRepository {

    private val workoutCollection: CollectionReference
        get() = firestore.collection("workout")

    override suspend fun getWorkoutsByStartTime(userId: String, startAfterTime: Long, limit: Int): List<WorkoutEntity> {
        val query = workoutCollection.whereEqualTo("userId", userId)
            .orderBy("startTime", Query.Direction.DESCENDING)
            .startAfter(startAfterTime)
            .limit(limit.toLong())

        val snapshot = query.get().await()

        return snapshot.documents.mapNotNull {
            it.toObject(FirestoreWorkout::class.java)
        }
            .map { it.toWorkoutEntity() }
    }

    private fun FirestoreRunData.toRunDataEntity() = RunDataEntity(
        routePhoto, avgPace, distance, duration, locations, speeds, steps, cadences
    )

    private fun FirestoreWorkout.toWorkoutEntity() = WorkoutEntity(
        userId, activityType, name, startTime, endTime, heartRates, calories, runData?.toRunDataEntity()
    )
}
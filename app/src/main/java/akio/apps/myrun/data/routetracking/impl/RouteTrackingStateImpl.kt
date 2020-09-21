package akio.apps.myrun.data.routetracking.impl

import akio.apps.myrun.data.activity.ActivityType
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RouteTrackingStateImpl @Inject constructor(
    appContext: Context
) : RouteTrackingState {

    private val prefDataStore: DataStore<Preferences> = appContext.createDataStore("route_tracking_state")

    override suspend fun getTrackingStatus(): @RouteTrackingStatus Int = getTrackingStatusFlow()
        .first()

    override fun getTrackingStatusFlow(): Flow<@RouteTrackingStatus Int> = prefDataStore.data
        .map { data -> data[KEY_TRACKING_STATUS] ?: RouteTrackingStatus.STOPPED }
        .flowOn(Dispatchers.IO)

    override suspend fun setTrackingStatus(@RouteTrackingStatus status: Int): Unit = withContext(Dispatchers.IO) {
        prefDataStore.edit { state -> state[KEY_TRACKING_STATUS] = status }
    }

    override suspend fun setRouteDistance(distance: Double): Unit = withContext(Dispatchers.IO) {
        prefDataStore.edit { state -> state[KEY_ROUTE_DISTANCE] = distance.toFloat() }
    }

    override suspend fun setTrackingStartTime(startTime: Long): Unit = withContext(Dispatchers.IO) {
        prefDataStore.edit { state ->
            state[KEY_TRACKING_START_TIME] = startTime
        }
    }

    override suspend fun getTrackingStartTime(): Long = prefDataStore.data
        .map { data -> data[KEY_TRACKING_START_TIME] ?: 0L }
        .flowOn(Dispatchers.IO)
        .first()

    override suspend fun getRouteDistance(): Double = prefDataStore.data
        .map { state -> state[KEY_ROUTE_DISTANCE] ?: 0f }
        .flowOn(Dispatchers.IO)
        .first()
        .toDouble()

    override suspend fun setTrackingDuration(totalSec: Long): Unit = withContext(Dispatchers.IO) {
        prefDataStore.edit { data ->
            data[KEY_TRACKING_DURATION] = totalSec
        }
    }

    override suspend fun getTrackingDuration(): Long = prefDataStore.data
        .map { state -> state[KEY_TRACKING_DURATION] ?: 0L }
        .flowOn(Dispatchers.IO)
        .first()

    override suspend fun getLastResumeTime(): Long = prefDataStore.data
        .map { state -> state[KEY_LAST_RESUME_TIME] ?: getTrackingStartTime() }
        .flowOn(Dispatchers.IO)
        .first()

    override suspend fun setLastResumeTime(resumeTime: Long): Unit = withContext(Dispatchers.IO) {
        prefDataStore.edit { data -> data[KEY_LAST_RESUME_TIME] = resumeTime }
    }

    override suspend fun getInstantSpeed(): Double = prefDataStore.data
        .map { data -> data[KEY_CURRENT_SPEED] ?: 0f }
        .flowOn(Dispatchers.IO)
        .first()
        .toDouble()

    override suspend fun setInstantSpeed(currentSpeed: Double): Unit = withContext(Dispatchers.IO) {
        prefDataStore.edit { data ->
            data[KEY_CURRENT_SPEED] = currentSpeed.toFloat()
        }
    }

    override suspend fun clear(): Unit = withContext(Dispatchers.IO) {
        prefDataStore.edit { state -> state.clear() }
    }

    override suspend fun getActivityType(): ActivityType = prefDataStore.data
        .map { data ->
            data[KEY_ACTIVITY_TYPE]
                ?.let { ActivityType.valueOf(it) }
                ?: ActivityType.Running
        }
        .flowOn(Dispatchers.IO)
        .first()

    override suspend fun setActivityType(activityType: ActivityType) {
        prefDataStore.edit { data -> data[KEY_ACTIVITY_TYPE] = activityType.name }
    }

    companion object {
        private val KEY_TRACKING_STATUS = preferencesKey<Int>("KEY_TRACKING_STATUS")
        private val KEY_ACTIVITY_TYPE = preferencesKey<String>("KEY_ACTIVITY_TYPE")
        private val KEY_ROUTE_DISTANCE = preferencesKey<Float>("KEY_ROUTE_DISTANCE")
        private val KEY_TRACKING_START_TIME = preferencesKey<Long>("KEY_TRACKING_START_TIME")
        private val KEY_CURRENT_SPEED = preferencesKey<Float>("KEY_CURRENT_SPEED")
        private val KEY_TRACKING_DURATION = preferencesKey<Long>("KEY_TRACKING_DURATION")
        private val KEY_LAST_RESUME_TIME = preferencesKey<Long>("KEY_LAST_RESUME_TIME")
    }
}
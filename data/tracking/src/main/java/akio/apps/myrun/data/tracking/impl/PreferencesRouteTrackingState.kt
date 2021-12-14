package akio.apps.myrun.data.tracking.impl

import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.domain.tracking.api.RouteTrackingState
import akio.apps.myrun.domain.tracking.api.RouteTrackingStatus
import akio.apps.myrun.domain.activity.api.model.ActivityType
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.prefDataStore:
    DataStore<Preferences> by preferencesDataStore("route_tracking_state")

class PreferencesRouteTrackingState @Inject constructor(
    application: Application,
) : RouteTrackingState {

    private val prefDataStore: DataStore<Preferences> = application.prefDataStore

    override suspend fun getTrackingStatus(): @RouteTrackingStatus Int = getTrackingStatusFlow()
        .first()

    override fun getTrackingStatusFlow(): Flow<@RouteTrackingStatus Int> = prefDataStore.data
        .map { data -> data[KEY_TRACKING_STATUS] ?: RouteTrackingStatus.STOPPED }
        .distinctUntilChanged() // or it will fire whenever prefDataStore.data change

    override suspend fun setTrackingStatus(@RouteTrackingStatus status: Int) {
        prefDataStore.edit { state -> state[KEY_TRACKING_STATUS] = status }
    }

    override suspend fun setRouteDistance(distance: Double) {
        prefDataStore.edit { state -> state[KEY_ROUTE_DISTANCE] = distance.toFloat() }
    }

    override suspend fun setTrackingStartTime(startTime: Long) {
        prefDataStore.edit { state -> state[KEY_TRACKING_START_TIME] = startTime }
    }

    override suspend fun getTrackingStartTime(): Long? = prefDataStore.data
        .map { data -> data[KEY_TRACKING_START_TIME] }
        .first()

    override suspend fun getRouteDistance(): Double = prefDataStore.data
        .map { state -> state[KEY_ROUTE_DISTANCE] ?: 0f }
        .first()
        .toDouble()

    override suspend fun setTrackingDuration(totalSec: Long) {
        prefDataStore.edit { data ->
            data[KEY_TRACKING_DURATION] = totalSec
        }
    }

    override suspend fun getTrackingDuration(): Long = prefDataStore.data
        .map { state -> state[KEY_TRACKING_DURATION] ?: 0L }
        .first()

    override suspend fun getLastPauseTime(): Long? = prefDataStore.data
        .map { state -> state[KEY_LAST_PAUSE_TIME] }
        .first()

    override suspend fun setLastPauseTime(checkPointMs: Long?) {
        prefDataStore.edit { data ->
            if (checkPointMs == null) {
                data.remove(KEY_LAST_PAUSE_TIME)
            } else {
                data[KEY_LAST_PAUSE_TIME] = checkPointMs
            }
        }
    }

    override suspend fun getPauseDuration(): Long = prefDataStore.data
        .map { state -> state[KEY_PAUSE_DURATION] ?: 0L }
        .first()

    override suspend fun setPauseDuration(duration: Long) {
        prefDataStore.edit { data -> data[KEY_PAUSE_DURATION] = duration }
    }

    override suspend fun setLastLocationUpdateTime(time: Long) {
        prefDataStore.edit { data -> data[KEY_LAST_LOCATION_TIME] = time }
    }

    override suspend fun getLastLocationUpdateTime(): Long? = prefDataStore.data
        .map { state -> state[KEY_LAST_LOCATION_TIME] }
        .first()

    override suspend fun getInstantSpeed(): Double = prefDataStore.data
        .map { data -> data[KEY_CURRENT_SPEED] ?: 0f }
        .first()
        .toDouble()

    override suspend fun setInstantSpeed(currentSpeed: Double) {
        prefDataStore.edit { data ->
            data[KEY_CURRENT_SPEED] = currentSpeed.toFloat()
        }
    }

    override suspend fun clear() {
        prefDataStore.edit { state -> state.clear() }
    }

    override suspend fun getActivityType(): ActivityType = prefDataStore.data
        .map { data ->
            data[KEY_ACTIVITY_TYPE]
                ?.let { ActivityType.from(it) }
                ?: ActivityType.Running
        }
        .first()

    override suspend fun setActivityType(activityType: ActivityType) {
        prefDataStore.edit { data -> data[KEY_ACTIVITY_TYPE] = activityType.identity }
    }

    override suspend fun setStartLocation(location: Location) {
        prefDataStore.edit { data ->
            data[KEY_START_LOCATION_TIME] = location.time
            data[KEY_START_LOCATION_ELAPSED_TIME] = location.elapsedTime
            data[KEY_START_LOCATION_LAT] = location.latitude.toFloat()
            data[KEY_START_LOCATION_LNG] = location.longitude.toFloat()
            data[KEY_START_LOCATION_ALT] = location.altitude.toFloat()
            data[KEY_START_LOCATION_SPEED] = location.speed.toFloat()
        }
    }

    override suspend fun getStartLocation(): Location? = prefDataStore.data.map { data ->
        val time = data[KEY_START_LOCATION_TIME]
        val elapsedTime = data[KEY_START_LOCATION_ELAPSED_TIME]
        val lat = data[KEY_START_LOCATION_LAT]
        val lng = data[KEY_START_LOCATION_LNG]
        val alt = data[KEY_START_LOCATION_ALT]
        val speed = data[KEY_START_LOCATION_SPEED] ?: 0
        if (elapsedTime == null || time == null || lat == null || lng == null || alt == null)
            null
        else
            Location(
                elapsedTime,
                time,
                lat.toDouble(),
                lng.toDouble(),
                alt.toDouble(),
                speed.toDouble()
            )
    }
        .first()

    override suspend fun setPlaceIdentifier(placeIdentifier: String) {
        prefDataStore.edit { data ->
            data[KEY_PLACE_IDENTIFIER] = placeIdentifier
        }
    }

    override suspend fun getPlaceIdentifier(): String? =
        prefDataStore.data.map { data ->
            data[KEY_PLACE_IDENTIFIER]
        }
            .first()

    companion object {
        private val KEY_TRACKING_STATUS = intPreferencesKey("KEY_TRACKING_STATUS")
        private val KEY_ACTIVITY_TYPE = stringPreferencesKey("KEY_ACTIVITY_TYPE")
        private val KEY_ROUTE_DISTANCE = floatPreferencesKey("KEY_ROUTE_DISTANCE")
        private val KEY_TRACKING_START_TIME = longPreferencesKey("KEY_TRACKING_START_TIME")
        private val KEY_CURRENT_SPEED = floatPreferencesKey("KEY_CURRENT_SPEED")
        private val KEY_TRACKING_DURATION = longPreferencesKey("KEY_TRACKING_DURATION")

        private val KEY_START_LOCATION_ELAPSED_TIME =
            longPreferencesKey("KEY_START_LOCATION_ELAPSED_TIME")
        private val KEY_START_LOCATION_TIME = longPreferencesKey("KEY_START_LOCATION_TIME")
        private val KEY_START_LOCATION_LAT = floatPreferencesKey("KEY_START_LOCATION_LAT")
        private val KEY_START_LOCATION_LNG = floatPreferencesKey("KEY_START_LOCATION_LNG")
        private val KEY_START_LOCATION_ALT = floatPreferencesKey("KEY_START_LOCATION_ALT")
        private val KEY_START_LOCATION_SPEED = floatPreferencesKey("KEY_START_LOCATION_SPEED")

        private val KEY_PLACE_IDENTIFIER = stringPreferencesKey("KEY_PLACE_IDENTIFIER")
        private val KEY_LAST_PAUSE_TIME = longPreferencesKey("KEY_LAST_PAUSE_TIME")
        private val KEY_PAUSE_DURATION = longPreferencesKey("KEY_PAUSE_DURATION")
        private val KEY_LAST_LOCATION_TIME = longPreferencesKey("KEY_LAST_LOCATION_TIME")
    }
}

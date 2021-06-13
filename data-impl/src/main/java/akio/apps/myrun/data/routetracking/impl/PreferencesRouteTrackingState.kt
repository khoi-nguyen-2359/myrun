package akio.apps.myrun.data.routetracking.impl

import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.prefDataStore: DataStore<Preferences> by preferencesDataStore("route_tracking_state")

class PreferencesRouteTrackingState @Inject constructor(
    application: Application
) : RouteTrackingState {

    private val prefDataStore: DataStore<Preferences> = application.prefDataStore

    override suspend fun getTrackingStatus(): @RouteTrackingStatus Int = getTrackingStatusFlow()
        .first()

    override fun getTrackingStatusFlow(): Flow<@RouteTrackingStatus Int> = prefDataStore.data
        .map { data -> data[KEY_TRACKING_STATUS] ?: RouteTrackingStatus.STOPPED }

    override suspend fun setTrackingStatus(@RouteTrackingStatus status: Int) {
        prefDataStore.edit { state -> state[KEY_TRACKING_STATUS] = status }
    }

    override suspend fun setRouteDistance(distance: Double) {
        prefDataStore.edit { state -> state[KEY_ROUTE_DISTANCE] = distance.toFloat() }
    }

    override suspend fun setTrackingStartTime(startTime: Long) {
        prefDataStore.edit { state ->
            state[KEY_TRACKING_START_TIME] = startTime
        }
    }

    override suspend fun getTrackingStartTime(): Long = prefDataStore.data
        .map { data -> data[KEY_TRACKING_START_TIME] ?: 0L }
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

    override suspend fun getLastResumeTime(): Long = prefDataStore.data
        .map { state -> state[KEY_LAST_RESUME_TIME] ?: getTrackingStartTime() }
        .first()

    override suspend fun setLastResumeTime(resumeTime: Long) {
        prefDataStore.edit { data -> data[KEY_LAST_RESUME_TIME] = resumeTime }
    }

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
        prefDataStore.edit { data -> data[KEY_ACTIVITY_TYPE] = activityType.name }
    }

    override suspend fun setStartLocation(location: LocationEntity) {
        prefDataStore.edit { data ->
            data[KEY_START_LOCATION_LAT] = location.latitude.toFloat()
            data[KEY_START_LOCATION_LNG] = location.longitude.toFloat()
            data[KEY_START_LOCATION_ALT] = location.altitude.toFloat()
        }
    }

    override suspend fun getStartLocation(): LocationEntity? = prefDataStore.data.map { data ->
        val lat = data[KEY_START_LOCATION_LAT]
        val lng = data[KEY_START_LOCATION_LNG]
        val alt = data[KEY_START_LOCATION_ALT]
        if (lat == null || lng == null || alt == null)
            null
        else
            LocationEntity(lat.toDouble(), lng.toDouble(), alt.toDouble())
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
        private val KEY_LAST_RESUME_TIME = longPreferencesKey("KEY_LAST_RESUME_TIME")
        private val KEY_START_LOCATION_LAT = floatPreferencesKey("KEY_START_LOCATION_LAT")
        private val KEY_START_LOCATION_LNG = floatPreferencesKey("KEY_START_LOCATION_LNG")
        private val KEY_START_LOCATION_ALT = floatPreferencesKey("KEY_START_LOCATION_ALT")
        private val KEY_PLACE_IDENTIFIER = stringPreferencesKey("KEY_PLACE_IDENTIFIER")
    }
}

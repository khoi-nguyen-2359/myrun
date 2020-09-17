package akio.apps.myrun.data.routetracking.impl

import akio.apps.myrun.data.routetracking.RouteTrackingState
import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RouteTrackingStateImpl @Inject constructor(
    appContext: Context
) : RouteTrackingState {

    private val prefDataStore: DataStore<Preferences> = appContext.createDataStore("route_tracking_state")

    init {

    }

    override suspend fun isRouteTrackingInProgress(): Boolean {
        return prefDataStore.data.map { data -> data[KEY_IS_ROUTE_TRACKING] ?: false }
            .flowOn(Dispatchers.IO)
            .single()
    }

    override suspend fun setRouteTrackingInProgress(isTracking: Boolean): Unit = withContext(Dispatchers.IO) {
        prefDataStore.edit { state ->
            state[KEY_IS_ROUTE_TRACKING] = isTracking
        }
    }

    companion object {
        private val KEY_IS_ROUTE_TRACKING = preferencesKey<Boolean>("IS_TRACKING_ROUTE")
    }
}
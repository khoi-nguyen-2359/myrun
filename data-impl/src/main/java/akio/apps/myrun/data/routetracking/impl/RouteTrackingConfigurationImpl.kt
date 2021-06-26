package akio.apps.myrun.data.routetracking.impl

import akio.apps.myrun.data.routetracking.RouteTrackingConfiguration
import akio.apps.myrun.data.routetracking.model.LocationRequestConfig
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prefDataStore: DataStore<Preferences> by
preferencesDataStore("RouteTrackingConfigurationImpl")

class RouteTrackingConfigurationImpl @Inject constructor(application: Application) :
    RouteTrackingConfiguration {

    private val prefDataStore = application.prefDataStore

    override fun getLocationRequestConfig(): Flow<LocationRequestConfig> =
        prefDataStore.data.map { data ->
            LocationRequestConfig(
                updateInterval = data[LOCATION_UPDATE_INTERVAL_KEY] ?: LOCATION_UPDATE_INTERVAL,
                fastestUpdateInterval = data[LOCATION_FASTEST_UPDATE_INTERVAL_KEY]
                    ?: LOCATION_FASTEST_UPDATE_INTERVAL,
                smallestDisplacement = data[LOCATION_SMALLEST_DISPLACEMENT] ?: SMALLEST_DISPLACEMENT
            )
        }

    override suspend fun setLocationRequestConfiguration(config: LocationRequestConfig) {
        prefDataStore.edit { data ->
            data[LOCATION_UPDATE_INTERVAL_KEY] = config.updateInterval
            data[LOCATION_FASTEST_UPDATE_INTERVAL_KEY] = config.fastestUpdateInterval
            data[LOCATION_SMALLEST_DISPLACEMENT] = config.smallestDisplacement
        }
    }

    override suspend fun setLocationAccumulationEnabled(isEnabled: Boolean) {
        prefDataStore.edit { data -> data[LOCATION_ACCUMULATION_ENABLED] = isEnabled }
    }

    override fun isLocationAccumulationEnabled(): Flow<Boolean> =
        prefDataStore.data.map { data -> data[LOCATION_ACCUMULATION_ENABLED] ?: false }

    companion object {
        private const val LOCATION_UPDATE_INTERVAL = 2000L
        private const val LOCATION_FASTEST_UPDATE_INTERVAL = 500L
        private const val SMALLEST_DISPLACEMENT = 5f

        private val LOCATION_UPDATE_INTERVAL_KEY =
            longPreferencesKey("LOCATION_UPDATE_INTERVAL_KEY")
        private val LOCATION_FASTEST_UPDATE_INTERVAL_KEY =
            longPreferencesKey("LOCATION_FASTEST_UPDATE_INTERVAL_KEY")
        private val LOCATION_SMALLEST_DISPLACEMENT =
            floatPreferencesKey("LOCATION_SMALLEST_DISPLACEMENT")
        private val LOCATION_ACCUMULATION_ENABLED =
            booleanPreferencesKey("LOCATION_ACCUMULATION_ENABLED")
    }
}

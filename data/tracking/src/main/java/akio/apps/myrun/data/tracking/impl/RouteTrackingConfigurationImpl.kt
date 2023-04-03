package akio.apps.myrun.data.tracking.impl

import akio.apps.myrun.data.location.api.model.LocationRequestConfig
import akio.apps.myrun.data.tracking.api.RouteTrackingConfiguration
import akio.apps.myrun.data.tracking.api.model.LocationProcessingConfig
import akio.apps.myrun.data.tracking.di.TrackingDataScope
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.prefDataStore:
    DataStore<Preferences> by preferencesDataStore("route_tracking_configuration_prefs")

@Singleton
@ContributesBinding(TrackingDataScope::class)
class RouteTrackingConfigurationImpl @Inject constructor(
    application: Application,
) : RouteTrackingConfiguration {

    private val prefDataStore = application.prefDataStore
    override suspend fun getLocationRequestConfig(): LocationRequestConfig =
        getLocationRequestConfigFlow().first()

    override fun getLocationRequestConfigFlow(): Flow<LocationRequestConfig> =
        prefDataStore.data.map { data ->
            LocationRequestConfig(
                updateInterval = data[LOCATION_UPDATE_INTERVAL_KEY] ?: LOCATION_UPDATE_INTERVAL,
                minUpdateInterval = data[LOCATION_FASTEST_UPDATE_INTERVAL_KEY]
                    ?: LOCATION_MIN_UPDATE_INTERVAL,
                minUpdateDistance = data[LOCATION_SMALLEST_DISPLACEMENT] ?: MIN_UPDATE_DISTANCE,
                maxUpdateInterval = data[LOCATION_MAX_UPDATE_INTERVAL_KEY]
                    ?: LOCATION_MAX_UPDATE_INTERVAL
            )
        }

    override suspend fun setLocationRequestConfiguration(config: LocationRequestConfig) {
        prefDataStore.edit { data ->
            data[LOCATION_UPDATE_INTERVAL_KEY] = config.updateInterval
            data[LOCATION_FASTEST_UPDATE_INTERVAL_KEY] = config.minUpdateInterval
            data[LOCATION_SMALLEST_DISPLACEMENT] = config.minUpdateDistance
            data[LOCATION_MAX_UPDATE_INTERVAL_KEY] = config.maxUpdateInterval
        }
    }

    override suspend fun setLocationProcessingConfig(config: LocationProcessingConfig) {
        prefDataStore.edit { data ->
            data[LOCATION_AVG_ACCUMULATION_ENABLED] = config.isAvgAccumulatorEnabled
            data[LOCATION_SPEED_FILTER_ENABLED] = config.isSpeedFilterEnabled
        }
    }

    override fun getLocationProcessingConfig(): Flow<LocationProcessingConfig> =
        prefDataStore.data.map { data ->
            val isAvgAccumEnabled = data[LOCATION_AVG_ACCUMULATION_ENABLED] ?: AVG_ACCUM_ENABLED
            val isSpeedFilterEnabled = data[LOCATION_SPEED_FILTER_ENABLED] ?: SPEED_FILTER_ENABLED
            LocationProcessingConfig(isAvgAccumEnabled, isSpeedFilterEnabled)
        }

    companion object {
        private const val LOCATION_UPDATE_INTERVAL = 1000L
        private const val LOCATION_MIN_UPDATE_INTERVAL = 500L
        private const val LOCATION_MAX_UPDATE_INTERVAL = 1000L
        private const val MIN_UPDATE_DISTANCE = 1f
        private const val AVG_ACCUM_ENABLED = false
        private const val SPEED_FILTER_ENABLED = false

        private val LOCATION_UPDATE_INTERVAL_KEY =
            longPreferencesKey("LOCATION_UPDATE_INTERVAL_KEY")
        private val LOCATION_FASTEST_UPDATE_INTERVAL_KEY =
            longPreferencesKey("LOCATION_FASTEST_UPDATE_INTERVAL_KEY")
        private val LOCATION_SMALLEST_DISPLACEMENT =
            floatPreferencesKey("LOCATION_SMALLEST_DISPLACEMENT")
        private val LOCATION_MAX_UPDATE_INTERVAL_KEY =
            longPreferencesKey("LOCATION_MAX_UPDATE_INTERVAL_KEY")
        private val LOCATION_SPEED_FILTER_ENABLED =
            booleanPreferencesKey("LOCATION_SPEED_FILTER_ENABLED")
        private val LOCATION_AVG_ACCUMULATION_ENABLED =
            booleanPreferencesKey("LOCATION_AVG_ACCUMULATION_ENABLED")
    }
}

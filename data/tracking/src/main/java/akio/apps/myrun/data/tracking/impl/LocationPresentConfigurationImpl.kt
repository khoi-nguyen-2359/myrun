package akio.apps.myrun.data.tracking.impl

import akio.apps.myrun.data.tracking.api.LocationPresentConfiguration
import akio.apps.myrun.data.tracking.di.TrackingDataScope
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prefDataStore:
    DataStore<Preferences> by preferencesDataStore("location_present_configuration_prefs")

@Singleton
@ContributesBinding(TrackingDataScope::class)
class LocationPresentConfigurationImpl @Inject constructor(
    application: Application,
) : LocationPresentConfiguration {
    private val prefDataStore: DataStore<Preferences> = application.prefDataStore

    override suspend fun setBSplinesEnabled(isEnabled: Boolean) {
        prefDataStore.edit { data -> data[KEY_BSPLINES_ENABLED] = isEnabled }
    }

    override fun isBSplinesEnabledFlow(): Flow<Boolean> =
        prefDataStore.data.map { data -> data[KEY_BSPLINES_ENABLED] ?: true }

    companion object {
        private val KEY_BSPLINES_ENABLED = booleanPreferencesKey("KEY_BSPLINES_ENABLED")
    }
}

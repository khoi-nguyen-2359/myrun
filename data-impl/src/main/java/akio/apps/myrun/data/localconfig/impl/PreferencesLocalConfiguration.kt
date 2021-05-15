package akio.apps.myrun.data.localconfig.impl

import akio.apps.myrun.data.localconfig.LocalConfiguration
import akio.apps.myrun.data.recentplace.entity.PlaceIdentifier
import android.app.Application
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PreferencesLocalConfiguration @Inject constructor(
    application: Application
) : LocalConfiguration {

    private val prefDataStore: DataStore<Preferences> =
        application.createDataStore("local_configuration")

    override suspend fun setRecentPlaceIdentifier(placeIdentifier: PlaceIdentifier) {
        prefDataStore.edit { preferences ->
            preferences[KEY_RECENT_PLACE_IDENTIFIER] = placeIdentifier.identifier
        }
    }

    override suspend fun getRecentPlaceIdentifier(): PlaceIdentifier? =
        prefDataStore.data.map { preferences ->
            val identifier = preferences[KEY_RECENT_PLACE_IDENTIFIER]
            if (identifier != null) {
                PlaceIdentifier(identifier)
            } else {
                null
            }
        }
            .first()

    companion object {
        private val KEY_RECENT_PLACE_IDENTIFIER =
            preferencesKey<String>("KEY_RECENT_PLACE_IDENTIFIER")
    }
}

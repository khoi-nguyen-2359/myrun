package akio.apps.myrun.data.localconfig.impl

import akio.apps.myrun.data.localconfig.LocalConfiguration
import android.app.Application
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.preferencesKey
import javax.inject.Inject

class PreferencesLocalConfiguration @Inject constructor(
    application: Application
) : LocalConfiguration {

    private val prefDataStore: DataStore<Preferences> =
        application.createDataStore("local_configuration")

    companion object {
        private val KEY_RECENT_PLACE_IDENTIFIER =
            preferencesKey<String>("KEY_RECENT_PLACE_IDENTIFIER")
    }
}

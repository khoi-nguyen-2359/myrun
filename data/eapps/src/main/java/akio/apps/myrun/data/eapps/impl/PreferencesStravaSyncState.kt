package akio.apps.myrun.data.eapps.impl

import akio.apps.myrun.data.eapps.api.StravaSyncState
import akio.apps.myrun.data.eapps.di.ExternalAppDataScope
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.prefDataStore:
    DataStore<Preferences> by preferencesDataStore("strava_sync_state")

@Singleton
@ContributesBinding(ExternalAppDataScope::class)
class PreferencesStravaSyncState @Inject constructor(
    application: Application,
) : StravaSyncState {

    private val prefDataStore: DataStore<Preferences> = application.prefDataStore

    override suspend fun setStravaSyncAccountId(accountId: String?) {
        prefDataStore.edit { data ->
            if (accountId != null) {
                data[KEY_ACCOUNT_ID] = accountId
            } else {
                data.remove(KEY_ACCOUNT_ID)
            }
        }
    }

    override suspend fun getStravaSyncAccountId(): String? = prefDataStore.data.map {
        it[KEY_ACCOUNT_ID]
    }.first()

    companion object {
        private val KEY_ACCOUNT_ID = stringPreferencesKey("KEY_ACCOUNT_ID")
    }
}

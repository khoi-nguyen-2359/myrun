package akio.apps.myrun.data.user.impl

import akio.apps.myrun.data.user.api.AppVersionMigrationState
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.prefDataStore:
    DataStore<Preferences> by preferencesDataStore("app_version_migration_state")

class PreferenceAppVersionMigrationState @Inject constructor(
    application: Application
) : AppVersionMigrationState {
    private val prefs = application.prefDataStore

    override suspend fun isMigrationCompleted(): Boolean = prefs.data
        .map { data -> data[KEY_IS_MIGRATION_COMPLETED] ?: false }
        .first()

    override suspend fun setMigrationCompleted(isCompleted: Boolean) {
        prefs.edit { date -> date[KEY_IS_MIGRATION_COMPLETED] = isCompleted }
    }

    companion object {
        private val KEY_IS_MIGRATION_COMPLETED =
            booleanPreferencesKey("KEY_IS_MIGRATION_COMPLETED")
    }
}

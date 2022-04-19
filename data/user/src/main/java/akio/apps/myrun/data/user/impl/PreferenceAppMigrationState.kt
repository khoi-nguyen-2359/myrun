package akio.apps.myrun.data.user.impl

import akio.apps.myrun.data.user.api.AppMigrationState
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
    DataStore<Preferences> by preferencesDataStore("app_version_migration_state_prefs")

class PreferenceAppMigrationState @Inject constructor(
    application: Application,
) : AppMigrationState {
    private val prefs = application.prefDataStore

    override suspend fun isMigrationSucceeded(
        appVersionString: String,
        isSingleTask: Boolean,
    ): Boolean = prefs.data
        .map { data ->
            val prefKeyName = createPrefKeyOfAppVersion(appVersionString, isSingleTask)
            data[booleanPreferencesKey(prefKeyName)] ?: false
        }
        .first()

    override suspend fun setMigrationSucceeded(
        appVersionString: String,
        isCompleted: Boolean,
        isSingleTask: Boolean,
    ) {
        prefs.edit { date ->
            val prefKeyName = createPrefKeyOfAppVersion(appVersionString, isSingleTask)
            date[booleanPreferencesKey(prefKeyName)] = isCompleted
        }
    }

    private fun createPrefKeyOfAppVersion(appVersionString: String, isSingleTask: Boolean) =
        "{$KEY_IS_MIGRATION_COMPLETED}_$appVersionString" + if (isSingleTask) {
            "single"
        } else {
            "all"
        }

    companion object {
        private const val KEY_IS_MIGRATION_COMPLETED = "KEY_IS_MIGRATION_COMPLETED"
    }
}

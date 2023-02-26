package akio.apps.myrun.data.user.impl

import akio.apps.myrun.data.authentication.di.AuthenticationDataScope
import akio.apps.myrun.data.user.api.UserPreferences
import akio.apps.myrun.data.user.api.model.MeasureSystem
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prefDataStore:
    DataStore<Preferences> by preferencesDataStore(
        "akio.apps.myrun.data.user.impl.user_preferences_impl"
    )

@Singleton
@ContributesBinding(AuthenticationDataScope::class)
class UserPreferencesImpl @Inject constructor(application: Application) : UserPreferences {
    private val prefs = application.prefDataStore

    override fun getMeasureSystemFlow(): Flow<MeasureSystem> = prefs.data.map { prefs ->
        val id = prefs[KEY_MEASURE_UNIT_SYSTEM]
        MeasureSystem.createFromId(id)
    }

    override suspend fun setMeasureSystem(measureSystem: MeasureSystem) {
        prefs.edit { prefs ->
            prefs[KEY_MEASURE_UNIT_SYSTEM] = measureSystem.id
        }
    }

    companion object {
        private val KEY_MEASURE_UNIT_SYSTEM: Preferences.Key<String> =
            stringPreferencesKey("KEY_MEASURE_UNIT_SYSTEM")
    }
}

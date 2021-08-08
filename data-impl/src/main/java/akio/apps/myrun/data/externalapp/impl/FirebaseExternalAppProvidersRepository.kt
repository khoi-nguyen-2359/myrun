package akio.apps.myrun.data.externalapp.impl

import akio.apps._base.Resource
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.mapper.FirestoreProvidersMapper
import akio.apps.myrun.data.externalapp.mapper.FirestoreStravaTokenMapper
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import akio.apps.myrun.data.externalapp.model.FirestoreProviders
import akio.apps.myrun.data.externalapp.model.RunningApp
import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private val Context.prefDataStore: DataStore<Preferences> by
preferencesDataStore("FirebaseExternalAppProvidersRepository")

class FirebaseExternalAppProvidersRepository @Inject constructor(
    private val application: Application,
    private val firestore: FirebaseFirestore,
    private val firestoreStravaTokenMapper: FirestoreStravaTokenMapper,
    private val firestoreProvidersMapper: FirestoreProvidersMapper
) : ExternalAppProvidersRepository {

    private val prefDataStore: DataStore<Preferences> = application.prefDataStore

    private fun getProviderTokenDocument(userUid: String): DocumentReference {
        return firestore.collection(PROVIDERS_COLLECTION_PATH)
            .document(userUid)
    }

    @ExperimentalCoroutinesApi
    override fun getExternalProvidersFlow(accountId: String): Flow<Resource<ExternalProviders>> =
        callbackFlow {
            val providerTokenDocument = getProviderTokenDocument(accountId)
            try {
                val cached = providerTokenDocument.get(Source.CACHE)
                    .await()
                    .toObject(FirestoreProviders::class.java)
                    ?.run(firestoreProvidersMapper::map)

                setStravaSyncEnabled(cached?.strava != null)

                send(Resource.Loading(cached))
            } catch (ex: Exception) {
                send(Resource.Loading<ExternalProviders>(null))
            }

            val listener = providerTokenDocument.addSnapshotListener { snapshot, error ->
                if (error == null) {
                    val providers = snapshot?.toObject(FirestoreProviders::class.java)
                        ?.run(firestoreProvidersMapper::map)
                        ?: ExternalProviders.createEmpty()
                    CoroutineScope(Dispatchers.IO).launch {
                        setStravaSyncEnabled(providers.strava != null)
                    }
                    sendBlocking(Resource.Success(providers))
                } else {
                    sendBlocking(Resource.Error<ExternalProviders>(error))
                }
            }

            awaitClose {
                runBlocking(Dispatchers.Main.immediate) {
                    listener.remove()
                }
            }
        }
            .flowOn(Dispatchers.IO)

    override suspend fun getExternalProviders(
        accountId: String
    ): ExternalProviders = withContext(Dispatchers.IO) {
        val allTokenData = getProviderTokenDocument(accountId)
            .get()
            .await()
            .toObject(FirestoreProviders::class.java)
            ?.run(firestoreProvidersMapper::map)
            ?: ExternalProviders.createEmpty()

        setStravaSyncEnabled(allTokenData.strava != null)

        allTokenData
    }

    override suspend fun updateStravaProvider(
        accountId: String,
        token: ExternalAppToken.StravaToken
    ): Unit = withContext(Dispatchers.IO) {
        val providerTokenDocument = getProviderTokenDocument(accountId)
        val providerToken = FirestoreProviders.FirestoreProviderToken(
            RunningApp.Strava.appName,
            firestoreStravaTokenMapper.mapReversed(token)
        )
        providerTokenDocument.set(
            mapOf(RunningApp.Strava.id to providerToken),
            SetOptions.merge()
        )
            .await()

        setStravaSyncEnabled(true)
    }

    override suspend fun removeStravaProvider(
        accountId: String
    ): Unit = withContext(Dispatchers.IO) {
        val providerTokenDocument = getProviderTokenDocument(accountId)
        providerTokenDocument.set(mapOf(RunningApp.Strava.id to null), SetOptions.merge()).await()
        setStravaSyncEnabled(false)
    }

    override suspend fun getStravaProviderToken(
        accountId: String
    ): ExternalAppToken.StravaToken? = withContext(Dispatchers.IO) {
        // TODO: add document path to get directly the strava token?
        val tokenData = getProviderTokenDocument(accountId)
            .get()
            .await()
            .toObject(FirestoreProviders::class.java)
            ?.run(firestoreProvidersMapper::map)
            ?.strava
            ?.token

        setStravaSyncEnabled(tokenData != null)

        tokenData
    }

    private suspend fun setStravaSyncEnabled(isEnabled: Boolean) {
        prefDataStore.edit { data -> data[KEY_IS_STRAVA_SYNC_ENABLED] = isEnabled }
    }

    override suspend fun isStravaSyncEnabled(): Boolean = prefDataStore.data.map { prefs ->
        prefs[KEY_IS_STRAVA_SYNC_ENABLED] ?: false
    }
        .first()

    companion object {
        private const val PROVIDERS_COLLECTION_PATH = "providers"
        private val KEY_IS_STRAVA_SYNC_ENABLED = booleanPreferencesKey("KEY_IS_STRAVA_SYNC_ENABLED")
    }
}

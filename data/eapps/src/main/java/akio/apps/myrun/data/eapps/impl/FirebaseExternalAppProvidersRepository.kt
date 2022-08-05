package akio.apps.myrun.data.eapps.impl

import akio.apps.myrun.data.common.Resource
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.api.model.ExternalProviders
import akio.apps.myrun.data.eapps.api.model.RunningApp
import akio.apps.myrun.data.eapps.di.ExternalAppDataScope
import akio.apps.myrun.data.eapps.impl.mapper.FirestoreProvidersMapper
import akio.apps.myrun.data.eapps.impl.mapper.FirestoreStravaTokenMapper
import akio.apps.myrun.data.eapps.impl.model.FirestoreProviders
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

@Singleton
@ContributesBinding(ExternalAppDataScope::class)
class FirebaseExternalAppProvidersRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firestoreStravaTokenMapper: FirestoreStravaTokenMapper,
    private val firestoreProvidersMapper: FirestoreProvidersMapper,
) : ExternalAppProvidersRepository {

    private fun getProviderTokenDocument(userUid: String): DocumentReference {
        return firestore.collection(PROVIDERS_COLLECTION_PATH).document(userUid)
    }

    override fun getExternalProvidersFlow(
        accountId: String,
    ): Flow<Resource<out ExternalProviders>> = callbackFlow {
        val providerTokenDocument = getProviderTokenDocument(accountId)
        try {
            val cached = providerTokenDocument.get(Source.CACHE)
                .await()
                .toObject(FirestoreProviders::class.java)
                ?.run(firestoreProvidersMapper::map)

            send(Resource.Loading(cached))
        } catch (ex: Exception) {
            send(Resource.Loading(null))
        }

        val listener = providerTokenDocument.addSnapshotListener { snapshot, error ->
            if (error == null) {
                val providers = snapshot?.toObject(FirestoreProviders::class.java)
                    ?.run(firestoreProvidersMapper::map)
                    ?: ExternalProviders.createEmpty()
                trySendBlocking(Resource.Success(providers))
            } else {
                trySendBlocking(Resource.Error<ExternalProviders>(error))
            }
        }

        awaitClose {
            runBlocking(Dispatchers.Main.immediate) {
                listener.remove()
            }
        }
    }

    override suspend fun updateStravaProvider(
        accountId: String,
        token: ExternalAppToken.StravaToken,
    ) {
        val providerTokenDocument = getProviderTokenDocument(accountId)
        val providerToken = FirestoreProviders.FirestoreProviderToken(
            RunningApp.Strava.appName,
            firestoreStravaTokenMapper.mapReversed(token)
        )
        providerTokenDocument.set(
            mapOf(RunningApp.Strava.id to providerToken),
            SetOptions.merge()
        ).await()
    }

    override suspend fun removeStravaProvider(accountId: String) {
        val providerTokenDocument = getProviderTokenDocument(accountId)
        providerTokenDocument.set(mapOf(RunningApp.Strava.id to null), SetOptions.merge()).await()
    }

    override suspend fun getStravaProviderToken(accountId: String): ExternalAppToken.StravaToken? {
        // TODO: add document path to get directly the strava token?
        val tokenData = getProviderTokenDocument(accountId)
            .get()
            .await()
            .toObject(FirestoreProviders::class.java)
            ?.run(firestoreProvidersMapper::map)
            ?.strava
            ?.token

        return tokenData
    }

    companion object {
        private const val PROVIDERS_COLLECTION_PATH = "providers"
    }
}

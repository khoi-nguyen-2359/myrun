package akio.apps.myrun.data.externalapp.impl

import akio.apps._base.data.Resource
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.entity.FirestoreProvidersEntity
import akio.apps.myrun.data.externalapp.mapper.FirestoreProvidersMapper
import akio.apps.myrun.data.externalapp.mapper.FirestoreStravaTokenMapper
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.ExternalProviders
import akio.apps.myrun.data.externalapp.model.RunningApp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ExternalAppProvidersRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firestoreStravaTokenMapper: FirestoreStravaTokenMapper,
    private val firestoreProvidersMapper: FirestoreProvidersMapper
) : ExternalAppProvidersRepository {

    companion object {
        const val PROVIDERS_COLLECTION_PATH = "providers"
    }

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
                    .toObject(FirestoreProvidersEntity::class.java)
                    ?.run(firestoreProvidersMapper::map)

                send(Resource.Loading(cached))
            } catch (ex: Exception) {
                send(Resource.Loading<ExternalProviders>(null))
            }

            val listener = providerTokenDocument.addSnapshotListener { snapshot, error ->
                if (error == null) {
                    val providers = snapshot?.toObject(FirestoreProvidersEntity::class.java)
                        ?.run(firestoreProvidersMapper::map)
                        ?: ExternalProviders.createEmpty()
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

    override suspend fun getExternalProviders(accountId: String): ExternalProviders {
        return getProviderTokenDocument(accountId)
            .get()
            .await()
            .toObject(FirestoreProvidersEntity::class.java)
            ?.run(firestoreProvidersMapper::map)
            ?: ExternalProviders.createEmpty()
    }

    override fun updateStravaProvider(accountId: String, token: ExternalAppToken.StravaToken) {
        val providerTokenDocument = getProviderTokenDocument(accountId)
        providerTokenDocument.set(
            mapOf(RunningApp.Strava.id to token.let {
                FirestoreProvidersEntity.FirestoreProviderToken(
                    RunningApp.Strava.appName,
                    firestoreStravaTokenMapper.mapReversed(it)
                )
            }),
            SetOptions.merge()
        )
    }

    override fun removeStravaProvider(accountId: String) {
        val providerTokenDocument = getProviderTokenDocument(accountId)
        providerTokenDocument.set(mapOf(RunningApp.Strava.id to null), SetOptions.merge())
    }
}

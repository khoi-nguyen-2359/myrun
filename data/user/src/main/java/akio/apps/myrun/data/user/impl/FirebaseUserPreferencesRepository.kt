package akio.apps.myrun.data.user.impl

import akio.apps.myrun.data.authentication.di.AuthenticationDataScope
import akio.apps.myrun.data.user.api.UserPreferencesRepository
import akio.apps.myrun.data.user.api.model.UserPreferences
import akio.apps.myrun.data.user.impl.model.FirestoreUser
import akio.apps.myrun.data.user.impl.model.FirestoreUserPreferences
import akio.apps.myrun.data.user.impl.model.FirestoreUserPreferencesUpdateMap
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.squareup.anvil.annotations.ContributesBinding
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@Singleton
@ContributesBinding(AuthenticationDataScope::class)
class FirebaseUserPreferencesRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserPreferencesRepository {

    private fun getUserDocument(userId: String): DocumentReference {
        return firestore.collection(FIRESTORE_USERS_DOCUMENT).document(userId)
    }

    override fun getUserPreferencesFlow(userId: String): Flow<UserPreferences> = callbackFlow {
        val listener = withContext(Dispatchers.Main.immediate) {
            getUserDocument(userId).addSnapshotListener { snapshot, _ ->
                val fsUserProfile = snapshot?.toObject(FirestoreUser::class.java)
                    ?: return@addSnapshotListener

                trySend(fsUserProfile.preferences.toRepoModel())
            }
        }

        awaitClose {
            runBlocking(Dispatchers.Main.immediate) {
                listener.remove()
            }
        }
    }

    override fun setShowActivityMapOnFeed(userId: String, isShow: Boolean) {
        getUserDocument(userId).set(
            FirestoreUserPreferencesUpdateMap().apply {
                isMapVisible(isShow)
            },
            SetOptions.merge()
        )
    }

    private fun FirestoreUserPreferences.toRepoModel(): UserPreferences = UserPreferences(
        showActivityMapOnFeed
    )

    companion object {
        private const val FIRESTORE_USERS_DOCUMENT = "users"
    }
}

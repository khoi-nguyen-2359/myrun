package akio.apps.myrun.data.authentication.impl

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.authentication.api.model.UserAccount
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class FirebaseAuthenticationState @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseUserMapper: FirebaseUserMapper
) : UserAuthenticationState {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUserAccountFlow(): Flow<UserAccount?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener {
            it.currentUser?.let { firebaseUser ->
                trySendBlocking(firebaseUserMapper.map(firebaseUser))
            } ?: run {
                trySendBlocking(null)
            }
        }

        withContext(Dispatchers.Main.immediate) {
            firebaseAuth.addAuthStateListener(listener)
        }

        awaitClose {
            runBlocking(Dispatchers.Main.immediate) {
                firebaseAuth.removeAuthStateListener(listener)
            }
        }
    }

    override fun getUserAccount(): UserAccount? =
        firebaseAuth.currentUser?.let { firebaseUserMapper.map(it) }

    override fun getUserAccountId(): String? = firebaseAuth.currentUser?.uid
    override fun requireUserAccountId(): String = getUserAccountId()
        ?: throw IllegalStateException("User id is required but null.")

    override fun isLinkedWithFacebook(): Boolean =
        firebaseAuth.currentUser?.providerData
            ?.any { it.providerId == FacebookAuthProvider.PROVIDER_ID }
            ?: false

    override fun isSignedIn(): Boolean = firebaseAuth.currentUser?.uid != null
    override fun isAnonymousUser(): Boolean = firebaseAuth.currentUser?.isAnonymous == true

    override fun clear() {
        firebaseAuth.signOut()
    }
}
package akio.apps.myrun.feature.splash.impl

import akio.apps.myrun.feature.splash.SignInAnonymouslyUsecase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignInAnonymouslyUsecaseImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : SignInAnonymouslyUsecase {

    override suspend fun signInAnonymously(): Unit = withContext(Dispatchers.IO) {
        firebaseAuth.signInAnonymously().await()
    }
}
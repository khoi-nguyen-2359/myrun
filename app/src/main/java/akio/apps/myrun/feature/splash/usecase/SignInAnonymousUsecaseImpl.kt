package akio.apps.myrun.feature.splash.usecase

import akio.apps.myrun.feature.splash.SignInAnonymousUsecase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SignInAnonymousUsecaseImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : SignInAnonymousUsecase {

    override suspend fun signInAnonymously(): Unit = withContext(Dispatchers.IO) {
        firebaseAuth.signInAnonymously().await()
    }
}
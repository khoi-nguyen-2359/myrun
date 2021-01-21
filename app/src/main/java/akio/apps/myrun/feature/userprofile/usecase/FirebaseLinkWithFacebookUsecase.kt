package akio.apps.myrun.feature.userprofile.usecase

import akio.apps.myrun.feature.userprofile.LinkFacebookUsecase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseLinkWithFacebookUsecase @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : LinkFacebookUsecase {
    override suspend fun linkFacebook(accessTokenValue: String) {
        val credential = FacebookAuthProvider.getCredential(accessTokenValue)
        firebaseAuth.currentUser?.linkWithCredential(credential)
            ?.await()
    }
}

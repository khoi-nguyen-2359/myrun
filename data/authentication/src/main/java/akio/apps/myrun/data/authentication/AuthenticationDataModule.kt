package akio.apps.myrun.data.authentication

import akio.apps.myrun.data.authentication.api.SignInManager
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.authentication.impl.FirebaseAuthenticationState
import akio.apps.myrun.data.authentication.impl.FirebaseSignInManager
import akio.apps.myrun.data.firebase.FirebaseDataModule
import dagger.Binds
import dagger.Module

@Module(includes = [FirebaseDataModule::class])
interface AuthenticationDataModule {
    @Binds
    fun userAuthStorage(state: FirebaseAuthenticationState): UserAuthenticationState

    @Binds
    fun signInManager(manager: FirebaseSignInManager): SignInManager
}

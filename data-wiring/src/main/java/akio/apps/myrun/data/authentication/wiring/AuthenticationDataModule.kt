package akio.apps.myrun.data.authentication.wiring

import akio.apps.myrun.data.authentication.SignInManager
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.authentication.impl.FirebaseAuthenticationState
import akio.apps.myrun.data.authentication.impl.FirebaseSignInManager
import dagger.Binds
import dagger.Module

@Module
internal interface AuthenticationDataModule {
    @Binds
    fun userAuthStorage(state: FirebaseAuthenticationState): UserAuthenticationState

    @Binds
    fun signInManager(manager: FirebaseSignInManager): SignInManager
}

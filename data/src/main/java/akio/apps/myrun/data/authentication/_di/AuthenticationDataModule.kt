package akio.apps.myrun.data.authentication._di

import akio.apps.myrun.data._base.FirebaseDataModule
import akio.apps.myrun.data.authentication.SignInManager
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.authentication.impl.FirebaseAuthenticationState
import akio.apps.myrun.data.authentication.impl.FirebaseSignInManager
import dagger.Binds
import dagger.Module

@Module(includes = [FirebaseDataModule::class])
interface AuthenticationDataModule {
    @Binds
    fun userAuthStorage(state: FirebaseAuthenticationState): UserAuthenticationState

    @Binds
    fun signInManager(manager: FirebaseSignInManager): SignInManager
}

package akio.apps.myrun.data.authentication.wiring

import akio.apps.myrun.data.authentication.api.SignInManager
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.authentication.impl.FirebaseAuthenticationState
import akio.apps.myrun.data.authentication.impl.FirebaseSignInManager
import akio.apps.myrun.data.wiring.FirebaseDataModule
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(modules = [AuthenticationDataModule::class, FirebaseDataModule::class])
interface AuthenticationDataComponent {
    fun userAuthenticationState(): UserAuthenticationState
    fun signInManager(): SignInManager
}

@Module
internal interface AuthenticationDataModule {
    @Binds
    fun userAuthStorage(state: FirebaseAuthenticationState): UserAuthenticationState

    @Binds
    fun signInManager(manager: FirebaseSignInManager): SignInManager
}

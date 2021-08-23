package akio.apps.myrun.wiring.data.authentication

import akio.apps.myrun.data.authentication.api.SignInManager
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.wiring.FirebaseDataModule
import dagger.Component

@Component(modules = [AuthenticationDataModule::class, FirebaseDataModule::class])
interface AuthenticationDataComponent {
    fun userAuthenticationState(): UserAuthenticationState
    fun signInManager(): SignInManager
}

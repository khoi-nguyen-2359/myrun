package akio.apps.myrun.data.authentication.wiring

import akio.apps.myrun.data.authentication.SignInManager
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.wiring.FirebaseDataModule
import dagger.Component

@Component(modules = [AuthenticationDataModule::class, FirebaseDataModule::class])
interface AuthenticationDataComponent {
    fun userAuthenticationState(): UserAuthenticationState
    fun signInManager(): SignInManager
}

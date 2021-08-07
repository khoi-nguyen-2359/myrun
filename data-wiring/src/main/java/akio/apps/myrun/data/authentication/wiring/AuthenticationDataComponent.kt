package akio.apps.myrun.data.authentication.wiring

import akio.apps.myrun.data._base.wiring.FirebaseDataModule
import akio.apps.myrun.data.authentication.SignInManager
import akio.apps.myrun.data.authentication.UserAuthenticationState
import dagger.Component

@Component(modules = [AuthenticationDataModule::class, FirebaseDataModule::class])
interface AuthenticationDataComponent {
    fun userAuthenticationState(): UserAuthenticationState
    fun signInManager(): SignInManager
}

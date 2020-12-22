package akio.apps.myrun.data.authentication

import akio.apps.myrun.data.authentication.impl.UserAuthenticationStateImpl
import dagger.Binds
import dagger.Module

@Module(includes = [AuthenticationDataModule.Bindings::class])
interface AuthenticationDataModule {

    @Module
    interface Bindings {
        @Binds
        fun userAuthStorage(state: UserAuthenticationStateImpl): UserAuthenticationState
    }

}
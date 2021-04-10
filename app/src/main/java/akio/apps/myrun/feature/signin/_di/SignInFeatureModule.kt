package akio.apps.myrun.feature.signin._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.signin.SignInViewModel
import akio.apps.myrun.feature.signin.impl.SignInViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface SignInFeatureModule {
    @Binds
    @IntoMap
    @ViewModelKey(SignInViewModel::class)
    fun signInViewModel(vm: SignInViewModelImpl): ViewModel
}

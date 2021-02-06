package akio.apps.myrun.feature.signin._di

import akio.apps._base.di.ViewModelFactoryModule
import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.signin.SignInViewModel
import akio.apps.myrun.feature.signin.impl.SignInActivity
import akio.apps.myrun.feature.signin.impl.SignInViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface SignInFeatureModule {
    @ContributesAndroidInjector(
        modules = [
            Bindings::class,
            ViewModelFactoryModule::class
        ]
    )
    fun signInActivity(): SignInActivity

    @Module
    interface Bindings {
        @Binds
        @IntoMap
        @ViewModelKey(SignInViewModel::class)
        fun signInViewModel(vm: SignInViewModelImpl): ViewModel
    }
}

package akio.apps.myrun.feature.splash._di

import akio.apps._base.di.ViewModelFactoryModule
import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.data.authentication._di.AuthenticationDataModule
import akio.apps.myrun.feature.splash.SplashViewModel
import akio.apps.myrun.feature.splash.impl.SplashActivity
import akio.apps.myrun.feature.splash.impl.SplashViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface SplashFeatureModule {
    @ContributesAndroidInjector(
        modules = [
            AuthenticationDataModule::class,
            ViewModelFactoryModule::class,
            Bindings::class
        ]
    )
    fun splashActivity(): SplashActivity

    @Module
    interface Bindings {
        @Binds
        @IntoMap
        @ViewModelKey(SplashViewModel::class)
        fun splashViewModel(viewModelImpl: SplashViewModelImpl): ViewModel
    }
}

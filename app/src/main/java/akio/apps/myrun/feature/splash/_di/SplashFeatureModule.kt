package akio.apps.myrun.feature.splash._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.splash.SplashViewModel
import akio.apps.myrun.feature.splash.impl.SplashViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface SplashFeatureModule {
    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    fun splashViewModel(viewModelImpl: SplashViewModelImpl): ViewModel
}

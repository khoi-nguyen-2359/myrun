package akio.apps.myrun.feature.splash.impl

import akio.apps.myrun._di.ViewModelKey
import akio.apps.myrun.feature.splash.SplashViewModel
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface SplashFeatureModule {

    @ContributesAndroidInjector
    fun splashActivity(): SplashActivity

    @Binds
    @IntoMap
    @ViewModelKey(SplashViewModel::class)
    fun splashViewModel(splashViewModelImpl: SplashViewModelImpl): ViewModel
}
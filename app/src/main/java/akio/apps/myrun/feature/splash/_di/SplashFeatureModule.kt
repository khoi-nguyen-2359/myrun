package akio.apps.myrun.feature.splash._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.splash.SignInAnonymousUsecase
import akio.apps.myrun.feature.splash.SignOutAnonymousUserUsecase
import akio.apps.myrun.feature.splash.SplashViewModel
import akio.apps.myrun.feature.splash.usecase.SignInAnonymousUsecaseImpl
import akio.apps.myrun.feature.splash.impl.SplashActivity
import akio.apps.myrun.feature.splash.impl.SplashViewModelImpl
import akio.apps.myrun.feature.splash.usecase.SignOutAnonymousUserUsecaseImpl
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

    @Binds
    fun signInAnonymousUsecase(usecaseImpl: SignInAnonymousUsecaseImpl): SignInAnonymousUsecase

    @Binds
    fun signOutAnonymousUsecase(usecaseImpl: SignOutAnonymousUserUsecaseImpl): SignOutAnonymousUserUsecase
}
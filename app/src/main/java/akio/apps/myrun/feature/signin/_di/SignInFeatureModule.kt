package akio.apps.myrun.feature.signin._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.signin.SignInViewModel
import akio.apps.myrun.feature.signin.SignInWithFacebookUsecase
import akio.apps.myrun.feature.signin.SignInWithGoogleUsecase
import akio.apps.myrun.feature.signin.impl.FirebaseSignInWithFacebookUsecase
import akio.apps.myrun.feature.signin.impl.SignInActivity
import akio.apps.myrun.feature.signin.impl.SignInViewModelImpl
import akio.apps.myrun.feature.signin.impl.SignInWithGoogleUsecaseImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(includes = [SignInFeatureModule.Bindings::class])
abstract class SignInFeatureModule {
    @ContributesAndroidInjector
    abstract fun signIn(): SignInActivity

    @Module
    interface Bindings {
        @Binds
        @IntoMap
        @ViewModelKey(SignInViewModel::class)
        fun signInViewModel(vm: SignInViewModelImpl): ViewModel

        @Binds
        fun signInFacebookUsecase(signInWithFacebookUsecase: FirebaseSignInWithFacebookUsecase): SignInWithFacebookUsecase

        @Binds
        fun signInWithGoogleUsecase(usecaseImpl: SignInWithGoogleUsecaseImpl): SignInWithGoogleUsecase
    }
}
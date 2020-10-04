package akio.apps.myrun.feature.signin

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.signin.impl.FirebaseSignInWithFacebookUsecase
import akio.apps.myrun.feature.signin.impl.SignInActivity
import akio.apps.myrun.feature.signin.impl.SignInViewModelImpl
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
    }
}
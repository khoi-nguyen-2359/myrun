package akio.apps.myrun.feature.signin._di

import akio.apps._base.di.ViewModelFactoryModule
import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.feature.signin.SignInViewModel
import akio.apps.myrun.feature.signin.SignInWithFacebookUsecase
import akio.apps.myrun.feature.signin.SignInWithGoogleUsecase
import akio.apps.myrun.feature.signin.impl.FirebaseLinkWithFacebookUsecase
import akio.apps.myrun.feature.signin.impl.FirebaseSignInWithFacebookUsecase
import akio.apps.myrun.feature.signin.impl.SignInActivity
import akio.apps.myrun.feature.signin.impl.SignInViewModelImpl
import akio.apps.myrun.feature.signin.impl.SignInWithGoogleUsecaseImpl
import akio.apps.myrun.feature.userprofile.LinkFacebookUsecase
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
interface SignInFeatureModule {
    @ContributesAndroidInjector(
        modules = [
            SignInDomainModule::class,
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

        @Binds
        fun signInFacebookUsecase(signInWithFacebookUsecase: FirebaseSignInWithFacebookUsecase): SignInWithFacebookUsecase

        @Binds
        fun signInWithGoogleUsecase(usecaseImpl: SignInWithGoogleUsecaseImpl): SignInWithGoogleUsecase

        @Binds
        fun linkFacebookUsecase(firebaseLinkWithFacebookUsecase: FirebaseLinkWithFacebookUsecase): LinkFacebookUsecase
    }
}

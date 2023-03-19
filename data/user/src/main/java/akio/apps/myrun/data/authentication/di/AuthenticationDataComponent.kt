package akio.apps.myrun.data.authentication.di

import akio.apps.myrun.base.firebase.FirebaseDataModule
import akio.apps.myrun.data.authentication.api.SignInManager
import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.user.api.AppMigrationState
import akio.apps.myrun.data.user.api.CurrentUserPreferences
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserPreferencesRepository
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.UserRecentActivityRepository
import android.app.Application
import com.squareup.anvil.annotations.MergeComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

abstract class AuthenticationDataScope private constructor()

@Singleton
@MergeComponent(
    scope = AuthenticationDataScope::class,
    modules = [FirebaseDataModule::class]
)
interface AuthenticationDataComponent {
    fun signInManager(): SignInManager
    fun userAuthState(): UserAuthenticationState
    fun userRecentPlace(): UserRecentActivityRepository
    fun userProfileRepository(): UserProfileRepository
    fun appMigrationState(): AppMigrationState
    fun userFollowRepository(): UserFollowRepository
    fun userPreferences(): CurrentUserPreferences
    fun userPreferencesRepo(): UserPreferencesRepository

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AuthenticationDataComponent
    }
}

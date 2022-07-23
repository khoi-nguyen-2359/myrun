package akio.apps.myrun.data.eapps.di

import akio.apps.myrun.base.firebase.FirebaseDataModule
import akio.apps.myrun.data.eapps.ExternalAppDataModule
import akio.apps.myrun.data.eapps.api.ExternalAppProvidersRepository
import akio.apps.myrun.data.eapps.api.StravaDataRepository
import akio.apps.myrun.data.eapps.api.StravaSyncState
import akio.apps.myrun.data.eapps.api.StravaTokenRepository
import android.app.Application
import com.squareup.anvil.annotations.MergeComponent
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

abstract class ExternalAppDataScope private constructor()

@Singleton
@MergeComponent(
    scope = ExternalAppDataScope::class,
    modules = [
        ExternalAppDataModule::class,
        FirebaseDataModule::class,
    ]
)
interface ExternalAppDataComponent {
    fun externalAppRepository(): ExternalAppProvidersRepository
    fun stravaDataRepository(): StravaDataRepository
    fun stravaSyncState(): StravaSyncState
    fun stravaTokenRepository(): StravaTokenRepository

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): ExternalAppDataComponent
    }
}

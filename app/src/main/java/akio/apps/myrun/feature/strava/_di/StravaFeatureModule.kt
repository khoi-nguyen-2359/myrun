package akio.apps.myrun.feature.strava._di

import akio.apps._base.di.ViewModelKey
import akio.apps.myrun.data._base.wiring.NetworkModule
import akio.apps.myrun.feature.strava.LinkStravaViewModel
import akio.apps.myrun.feature.strava.impl.LinkStravaViewModelImpl
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module(includes = [NetworkModule::class])
interface StravaFeatureModule {
    @Binds
    @IntoMap
    @ViewModelKey(LinkStravaViewModel::class)
    fun linkStravaViewModel(viewModelImpl: LinkStravaViewModelImpl): ViewModel
}

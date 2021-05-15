package akio.apps.myrun.data.localconfig._di

import akio.apps.myrun.data.localconfig.LocalConfiguration
import akio.apps.myrun.data.localconfig.impl.PreferencesLocalConfiguration
import dagger.Binds
import dagger.Module

@Module
interface LocalConfigurationDataModule {
    @Binds
    fun localConfiguration(localConfiguration: PreferencesLocalConfiguration): LocalConfiguration
}

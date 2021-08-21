package akio.apps.common.wiring

import akio.apps.common.feature.viewmodel.LaunchCatchingDelegate
import akio.apps.common.feature.viewmodel.LaunchCatchingDelegateImpl
import dagger.Module
import dagger.Provides

@Module
object LaunchCatchingModule {
    @Provides
    fun provideLaunchCatching(): LaunchCatchingDelegate = LaunchCatchingDelegateImpl()
}

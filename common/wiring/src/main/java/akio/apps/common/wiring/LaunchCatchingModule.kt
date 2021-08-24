package akio.apps.common.wiring

import akio.apps.common.data.LaunchCatchingDelegate
import akio.apps.common.data.LaunchCatchingDelegateImpl
import dagger.Module
import dagger.Provides

@Module
object LaunchCatchingModule {
    @Provides
    fun provideLaunchCatching(): LaunchCatchingDelegate = LaunchCatchingDelegateImpl()
}

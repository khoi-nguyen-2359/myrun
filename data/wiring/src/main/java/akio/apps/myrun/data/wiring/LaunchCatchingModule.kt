package akio.apps.myrun.data.wiring

import akio.apps.myrun.data.LaunchCatchingDelegate
import akio.apps.myrun.data.LaunchCatchingDelegateImpl
import dagger.Module
import dagger.Provides

@Module
object LaunchCatchingModule {
    @Provides
    fun provideLaunchCatching(): LaunchCatchingDelegate =
        LaunchCatchingDelegateImpl()
}

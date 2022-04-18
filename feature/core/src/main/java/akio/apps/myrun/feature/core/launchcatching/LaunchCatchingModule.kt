package akio.apps.myrun.feature.core.launchcatching

import dagger.Module
import dagger.Provides

@Module
object LaunchCatchingModule {
    @Provides
    @JvmStatic
    fun provideLaunchCatching(): LaunchCatchingDelegate = LaunchCatchingDelegateImpl()
}

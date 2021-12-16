package akio.apps.myrun.wiring.common

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
object DispatchersModule {
    @Provides
    @NamedIoDispatcher
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

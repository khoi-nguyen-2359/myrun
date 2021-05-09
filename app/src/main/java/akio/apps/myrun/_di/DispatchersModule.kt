package akio.apps.myrun._di

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

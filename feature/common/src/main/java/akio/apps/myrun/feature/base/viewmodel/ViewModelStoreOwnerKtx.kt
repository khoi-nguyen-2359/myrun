package akio.apps.myrun.feature.base.viewmodel

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> ViewModelStoreOwner.lazyViewModelProvider(
    noinline viewModelFactory: () -> T,
): Lazy<T> = lazy {
    val viewModelProvider = ViewModelProvider(
        this,
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModelFactory() as T
        }
    )
    viewModelProvider[T::class.java]
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> ViewModelStoreOwner.viewModelProvider(
    noinline viewModelFactory: () -> T,
): T {
    val viewModelProvider = ViewModelProvider(
        this,
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModelFactory() as T
        }
    )
    return viewModelProvider[T::class.java]
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> ViewModelStoreOwner.savedStateViewModelProvider(
    savedStateOwner: SavedStateRegistryOwner,
    noinline viewModelFactory: (SavedStateHandle) -> T,
): T {
    val viewModelProvider = ViewModelProvider(
        this,
        object : AbstractSavedStateViewModelFactory(savedStateOwner, null) {
            override fun <T : ViewModel?> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle,
            ): T = viewModelFactory(handle) as T
        }
    )
    return viewModelProvider[T::class.java]
}

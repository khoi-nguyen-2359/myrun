package akio.apps.common.feature.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> ViewModelStoreOwner.lazyViewModelProvider(
    noinline viewModelFactory: () -> T,
): Lazy<T> = lazy {
    val viewModelProvider = ViewModelProvider(
        this,
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = viewModelFactory() as T
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
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = viewModelFactory() as T
        }
    )
    return viewModelProvider[T::class.java]
}

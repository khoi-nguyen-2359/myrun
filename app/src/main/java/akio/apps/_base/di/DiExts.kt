package akio.apps._base.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

inline fun <reified T : ViewModel> ViewModelStoreOwner.viewModel(
    noinline viewModelFactoryProvider: () -> ViewModelFactoryProvider
): Lazy<T> = lazy {
    val viewModelFactory = viewModelFactoryProvider().viewModelFactory()
    val viewModelProvider = ViewModelProvider(this, viewModelFactory)
    viewModelProvider[T::class.java]
}

package akio.apps.myrun.feature.base.ktx

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.savedstate.SavedStateRegistryOwner

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> ViewModelStoreOwner.viewModelProvider(
    savedStateOwner: SavedStateRegistryOwner,
    crossinline viewModelFactory: (SavedStateHandle) -> T,
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

@Suppress("UNCHECKED_CAST")
inline fun <reified T : ViewModel> AppCompatActivity.lazyViewModelProvider(
    crossinline viewModelFactory: (SavedStateHandle) -> T,
): Lazy<T> = lazy { viewModelProvider(this, viewModelFactory) }

/**
 * Use [remember] to avoid re-creating view model provider during recomposition, even though the
 * provided view model is always a same instance.
 */
@Suppress("UNCHECKED_CAST")
@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.rememberViewModelProvider(
    crossinline viewModelFactory: @DisallowComposableCalls (SavedStateHandle) -> T,
): T = remember(this) {
    this.viewModelProvider(this, viewModelFactory)
}

package akio.apps.myrun._di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector

val Context.androidInjector: AndroidInjector<Any>
    get() = (this.applicationContext as HasAndroidInjector).androidInjector()

inline fun <reified T : ViewModel> getViewModel(
    viewModelFactory: ViewModelProvider.Factory,
    viewModelStoreOwner: ViewModelStoreOwner
): T {
    val viewModelProvider = ViewModelProvider(viewModelStoreOwner, viewModelFactory)
    return viewModelProvider[T::class.java]
}

inline fun <reified T : ViewModel> viewModel(
    viewModelFactory: ViewModelProvider.Factory,
    viewModelStoreOwner: ViewModelStoreOwner
): Lazy<T> = lazy {
    val viewModelProvider = ViewModelProvider(viewModelStoreOwner, viewModelFactory)
    viewModelProvider[T::class.java]
}

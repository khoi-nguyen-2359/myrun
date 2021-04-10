package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector

val Context.androidInjector: AndroidInjector<Any>
    get() = (this.applicationContext as HasAndroidInjector).androidInjector()

val Context.appComponent: AppComponent
    get() = (applicationContext as MyRunApp).appComponent

inline fun <reified T : ViewModel> getViewModel(
    viewModelFactory: ViewModelProvider.Factory,
    viewModelStoreOwner: ViewModelStoreOwner
): T {
    val viewModelProvider = ViewModelProvider(viewModelStoreOwner, viewModelFactory)
    return viewModelProvider[T::class.java]
}

inline fun <reified T : ViewModel> Fragment.viewModel(
    noinline viewModelCreator: () -> ViewModel
): Lazy<T> = viewModel(viewModelStoreOwner = this, viewModelCreator = viewModelCreator)

inline fun <reified T : ViewModel> AppCompatActivity.viewModel(
    noinline viewModelCreator: () -> ViewModel
): Lazy<T> = viewModel(viewModelStoreOwner = this, viewModelCreator = viewModelCreator)

inline fun <reified T : ViewModel> viewModel(
    viewModelStoreOwner: ViewModelStoreOwner,
    noinline viewModelCreator: () -> ViewModel,
): Lazy<T> = lazy {
    val viewModelFactory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T = viewModelCreator() as T
    }
    val viewModelProvider = ViewModelProvider(viewModelStoreOwner, viewModelFactory)
    viewModelProvider[T::class.java]
}

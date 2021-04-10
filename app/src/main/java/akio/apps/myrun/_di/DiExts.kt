package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import android.app.Activity
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

val Activity.appComponent: AppComponent
    get() = (applicationContext as MyRunApp).appComponent

inline fun <reified T : ViewModel> Fragment.viewModel(
    noinline viewModelCreator: (() -> ViewModel)? = null
): Lazy<T> = lazy {
    requireContext().getViewModel(this, viewModelCreator)
}

inline fun <reified T : ViewModel> AppCompatActivity.viewModel(
    noinline viewModelCreator: (() -> ViewModel)? = null
): Lazy<T> = lazy {
    getViewModel(viewModelStoreOwner = this, viewModelCreator = viewModelCreator)
}

inline fun <reified T : ViewModel> Context.getViewModel(
    viewModelStoreOwner: ViewModelStoreOwner,
    noinline viewModelCreator: (() -> ViewModel)? = null
): T {
    val viewModelFactory = if (viewModelCreator == null) {
        appComponent.viewModelFactory()
    } else {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T = viewModelCreator() as T
        }
    }
    val viewModelProvider = ViewModelProvider(viewModelStoreOwner, viewModelFactory)
    return viewModelProvider[T::class.java]
}

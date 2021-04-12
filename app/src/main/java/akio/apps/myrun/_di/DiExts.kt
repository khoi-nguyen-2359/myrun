package akio.apps.myrun._di

import akio.apps._base.di.ViewModelFactoryProvider
import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

//val Context.appComponent: AppComponent
//    get() = (applicationContext as AppComponent.Holder).getAppComponent()

val Application.appComponent: AppComponent
    get() = (this as AppComponent.Holder).getAppComponent()

val Fragment.appComponent: AppComponent
    get() = requireActivity().application.appComponent

inline fun <reified T : ViewModel> ViewModelStoreOwner.viewModel(
    noinline viewModelFactoryProvider: () -> ViewModelFactoryProvider
): Lazy<T> = lazy {
    val viewModelFactory = viewModelFactoryProvider().viewModelFactory()
    val viewModelProvider = ViewModelProvider(this, viewModelFactory)
    viewModelProvider[T::class.java]
}

package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import androidx.fragment.app.Fragment

val Fragment.appComponent
get() = (requireContext().applicationContext as MyRunApp).appComponent

fun Fragment.createViewModelInjectionDelegate(): ViewModelInjectionDelegate {
    return ViewModelInjectionDelegate(appComponent, this)
}
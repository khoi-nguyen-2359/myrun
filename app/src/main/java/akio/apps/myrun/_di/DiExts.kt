package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment

val Fragment.appComponent
get() = (requireContext().applicationContext as MyRunApp).appComponent

val Activity.appComponent
get() = (applicationContext as MyRunApp).appComponent

fun Fragment.createViewModelInjectionDelegate(): ViewModelInjectionDelegate {
    return ViewModelInjectionDelegate(appComponent, this)
}

fun ComponentActivity.createViewModelInjectionDelegate(): ViewModelInjectionDelegate {
    return ViewModelInjectionDelegate(appComponent, this)
}
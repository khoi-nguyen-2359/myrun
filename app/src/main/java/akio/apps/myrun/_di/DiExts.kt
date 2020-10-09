package akio.apps.myrun._di

import akio.apps.myrun.MyRunApp
import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import dagger.android.HasAndroidInjector

// TODO: refactor using androidInjector

val Context.androidInjector
get() = (this.applicationContext as? HasAndroidInjector)?.androidInjector()

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
package akio.apps.myrun._di

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector

val Context.androidInjector: AndroidInjector<Any>
    get() = (this.applicationContext as HasAndroidInjector).androidInjector()

val Fragment.androidInjector: AndroidInjector<Any>
    get() = requireContext().androidInjector

fun Fragment.createViewModelInjectionDelegate(): ViewModelInjectionDelegate {
    return ViewModelInjectionDelegate(androidInjector, this)
}

fun ComponentActivity.createViewModelInjectionDelegate(): ViewModelInjectionDelegate {
    return ViewModelInjectionDelegate(androidInjector, this)
}
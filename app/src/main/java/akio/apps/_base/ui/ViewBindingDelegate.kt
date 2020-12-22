package akio.apps._base.ui

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ViewBindingDelegate<T: ViewBinding>(
    private val creator: (View) -> T
) : ReadOnlyProperty<Fragment, T> {

    private var _value: T? = null
    val value: T
    get() = _value!!

    fun release() {
        _value = null
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        if (thisRef.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            throw IllegalStateException("get view binding value in wrong state")
        }

        if (_value == null) {
            _value = creator(thisRef.requireView())
        }

        return _value!!
    }
}
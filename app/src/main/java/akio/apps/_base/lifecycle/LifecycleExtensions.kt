package akio.apps._base.lifecycle

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.observe

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, block: (T) -> Unit) {
    liveData.observe(this, block)
}

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, observer: Observer<T>) {
    liveData.observe(this, observer)
}

fun <T> LifecycleOwner.observeEvent(liveData: LiveData<Event<T>>, block: (T) -> Unit) {
    liveData.observe(this, EventObserver { eventData -> block(eventData) })
}

package akio.apps.base.feature.lifecycle

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, block: (T) -> Unit) {
    liveData.observe(this, block)
}

fun <T> LifecycleOwner.observeEvent(liveData: LiveData<Event<T>>, block: (T) -> Unit) {
    liveData.observe(this, EventObserver { eventData -> block(eventData) })
}

val Fragment.viewLifecycleScope: CoroutineScope
    get() = viewLifecycleOwner.lifecycle.coroutineScope

fun <T> LifecycleOwner.collectRepeatOnStarted(flow: Flow<T>, action: suspend (T) -> Unit) =
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(action)
        }
    }

fun <T> LifecycleOwner.collectEventRepeatOnStarted(
    flow: Flow<Event<T>>,
    action: suspend (T) -> Unit
) = lifecycleScope.launch {
    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        flow.mapNotNull { it.getContentIfNotHandled() }
            .collect(action)
    }
}

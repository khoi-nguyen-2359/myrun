package akio.apps.common.feature.lifecycle

import akio.apps.common.data.Event
import androidx.lifecycle.Observer

class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getContentIfNotHandled()
            ?.let { value ->
                onEventUnhandledContent(value)
            }
    }
}

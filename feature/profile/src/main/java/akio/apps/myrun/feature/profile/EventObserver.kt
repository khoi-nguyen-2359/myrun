package akio.apps.myrun.feature.profile

import akio.apps.myrun.domain.launchcatching.Event
import androidx.lifecycle.Observer

class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getContentIfNotHandled()
            ?.let { value ->
                onEventUnhandledContent(value)
            }
    }
}

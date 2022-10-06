package akio.apps.myrun.feature.core.launchcatching

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

fun CoroutineScope.launchCatching(
    onError: ((Throwable) -> Unit)? = null,
    action: suspend CoroutineScope.() -> Unit,
) = launch {
    try {
        this.action()
    } catch (t: Throwable) {
        if (onError == null) {
            Timber.e(t)
        } else {
            onError(t)
        }
    }
}

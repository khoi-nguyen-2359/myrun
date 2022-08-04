package akio.apps.myrun.feature.core.launchcatching

import akio.apps.myrun.feature.core.Event
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

interface LaunchCatchingDelegate {
    val launchCatchingError: StateFlow<Event<Throwable>>
    val isLaunchCatchingInProgress: StateFlow<Boolean>
    fun setLaunchCatchingError(error: Throwable)
    fun setLaunchCatchingInProgress(value: Boolean)
    fun CoroutineScope.launchCatching(
        progressStateFlow: MutableStateFlow<Boolean>? = null,
        errorStateFlow: MutableStateFlow<Event<Throwable>>? = null,
        task: suspend CoroutineScope.() -> Unit,
    ): Job
}

class LaunchCatchingDelegateImpl @Inject constructor() : LaunchCatchingDelegate {
    private val _launchCatchingError: MutableStateFlow<Event<Throwable>> =
        MutableStateFlow(Event(null))
    override val launchCatchingError: StateFlow<Event<Throwable>> = _launchCatchingError

    private val _isLaunchCatchingInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isLaunchCatchingInProgress: StateFlow<Boolean> = _isLaunchCatchingInProgress

    override fun setLaunchCatchingError(error: Throwable) {
        _launchCatchingError.value = Event(error)
    }

    override fun setLaunchCatchingInProgress(value: Boolean) {
        _isLaunchCatchingInProgress.value = value
    }

    override fun CoroutineScope.launchCatching(
        progressStateFlow: MutableStateFlow<Boolean>?,
        errorStateFlow: MutableStateFlow<Event<Throwable>>?,
        task: suspend CoroutineScope.() -> Unit,
    ) = launch {
        val selectedProgressStateFlow = progressStateFlow
            ?: _isLaunchCatchingInProgress
        val selectedErrorStateFlow = errorStateFlow
            ?: _launchCatchingError
        try {
            selectedProgressStateFlow.value = true
            task()
        } catch (ex: Throwable) {
            Timber.e(ex)
            selectedErrorStateFlow.value = Event(ex)
        } finally {
            selectedProgressStateFlow.value = false
        }
    }
}

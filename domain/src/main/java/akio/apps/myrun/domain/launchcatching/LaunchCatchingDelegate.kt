package akio.apps.myrun.domain.launchcatching

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

interface LaunchCatchingDelegate {
    val launchCatchingError: StateFlow<Event<Exception>>
    val isLaunchCatchingInProgress: StateFlow<Boolean>
    fun CoroutineScope.launchCatching(
        progressStateFlow: MutableStateFlow<Boolean>? = null,
        errorStateFlow: MutableStateFlow<Event<Exception>>? = null,
        task: suspend CoroutineScope.() -> Unit,
    ): Job
}

class LaunchCatchingDelegateImpl @Inject constructor() : LaunchCatchingDelegate {
    private val _launchCatchingError: MutableStateFlow<Event<Exception>> =
        MutableStateFlow(Event(null))
    override val launchCatchingError: StateFlow<Event<Exception>> = _launchCatchingError

    private val _isLaunchCatchingInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isLaunchCatchingInProgress: StateFlow<Boolean> = _isLaunchCatchingInProgress

    override fun CoroutineScope.launchCatching(
        progressStateFlow: MutableStateFlow<Boolean>?,
        errorStateFlow: MutableStateFlow<Event<Exception>>?,
        task: suspend CoroutineScope.() -> Unit,
    ) = launch {
        val selectedProgressStateFlow = progressStateFlow
            ?: _isLaunchCatchingInProgress
        val selectedErrorStateFlow = errorStateFlow
            ?: _launchCatchingError
        try {
            selectedProgressStateFlow.value = true
            task()
        } catch (ex: Exception) {
            Timber.e(ex)
            selectedErrorStateFlow.value = Event(ex)
        } finally {
            selectedProgressStateFlow.value = false
        }
    }
}

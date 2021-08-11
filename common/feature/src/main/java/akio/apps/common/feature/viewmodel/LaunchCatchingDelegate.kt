package akio.apps.common.feature.viewmodel

import akio.apps.common.feature.lifecycle.Event
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

interface LaunchCatchingDelegate {
    val error: StateFlow<Event<Exception>>
    val isInProgress: StateFlow<Boolean>
    fun CoroutineScope.launchCatching(
        progressStateFlow: MutableStateFlow<Boolean>? = null,
        errorStateFlow: MutableStateFlow<Event<Exception>>? = null,
        task: suspend CoroutineScope.() -> Unit
    )
}

class LaunchCatchingDelegateImpl @Inject constructor() : LaunchCatchingDelegate {
    private val _error: MutableStateFlow<Event<Exception>> = MutableStateFlow(Event(null))
    override val error: StateFlow<Event<Exception>> = _error

    private val _isInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val isInProgress: StateFlow<Boolean> = _isInProgress

    override fun CoroutineScope.launchCatching(
        progressStateFlow: MutableStateFlow<Boolean>?,
        errorStateFlow: MutableStateFlow<Event<Exception>>?,
        task: suspend CoroutineScope.() -> Unit
    ) {
        launch {
            val selectedProgressStateFlow = progressStateFlow ?: _isInProgress
            val selectedErrorStateFlow = errorStateFlow ?: _error
            try {
                selectedProgressStateFlow.value = true
                task()
            } catch (ex: Exception) {
                Timber.d(ex)
                selectedErrorStateFlow.value = Event(ex)
            } finally {
                selectedProgressStateFlow.value = false
            }
        }
    }
}

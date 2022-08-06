package akio.apps.myrun.feature.core.launchcatching

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

interface LaunchCatchingDelegate {
    val launchCatchingError: Flow<Throwable?>
    val launchCatchingLoading: Flow<Boolean>
    fun setLaunchCatchingError(error: Throwable?)
    fun CoroutineScope.launchCatching(
        loadingStateFlow: MutableStateFlow<Boolean>? = null,
        errorStateFlow: MutableStateFlow<Throwable?>? = null,
        task: suspend CoroutineScope.() -> Unit,
    ): Job
}

class LaunchCatchingDelegateImpl @Inject constructor() : LaunchCatchingDelegate {
    private val _launchCatchingError: MutableStateFlow<Throwable?> =
        MutableStateFlow(null)
    override val launchCatchingError: Flow<Throwable?> = _launchCatchingError

    private val _launchCatchingLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override val launchCatchingLoading: Flow<Boolean> = _launchCatchingLoading

    override fun setLaunchCatchingError(error: Throwable?) {
        _launchCatchingError.value = error
    }

    override fun CoroutineScope.launchCatching(
        loadingStateFlow: MutableStateFlow<Boolean>?,
        errorStateFlow: MutableStateFlow<Throwable?>?,
        task: suspend CoroutineScope.() -> Unit,
    ) = launch {
        val selectedProgressStateFlow = loadingStateFlow
            ?: _launchCatchingLoading
        val selectedErrorStateFlow = errorStateFlow
            ?: _launchCatchingError
        try {
            selectedProgressStateFlow.value = true
            task()
        } catch (ex: Throwable) {
            Timber.e(ex)
            selectedErrorStateFlow.value = ex
        } finally {
            selectedProgressStateFlow.value = false
        }
    }
}

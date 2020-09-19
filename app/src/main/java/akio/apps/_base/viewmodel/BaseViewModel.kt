package akio.apps._base.viewmodel

import akio.apps._base.lifecycle.Event
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

open class BaseViewModel : ViewModel() {
    protected val _error = MutableLiveData<Event<Throwable>>()
    val error: LiveData<Event<Throwable>> = _error

    protected val _isInProgress = MutableLiveData<Boolean>()
    val isInProgress: LiveData<Boolean> = _isInProgress

    protected fun launchCatching(
        isInProgressLiveData: MutableLiveData<Boolean>? = _isInProgress,
        errorLiveData: MutableLiveData<Event<Throwable>>? = _error,
        task: suspend CoroutineScope.() -> Unit
    ): Job = viewModelScope.launch {
        try {
            isInProgressLiveData?.value = true
            task()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            errorLiveData?.value = Event(throwable)
        } finally {
            isInProgressLiveData?.value = false
        }
    }
}

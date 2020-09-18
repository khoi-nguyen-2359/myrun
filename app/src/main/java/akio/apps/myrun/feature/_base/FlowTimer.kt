package akio.apps.myrun.feature._base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

fun CoroutineScope.flowTimer(initialDelay: Long, period: Long, action: () -> Unit): Job = flow {
    if (initialDelay > 0) {
        delay(initialDelay)
    }
    while (true) {
        emit(Unit)
        delay(period)
    }
}
    //. this flow is on default dispatcher
    .onEach { action() }
    .launchIn(this)
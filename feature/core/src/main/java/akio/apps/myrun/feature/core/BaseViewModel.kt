package akio.apps.myrun.feature.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow

abstract class BaseViewModel {
    @Composable
    protected inline fun <reified T> rememberFlow(flow: Flow<T>, default: T): T {
        var value by rememberSaveable { mutableStateOf(default) }
        LaunchedEffect(key1 = true) {
            flow.collect { value = it }
        }
        return value
    }
}

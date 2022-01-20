package akio.apps.test

import kotlinx.coroutines.runBlocking
import org.mockito.Mockito.`when`
import org.mockito.stubbing.OngoingStubbing

fun <T, R> whenBlocking(r: R, block: suspend R.() -> T?): OngoingStubbing<T> = runBlocking {
    `when`(block(r))
}

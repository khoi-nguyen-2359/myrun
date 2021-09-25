package akio.apps.common.data.time

interface TimeProvider {
    fun currentTimeMillis(): Long
}

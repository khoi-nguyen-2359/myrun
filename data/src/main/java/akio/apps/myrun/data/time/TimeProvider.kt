package akio.apps.myrun.data.time

interface TimeProvider {
    fun currentMillisecond(): Long
}

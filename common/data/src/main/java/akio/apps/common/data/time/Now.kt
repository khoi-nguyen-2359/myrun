package akio.apps.common.data.time

class Now : TimeProvider {
    override fun currentMillisecond(): Long = System.currentTimeMillis()
}

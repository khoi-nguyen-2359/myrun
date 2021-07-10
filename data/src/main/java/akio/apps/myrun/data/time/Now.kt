package akio.apps.myrun.data.time

class Now : TimeProvider {
    override fun currentMillisecond(): Long = System.currentTimeMillis()
}

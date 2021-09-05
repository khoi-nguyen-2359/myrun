package akio.apps.common.data.time

import java.util.Calendar

class Now : TimeProvider {
    override fun currentMillisecond(): Long = Calendar.getInstance().timeInMillis
}

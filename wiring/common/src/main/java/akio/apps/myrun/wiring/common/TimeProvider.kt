package akio.apps.myrun.wiring.common

import java.util.Calendar
import javax.inject.Inject

open class TimeProvider @Inject constructor() {
    open fun currentTimeMillis(): Long = Calendar.getInstance().timeInMillis
    fun currentTimeMillisWithOffset(): Long = Calendar.getInstance().run {
        timeInMillis + timeZone.rawOffset
    }
    fun rawOffset(): Int = Calendar.getInstance().timeZone.rawOffset
}

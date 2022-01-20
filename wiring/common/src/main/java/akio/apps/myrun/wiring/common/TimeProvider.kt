package akio.apps.myrun.wiring.common

import java.util.Calendar
import javax.inject.Inject

open class TimeProvider @Inject constructor() {
    open fun currentTimeMillis(): Long = Calendar.getInstance().timeInMillis
}

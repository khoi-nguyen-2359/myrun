package akio.apps.myrun.wiring.common

import java.util.Calendar

object Now : TimeProvider() {
    override fun currentTimeMillis(): Long = Calendar.getInstance().timeInMillis
}

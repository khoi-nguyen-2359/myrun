package akio.apps.myrun.domain.time

import java.util.Calendar

object Now : TimeProvider() {
    override fun currentTimeMillis(): Long = Calendar.getInstance().timeInMillis
}

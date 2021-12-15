package akio.apps.myrun.data.time

import java.util.Calendar

object Now : TimeProvider {
    override fun currentTimeMillis(): Long = Calendar.getInstance().timeInMillis
}

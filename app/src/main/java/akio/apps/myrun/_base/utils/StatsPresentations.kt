package akio.apps.myrun._base.utils

import timber.log.Timber
import java.util.concurrent.TimeUnit

object StatsPresentations {
    fun getDisplayTrackingDistance(distance: Double): String =
        String.format("%.2f", distance / 1000)

    fun getDisplayDuration(duration: Long): String {
        val hour = duration / 3600000
        val min = (duration % 3600000) / 60000
        val sec = ((duration % 3600000) % 60000) / 1000
        return if (hour > 0) {
            String.format("%d:%02d:%02d", hour, min, sec)
        } else {
            String.format("%d:%02d", min, sec)
        }
    }

    /**
     * @param speed of unit meter/milisec
     */
    fun getDisplaySpeed(speed: Double): String {
        val speedInKmPerHour = speed * (TimeUnit.HOURS.toMillis(1) / 1000)
        return String.format("%.2f", speedInKmPerHour)
    }

    /**
     * @param speed of unit meter/milisec
     */
    fun getDisplayPace(speed: Double): String {
        var pace = if (speed == 0.0)
            0.0
        else
            1 / ((TimeUnit.MINUTES.toMillis(1) / 1000) * speed)

        // try to correct too large pace due to too small speed
        if (pace > 50) {
            Timber.e(Exception("Too large pace $pace. Corrected to zero."))
            pace = 0.0
        }
        return String.format("%d:%02d", pace.toInt(), ((pace % 1) * 60).toInt())
    }
}

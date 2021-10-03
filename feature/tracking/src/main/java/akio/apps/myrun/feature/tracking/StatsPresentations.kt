package akio.apps.myrun.feature.tracking

import akio.apps.myrun.domain.common.TrackingValueConverter

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
     * @param speed of unit meter/sec
     */
    fun getDisplaySpeed(speed: Double): String {
        return String.format("%.2f", TrackingValueConverter.SpeedKmPerHour.fromRawValue(speed))
    }

    /**
     * @param speed of unit meter/sec
     */
    fun getDisplayPace(speed: Double): String {
        val pace = if (speed == 0.0)
            0.0
        else
            1 / TrackingValueConverter.SpeedKmPerMinute.fromRawValue(speed)

        return String.format("%d:%02d", pace.toInt(), ((pace % 1) * 60).toInt())
    }
}

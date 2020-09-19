package akio.apps.myrun.feature._base

object StatsPresentation {
    fun getDisplayTrackingDistance(distance: Double): String = String.format("%.2f", distance / 1000)

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

    fun getDisplaySpeed(speed: Double): CharSequence? {
        return String.format("%.2f", speed)
    }
}
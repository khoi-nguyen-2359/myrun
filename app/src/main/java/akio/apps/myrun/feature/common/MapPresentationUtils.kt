package akio.apps.myrun.feature.common

object MapPresentationUtils {
    fun getDisplayTrackingDistance(distanceInMeters: Double): String = String.format("%.2f", distanceInMeters / 1000)

    fun getDisplayDuration(totalSec: Long): String {
        val hour = totalSec / 3600
        val min = (totalSec % 3600) / 60
        val sec = (totalSec % 3600) % 60
        return if (hour > 0) {
            String.format("%d:%02d:%02d", hour, min, sec)
        } else {
            String.format("%d:%02d", min, sec)
        }
    }

    fun getDisplaySpeed(speed: Double): CharSequence? {
        TODO("Not yet implemented")
    }
}
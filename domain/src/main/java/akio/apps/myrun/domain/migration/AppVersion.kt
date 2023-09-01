package akio.apps.myrun.domain.migration

open class AppVersion(private val appVersionInt: Int) {
    object V1050 : AppVersion(1_05_00)
    object V1060 : AppVersion(1_06_00)

    /**
     * Human-friendly app version string.
     */
    val appVersionString: String = run {
        val zz = appVersionInt % 100
        val yy = (appVersionInt / 100) % 100
        val xx = appVersionInt / 10000
        String.format("%d.%d.%d", xx, yy, zz)
    }

    operator fun compareTo(appVersion: AppVersion): Int =
        appVersionInt.compareTo(appVersion.appVersionInt)
}

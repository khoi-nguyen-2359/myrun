package akio.apps.myrun.data.eapps.api.model

enum class RunningApp(
    val id: String,
    val appName: String
) {
    Strava("strava", "Strava");
}

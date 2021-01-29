package akio.apps.myrun.data.externalapp.model

enum class RunningApp(
    val id: String,
    val appName: String
) {
    Strava("strava", "Strava");
}

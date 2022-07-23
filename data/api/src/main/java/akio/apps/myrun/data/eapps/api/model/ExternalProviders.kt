package akio.apps.myrun.data.eapps.api.model

class ExternalProviders(
    val strava: ProviderToken<ExternalAppToken.StravaToken>?,
) {

    fun toList(): List<ProviderToken<out ExternalAppToken>> = listOfNotNull(strava)

    companion object {
        fun createEmpty() = ExternalProviders(null)
    }
}

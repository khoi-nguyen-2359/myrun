package akio.apps.myrun.data.eapps.api.model

class ProviderToken<T : ExternalAppToken>(
    val runningApp: RunningApp,
    val token: T,
)

package akio.apps.myrun.data.externalapp.model

class ProviderToken<T : ExternalAppToken>(
    val runningApp: RunningApp,
    val token: T
)

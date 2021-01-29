package akio.apps.myrun.data.externalapp.entity

import com.google.firebase.firestore.PropertyName

data class FirestoreProvidersEntity(
    @JvmField @PropertyName("strava")
    val strava: FirestoreProviderToken<FirestoreStravaToken>? = null
) {

    data class FirestoreProviderToken<T>(
        @JvmField @PropertyName("name")
        val name: String = "",

        @JvmField @PropertyName("token")
        val token: T? = null
    )

    data class FirestoreStravaToken(
        @JvmField @PropertyName("accessToken")
        val accessToken: String = "",

        @JvmField @PropertyName("refreshToken")
        val refreshToken: String = "",

        @JvmField @PropertyName("athlete")
        val athlete: FirestoreStravaAthlete = FirestoreStravaAthlete(
            0
        )
    )

    data class FirestoreStravaAthlete(
        @JvmField @PropertyName("id")
        val id: Long = 0
    )
}

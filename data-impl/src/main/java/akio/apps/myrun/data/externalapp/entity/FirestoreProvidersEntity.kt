package akio.apps.myrun.data.externalapp.entity

import com.google.firebase.firestore.PropertyName

data class FirestoreProvidersEntity(
    @PropertyName("strava")
    val strava: FirestoreProviderToken<FirestoreStravaToken>? = null
) {

    data class FirestoreProviderToken<T>(
        @PropertyName("name")
        val name: String = "",

        @PropertyName("token")
        val token: T? = null
    )

    data class FirestoreStravaToken(
        @PropertyName("accessToken")
        val accessToken: String = "",

        @PropertyName("refreshToken")
        val refreshToken: String = "",

        @PropertyName("athlete")
        val athlete: FirestoreStravaAthlete = FirestoreStravaAthlete(
            0
        )
    )

    data class FirestoreStravaAthlete(
        @PropertyName("id")
        val id: Long = 0
    )
}

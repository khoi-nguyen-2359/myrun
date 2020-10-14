package akio.apps.myrun.data.externalapp.entity

data class FirestoreProvidersEntity(
	val strava: FirestoreProviderToken<FirestoreStravaToken>? = null
) {

	data class FirestoreProviderToken<T>(
		val name: String = "",

		val token: T? = null
	)

	data class FirestoreStravaToken(
		val access_token: String = "",
		val refresh_token: String = "",
		val athlete: FirestoreStravaAthlete = FirestoreStravaAthlete(
			0
		)
	)

	data class FirestoreStravaAthlete(
		val id: Long = 0
	)
}
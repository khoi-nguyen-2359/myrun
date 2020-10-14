package akio.apps.myrun.feature.externalapp.impl

import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp.entity.StravaTokenRefreshEntity
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.feature.userprofile.RemoveStravaTokenUsecase
import akio.apps.myrun.feature.editprofile.UpdateStravaTokenUsecase
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber

class StravaAuthenticator(
	private val httpClient: OkHttpClient,
	private val updateStravaTokenUsecase: UpdateStravaTokenUsecase,
	private val removeStravaTokenUsecase: RemoveStravaTokenUsecase,
	private val stravaTokenStorage: StravaTokenStorage,
	private val baseStravaUrl: String,
	private val gson: Gson,
	private val clientId: String,
	private val clientSecret: String
): Authenticator {

	override fun authenticate(route: Route?, response: Response): Request? {
		if (response.code != 401) {
			return null
		}

		val originalToken = stravaTokenStorage.getToken()
		val originalRequest = response.request
		val originalAccessToken = originalRequest.header("Authorization")?.removePrefix("Bearer ")
		if (originalToken == null || originalAccessToken == null) {
			return null
		}

		if (originalAccessToken != originalToken.accessToken) {
			return originalRequest.newBuilder()
				.addHeader("Authorization", "Bearer ${originalToken.accessToken}")
				.build()
		}

		Timber.d( "refresh Strava token original=$originalAccessToken refresh=${originalToken.refreshToken}")

		val refreshRequest = Request.Builder()
			.method("POST", "".toRequestBody("text/plain".toMediaType()))
			.url(baseStravaUrl + "oauth/token?grant_type=refresh_token" +
					"&client_id=$clientId" +
					"&client_secret=$clientSecret" +
					"&refresh_token=${originalToken.refreshToken}")
			.build()

		val refreshResponse = httpClient.newCall(refreshRequest).execute()
		if (refreshResponse.isSuccessful && refreshResponse.code == 200) {
			val stringResponse = refreshResponse.body?.string()
			val refreshToken = gson.fromJson(stringResponse, StravaTokenRefreshEntity::class.java)
			val newAccessToken = refreshToken.accessToken
			val newToken =
				ExternalAppToken.StravaToken(
					refreshToken,
					originalToken.athlete
				)
			updateStravaTokenUsecase.updateStravaToken(newToken)

			Timber.d("refresh Strava token request succeed")
			return response.request
				.newBuilder()
				.header("Authorization", "Bearer $newAccessToken")
				.build()
		}

		Timber.d("refresh Strava token request failed")
		removeStravaTokenUsecase.removeStravaTokenUsecase()

		return null
	}
}
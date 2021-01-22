package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun.data.externalapp.StravaAuthenticator
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp.entity.StravaTokenRefreshEntity
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.feature.editprofile.UpdateStravaTokenUsecase
import akio.apps.myrun.feature.userprofile.RemoveStravaTokenUsecase
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

class StravaAuthenticatorImpl(
    private val httpClient: OkHttpClient,
    private val updateStravaTokenUsecase: UpdateStravaTokenUsecase,
    private val removeStravaTokenUsecase: RemoveStravaTokenUsecase,
    private val stravaTokenStorage: StravaTokenStorage,
    private val baseStravaUrl: String,
    private val gson: Gson,
    private val clientId: String,
    private val clientSecret: String
) : StravaAuthenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) {
            return null
        }

        val originalToken = runBlocking { stravaTokenStorage.getToken() }
        val originalRequest = response.request
        val originalAccessToken = originalRequest.header("Authorization")
            ?.removePrefix("Bearer ")
        if (originalToken == null || originalAccessToken == null) {
            return null
        }

        if (originalAccessToken != originalToken.accessToken) {
            return originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer ${originalToken.accessToken}")
                .build()
        }

        val originalRefreshToken = originalToken.refreshToken
        Timber.d("refresh Strava token access_token=$originalAccessToken refresh_token=$originalRefreshToken")

        val refreshRequest = Request.Builder()
            .method("POST", "".toRequestBody("text/plain".toMediaType()))
            .url(
                baseStravaUrl + "oauth/token?grant_type=refresh_token" +
                    "&client_id=$clientId" +
                    "&client_secret=$clientSecret" +
                    "&refresh_token=$originalRefreshToken"
            )
            .build()

        val refreshResponse = httpClient.newCall(refreshRequest)
            .execute()
        if (refreshResponse.isSuccessful && refreshResponse.code == 200) {
            val stringResponse = refreshResponse.body?.string()
            val refreshToken = gson.fromJson(stringResponse, StravaTokenRefreshEntity::class.java)
            val newAccessToken = refreshToken.accessToken
            val newToken = ExternalAppToken.StravaToken(refreshToken, originalToken.athlete)

            runBlocking { updateStravaTokenUsecase.updateStravaToken(newToken) }

            Timber.d("refresh Strava token request succeed")
            return response.request
                .newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        }

        Timber.e("refresh Strava token failed. code=${refreshResponse.code}, access_token=$originalAccessToken, refresh_token=$originalRefreshToken")
        runBlocking {
            removeStravaTokenUsecase.removeStravaToken()
        }

        return null
    }
}

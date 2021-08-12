package akio.apps.myrun.data.externalapp.impl

import akio.apps.myrun.data.authentication.api.UserAuthenticationState
import akio.apps.myrun.data.externalapp.ExternalAppProvidersRepository
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.StravaStravaTokenRefresh
import akio.apps.myrun.data.externalapp.model.StravaTokenRefreshMapper
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber

class StravaAuthenticator(
    private val httpClient: OkHttpClient,
    private val externalAppProvidersRepository: ExternalAppProvidersRepository,
    private val stravaTokenRefreshMapper: StravaTokenRefreshMapper,
    private val userAuthenticationState: UserAuthenticationState,
    private val baseStravaUrl: String,
    private val gson: Gson,
    private val clientId: String,
    private val clientSecret: String
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val userAccountId = userAuthenticationState.getUserAccountId()
        if (response.code != 401 || userAccountId == null) {
            return null
        }

        val originalToken = runBlocking {
            externalAppProvidersRepository.getStravaProviderToken(userAccountId)
        }
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
        Timber.d(
            "refresh Strava access_token=$originalAccessToken refresh_token=$originalRefreshToken"
        )

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
            val tokenRefreshEntity =
                gson.fromJson(stringResponse, StravaStravaTokenRefresh::class.java)
            val newAccessToken = tokenRefreshEntity.accessToken
            val refreshToken = stravaTokenRefreshMapper.map(tokenRefreshEntity)
            val newToken = ExternalAppToken.StravaToken(refreshToken, originalToken.athlete)

            runBlocking {
                externalAppProvidersRepository.updateStravaProvider(userAccountId, newToken)
            }

            Timber.d("refresh Strava token request succeed")
            return response.request
                .newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        }

        Timber.e(
            Exception(
                "refresh Strava token failed. code=${refreshResponse.code}, " +
                    "access_token=$originalAccessToken, " +
                    "refresh_token=$originalRefreshToken"
            )
        )
        runBlocking { externalAppProvidersRepository.removeStravaProvider(userAccountId) }

        return null
    }
}

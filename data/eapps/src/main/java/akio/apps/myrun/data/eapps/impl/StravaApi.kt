package akio.apps.myrun.data.eapps.impl

import akio.apps.myrun.data.eapps.impl.model.StravaStravaRoute
import akio.apps.myrun.data.eapps.impl.model.StravaStravaToken
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface StravaApi {
    @POST("oauth/deauthorize")
    suspend fun deauthorize(
        @Header("Authorization") bearer: String,
        @Query("access_token") accessToken: String,
    ): Unit

    @POST("oauth/token?grant_type=authorization_code")
    suspend fun exchangeToken(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("code") code: String,
    ): StravaStravaToken

    @POST("api/v3/uploads")
    @Multipart
    suspend fun uploadActivity(
        @Header("Authorization") bearer: String,
        @Part file: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("data_type") dataType: RequestBody,
    ): Unit

    @GET("api/v3/athletes/{id}/routes")
    suspend fun getAthleteRoutes(
        @Header("Authorization") bearer: String,
        @Path("id") athleteId: Long,
    ): List<StravaStravaRoute>
}

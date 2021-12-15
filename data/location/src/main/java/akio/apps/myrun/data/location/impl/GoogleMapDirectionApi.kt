package akio.apps.myrun.data.location.impl

import akio.apps.myrun.data.location.impl.model.GoogleDirectionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleMapDirectionApi {
    @GET("maps/api/directions/json?mode=walking")
    suspend fun getWalkingDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("waypoints") waypoints: String,
        @Query("key") apiKey: String
    ): GoogleDirectionResponse
}

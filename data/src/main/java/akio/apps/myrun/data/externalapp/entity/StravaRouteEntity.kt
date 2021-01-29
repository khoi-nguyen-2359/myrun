package akio.apps.myrun.data.externalapp.entity

import com.google.gson.annotations.SerializedName

class StravaRouteEntity(
    val map: StravaRouteMapEntity?,
    val name: String,
    val segments: List<StravaSegmentEntity>?
) {
    fun getEncodedPolyline() = map?.polyline ?: map?.summaryPolyline
    fun getCountry() = segments?.firstOrNull()?.country
    fun getCity() = segments?.firstOrNull()?.city
}

class StravaRouteMapEntity(
    val polyline: String?,

    @SerializedName("summary_polyline")
    val summaryPolyline: String?
)

class StravaSegmentEntity(
    val country: String?,
    val city: String?
)

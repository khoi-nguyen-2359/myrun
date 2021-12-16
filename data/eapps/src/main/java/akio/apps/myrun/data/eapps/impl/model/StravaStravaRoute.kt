package akio.apps.myrun.data.eapps.impl.model

import com.google.gson.annotations.SerializedName

class StravaStravaRoute(
    val map: StravaStravaRouteMap?,
    val name: String,
    val segments: List<StravaStravaSegment>?
) {
    fun getEncodedPolyline() = map?.polyline ?: map?.summaryPolyline
    fun getCountry() = segments?.firstOrNull()?.country
    fun getCity() = segments?.firstOrNull()?.city
}

class StravaStravaRouteMap(
    val polyline: String?,

    @SerializedName("summary_polyline")
    val summaryPolyline: String?
)

class StravaStravaSegment(
    val country: String?,
    val city: String?
)

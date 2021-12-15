package akio.apps.myrun.data.location.impl.model

import com.google.gson.annotations.SerializedName

class GoogleDirectionResponse(
    @SerializedName("routes")
    val routes: List<RouteEntity>,

    @SerializedName("status")
    val status: MapApiStatus,

    @SerializedName("geocoded_waypoints")
    val geocodedWaypoints: List<GeocodedWaypointEntity>,
)

class RouteEntity(
    @SerializedName("legs")
    val legs: List<LegEntity>,

    @SerializedName("overview_polyline")
    val overviewPolyline: PolylineEntity,
)

class LegEntity(
    @SerializedName("start_address")
    val startAddress: String,

    @SerializedName("steps")
    val steps: List<StepEntity>,
)

class StepEntity(
    @SerializedName("start_location")
    val startLocation: DirectionLatLngEntity,

    @SerializedName("end_location")
    val endLocation: DirectionLatLngEntity,

    @SerializedName("polyline")
    val polyline: PolylineEntity,
)

class GeocodedWaypointEntity(
    @SerializedName("place_id")
    val placeId: String,

    @SerializedName("geocoder_status")
    val geocoderStatus: MapApiStatus,
)

class PolylineEntity(
    @SerializedName("points")
    val points: String,
)

class DirectionLatLngEntity(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double,
)

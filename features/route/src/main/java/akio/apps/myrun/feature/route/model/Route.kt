package akio.apps.myrun.feature.route.model

import akio.apps.myrun.data.location.api.model.LatLng
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import kotlinx.parcelize.Parcelize

@Parcelize
class Route(
    var id: String = "",
    var routeName: String = "",
    var waypoints: List<LatLng> = listOf(),
    var placeId: String? = null,
    var placeName: String = "",
    var checkpoints: List<Checkpoint> = listOf(),
    var country: String = "",
    val thumbnail: String? = null,
) : Parcelable {

    fun getLength() = SphericalUtil.computeLength(
        waypoints.map {
            com.google.android.gms.maps.model.LatLng(
                it.latitude,
                it.longitude
            )
        }
    )

    fun getBoundary(): LatLngBounds.Builder =
        waypoints.map {
            com.google.android.gms.maps.model.LatLng(
                it.latitude,
                it.longitude
            )
        }.foldRight(LatLngBounds.builder(), { curr, acc -> acc.include(curr) })

    fun getStartPoint(): LatLng {
        return waypoints.firstOrNull()
            ?: throw Exception("Can not get start point of an empty route")
    }

    override fun toString(): String {
        return routeName
    }

    fun getEndPoint(): LatLng {
        return waypoints.lastOrNull() ?: throw Exception("Can not get end point of an empty route")
    }
//
//    fun isNearLocation(location: LatLng) = PolyUtil.isLocationOnPath(location,
//        waypoints.toGoogleLatLngList(),
//        false,
//        MapHelper.NEAR_ROUTE_DISTANCE.toDouble())
}

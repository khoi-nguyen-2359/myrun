package akio.apps.myrun.feature.route.model

import akio.apps.myrun.data.location.api.model.LatLng
import com.mapbox.geojson.Point

fun LatLng.toPoint(): Point = Point.fromLngLat(longitude, latitude)
fun Point.toLatLng(): LatLng = LatLng(latitude(), longitude())

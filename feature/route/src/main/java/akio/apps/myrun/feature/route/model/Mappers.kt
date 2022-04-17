package akio.apps.myrun.feature.route.model

import akio.apps.myrun.data.location.api.model.LatLng
import com.mapbox.geojson.Point

internal fun LatLng.toPoint(): Point = Point.fromLngLat(longitude, latitude)
internal fun Point.toLatLng(): LatLng = LatLng(latitude(), longitude())

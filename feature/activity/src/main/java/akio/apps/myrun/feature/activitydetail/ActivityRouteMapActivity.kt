package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.base.BitmapUtils.createDrawableBitmap
import akio.apps.myrun.feature.base.ktx.dp2px
import akio.apps.myrun.feature.base.ktx.getColorCompat
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.PolyUtil
import kotlin.coroutines.resume
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class ActivityRouteMapActivity : AppCompatActivity(R.layout.activity_activity_route_map) {
    private lateinit var map: GoogleMap

    private val encodedPolyline: String by lazy {
        intent.getStringExtra(EXT_ENCODED_POLYLINE) ?: ""
    }

    private val decodedPolyline: List<LatLng> by lazy { PolyUtil.decode(encodedPolyline) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (decodedPolyline.isEmpty()) {
            finish()
            return
        }
        lifecycleScope.launch {
            initMap()
            drawRoutePolylineAndZoomToBounds()
            drawStartStopMarker()
        }
    }

    private suspend fun initMap() {
        map = suspendCancellableCoroutine { continuation ->
            (supportFragmentManager.findFragmentById(R.id.activity_map_view) as? SupportMapFragment)
                ?.getMapAsync { googleMap ->
                    runOnUiThread {
                        continuation.resume(googleMap)
                    }
                }
        }
        map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(this, R.raw.google_map_styles)
        )
        with(map.uiSettings) {
            isMyLocationButtonEnabled = false
            isCompassEnabled = false
            isZoomControlsEnabled = false
            isIndoorLevelPickerEnabled = false
            isMapToolbarEnabled = false
        }
    }

    private fun drawStartStopMarker() {
        val startMarkerBitmap = createDrawableBitmap(
            context = this,
            drawableResId = R.drawable.ic_start_marker
        )
        if (startMarkerBitmap != null) {
            val startMarker = MarkerOptions()
                .position(decodedPolyline[0])
                .icon(BitmapDescriptorFactory.fromBitmap(startMarkerBitmap))
                .anchor(0.5f, 0.5f)
            map.addMarker(startMarker)
        }

        val stopMarkerBitmap = createDrawableBitmap(
            context = this,
            drawableResId = R.drawable.ic_stop_marker
        )
        if (stopMarkerBitmap != null) {
            val stopMarker = MarkerOptions()
                .position(decodedPolyline.last())
                .icon(BitmapDescriptorFactory.fromBitmap(stopMarkerBitmap))
                .anchor(0.5f, 0.5f)
            map.addMarker(stopMarker)
        }
    }

    private fun drawRoutePolylineAndZoomToBounds() {
        val mapPolyline = PolylineOptions().addAll(decodedPolyline)
            .jointType(JointType.ROUND)
            .startCap(RoundCap())
            .endCap(RoundCap())
            .color(getColorCompat(R.color.route_tracking_polyline))
            .width(3.dp2px)
        map.addPolyline(mapPolyline)

        val routeBoundsBuilder = LatLngBounds.builder()
        decodedPolyline.forEach { routeBoundsBuilder.include(it) }
        val cameraUpdate =
            CameraUpdateFactory.newLatLngBounds(
                routeBoundsBuilder.build(),
                application.resources.displayMetrics.widthPixels,
                application.resources.displayMetrics.heightPixels,
                MAP_LATLNG_BOUND_PADDING
            )
        map.moveCamera(cameraUpdate)
    }

    companion object {
        private const val EXT_ENCODED_POLYLINE = "EXT_ENCODED_POLYLINE"

        private val MAP_LATLNG_BOUND_PADDING = 30.dp2px.toInt()

        fun createLaunchIntent(context: Context, encodedPolyline: String): Intent =
            Intent(context, ActivityRouteMapActivity::class.java)
                .putExtra(EXT_ENCODED_POLYLINE, encodedPolyline)
    }
}

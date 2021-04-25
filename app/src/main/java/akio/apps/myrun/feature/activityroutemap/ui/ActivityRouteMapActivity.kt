package akio.apps.myrun.feature.activityroutemap.ui

import akio.apps._base.ktext.getColorCompat
import akio.apps._base.ktext.getDrawableCompat
import akio.apps._base.ui.dp2px
import akio.apps.myrun.R
import akio.apps.myrun.databinding.ActivityActivityRouteMapBinding
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.JointType
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.LatLngBounds
import com.google.android.libraries.maps.model.MapStyleOptions
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.android.libraries.maps.model.PolylineOptions
import com.google.android.libraries.maps.model.RoundCap
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ActivityRouteMapActivity : AppCompatActivity() {
    val viewBinding: ActivityActivityRouteMapBinding by lazy {
        ActivityActivityRouteMapBinding.inflate(layoutInflater)
    }

    private lateinit var map: GoogleMap

    private val encodedPolyline: String by lazy {
        intent.getStringExtra(EXT_ENCODED_POLYLINE)
            ?: throw Exception("Missing data to display activity route")
    }

    private val decodedPolyline: List<LatLng> by lazy { PolyUtil.decode(encodedPolyline) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        lifecycleScope.launch {
            initMap()
            drawRoutePolylineAndZoomToBounds()
            drawStartStopMarker()
        }
    }

    private suspend fun initMap() {
        map = suspendCancellableCoroutine { continuation ->
            (supportFragmentManager.findFragmentById(R.id.activity_map_view) as? SupportMapFragment)
                ?.getMapAsync(continuation::resume)
        }
        map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this,
                R.raw.google_map_styles
            )
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
            drawableResId = R.drawable.ic_play_circle,
            tintColorResId = R.color.activity_route_map_start_marker_tint
        )
        val startMarker = MarkerOptions()
            .position(decodedPolyline[0])
            .icon(BitmapDescriptorFactory.fromBitmap(startMarkerBitmap))
        map.addMarker(startMarker)

        val stopMarkerBitmap = createDrawableBitmap(
            context = this,
            drawableResId = R.drawable.ic_stop_circle,
            tintColorResId = R.color.activity_route_map_stop_marker_tint
        )
        val stopMarker = MarkerOptions()
            .position(decodedPolyline.last())
            .icon(BitmapDescriptorFactory.fromBitmap(stopMarkerBitmap))
        map.addMarker(stopMarker)
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

        private fun createDrawableBitmap(
            context: Context,
            @DrawableRes drawableResId: Int,
            @ColorRes tintColorResId: Int
        ): Bitmap? {
            val drawable = context.getDrawableCompat(drawableResId) ?: return null
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            DrawableCompat.setTint(drawable, context.getColorCompat(tintColorResId))
            val canvas = Canvas(bitmap)
            drawable.draw(canvas)
            return bitmap
        }
    }
}

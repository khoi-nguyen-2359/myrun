package akio.apps.myrun.feature.activityroutemap.ui

import akio.apps._base.ktext.getColorCompat
import akio.apps._base.ui.dp2px
import akio.apps.myrun.R
import akio.apps.myrun.databinding.ActivityActivityRouteMapBinding
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initMapView()
    }

    private fun initMapView() = lifecycleScope.launch {
        map = suspendCancellableCoroutine { continuation ->
            (supportFragmentManager.findFragmentById(R.id.route_map_view) as? SupportMapFragment)
                ?.getMapAsync(continuation::resume)
        }
        val listLatLngs = PolyUtil.decode(encodedPolyline)
        val mapPolyline = PolylineOptions().addAll(listLatLngs)
            .jointType(JointType.ROUND)
            .startCap(RoundCap())
            .endCap(RoundCap())
            .color(getColorCompat(R.color.route_tracking_polyline))
            .width(3.dp2px)
        map.addPolyline(mapPolyline)
    }

    companion object {
        private const val EXT_ENCODED_POLYLINE = "EXT_ENCODED_POLYLINE"

        fun createLaunchIntent(context: Context, encodedPolyline: String): Intent =
            Intent(context, ActivityRouteMapActivity::class.java)
                .putExtra(EXT_ENCODED_POLYLINE, encodedPolyline)
    }
}

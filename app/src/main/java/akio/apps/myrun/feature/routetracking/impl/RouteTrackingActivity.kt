package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.activity.BaseInjectionActivity
import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps._base.view.dp2px
import akio.apps.myrun.R
import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import akio.apps.myrun.databinding.ActivityRouteTrackingBinding
import akio.apps.myrun.feature._base.*
import akio.apps.myrun.feature._base.AppPermissions.locationPermissions
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.feature.routetracking.model.RouteTrackingStats
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch

class RouteTrackingActivity : BaseInjectionActivity() {

    private val dialogDelegate by lazy { ActivityDialogDelegate(this) }

    private val viewBinding by lazy { ActivityRouteTrackingBinding.inflate(layoutInflater) }

    private val viewModel: RouteTrackingViewModel by lazy { getViewModel() }

    private lateinit var mapView: GoogleMap

    private val checkLocationServiceDelegate by lazy {
        CheckLocationServiceDelegate(
            this,
            listOf(RouteTrackingService.createLocationTrackingRequest()),
            RC_LOCATION_SERVICE,
            onLocationServiceAvailable
        )
    }

    private val checkRequiredPermissionsDelegate by lazy { CheckRequiredPermissionsDelegate(this, RC_LOCATION_PERMISSIONS, locationPermissions, onLocationPermissionsGranted) }

    private var routePolyline: Polyline? = null
    private var drawnLocationCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initObservers()
    }

    override fun onStart() {
        super.onStart()

        // onStart: check location permissions -> check location service availability -> allow user to use this screen
        checkRequiredPermissionsDelegate.requestPermissions()
        viewModel.resumeTrackingStatsUpdates()
    }

    override fun onStop() {
        super.onStop()

        viewModel.stopTrackingStatsUpdates()
    }

    private fun initObservers() {
        observeEvent(viewModel.mapInitialLocation) { initLocation ->
            mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(initLocation.toGmsLatLng(), MapPresentation.MAP_DEFAULT_ZOOM_LEVEL))
        }

        observe(viewModel.isInProgress, dialogDelegate::toggleProgressDialog)
        observeEvent(viewModel.error, dialogDelegate::showExceptionAlert)

        observe(viewModel.trackingLocationBatch, ::onTrackingLocationUpdated)

        observe(viewModel.trackingStats, ::updateTrackingStatsView)
    }

    private fun updateTrackingStatsView(routeTrackingStats: RouteTrackingStats) {
        viewBinding.trackingStatsView.update(routeTrackingStats)
    }

    private fun onTrackingLocationUpdated(batch: List<TrackingLocationEntity>) {
        drawTrackingLocations(batch)
        batch.lastOrNull()?.let {
            mapView.moveCamera(CameraUpdateFactory.newLatLng(GmsLatLng(it.latitude, it.longitude)))
        }
        viewModel.requestRouteTrackingLocationUpdate(drawnLocationCount)
    }

    private fun drawTrackingLocations(batch: List<TrackingLocationEntity>) {
        routePolyline?.let { currentPolyline ->
            val appendedPolypoints = currentPolyline.points
            appendedPolypoints.addAll(batch.map { LatLng(it.latitude, it.longitude) })
            currentPolyline.points = appendedPolypoints
        } ?: run {
            val polyline = PolylineOptions()
                .addAll(batch.map { LatLng(it.latitude, it.longitude) })
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
                .color(ContextCompat.getColor(this, R.color.route_tracking_polyline))
                .width(3.dp2px)
            routePolyline = mapView.addPolyline(polyline)
        }

        drawnLocationCount += batch.size
    }

    private fun initViews() {
        viewBinding.apply {
            setContentView(root)
            recordButton.root.setOnClickListener { onStartRouteTracking() }
            pauseButton.root.setOnClickListener { onPauseRouteTracking() }
            resumeButton.root.setOnClickListener { onResumeRouteTracking() }
            stopButton.root.setOnClickListener { onStopRouteTracking() }
        }
    }

    private fun onResumeRouteTracking() {
        viewBinding.apply {
            recordButton.root.visibility = View.GONE
            resumeButton.root.visibility = View.GONE
            stopButton.root.visibility = View.GONE
            pauseButton.root.visibility = View.VISIBLE
        }

        startService(RouteTrackingService.resumeIntent(this))
        viewModel.startTrackingStatsUpdates()
    }

    private fun onStopRouteTracking() {
        viewBinding.apply {
            recordButton.root.visibility = View.VISIBLE
            resumeButton.root.visibility = View.GONE
            stopButton.root.visibility = View.GONE
            pauseButton.root.visibility = View.GONE
        }

        startService(RouteTrackingService.stopIntent(this))
        viewModel.stopTrackingStatsUpdates()
    }

    private fun onPauseRouteTracking() {
        viewBinding.apply {
            recordButton.root.visibility = View.GONE
            resumeButton.root.visibility = View.VISIBLE
            stopButton.root.visibility = View.VISIBLE
            pauseButton.root.visibility = View.GONE
        }

        startService(RouteTrackingService.pauseIntent(this))
        viewModel.stopTrackingStatsUpdates()
    }

    private fun onStartRouteTracking() {
        viewBinding.apply {
            recordButton.root.visibility = View.GONE
            resumeButton.root.visibility = View.GONE
            stopButton.root.visibility = View.GONE
            pauseButton.root.visibility = View.VISIBLE
        }

        startRouteTrackingService()

        viewModel.requestRouteTrackingLocationUpdate(drawnLocationCount)
        viewModel.startTrackingStatsUpdates()
    }

    private fun startRouteTrackingService() {
        val serviceIntent = RouteTrackingService.startIntent(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkRequiredPermissionsDelegate.verifyPermissionsResult()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkLocationServiceDelegate.verifyLocationServiceResolutionResult(requestCode, resultCode)
    }

    @SuppressLint("MissingPermission")
    private fun onLocationRequirementsReady() {
        (supportFragmentManager.findFragmentById(R.id.tracking_map_view) as SupportMapFragment).getMapAsync { map ->
            this.mapView = map
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true

            viewModel.requestMapInitialLocation()
        }
    }

    private val onLocationPermissionsGranted = {
        lifecycleScope.launch {
            checkLocationServiceDelegate.checkLocationServiceAvailability(this@RouteTrackingActivity)
        }
    }

    private val onLocationServiceAvailable = { onLocationRequirementsReady() }

    companion object {
        const val RC_LOCATION_SERVICE = 1
        const val RC_LOCATION_PERMISSIONS = 2

        fun launchIntent(context: Context): Intent {
            val intent = Intent(context, RouteTrackingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
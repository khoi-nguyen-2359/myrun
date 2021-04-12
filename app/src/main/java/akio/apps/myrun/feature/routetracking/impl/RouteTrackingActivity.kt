package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps._base.ui.dp2px
import akio.apps.myrun.R
import akio.apps.myrun._base.permissions.AppPermissions.locationPermissions
import akio.apps.myrun._base.permissions.RequiredPermissionsDelegate
import akio.apps.myrun._base.utils.CheckLocationServiceDelegate
import akio.apps.myrun._base.utils.DialogDelegate
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun._di.viewModel
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import akio.apps.myrun.databinding.ActivityRouteTrackingBinding
import akio.apps.myrun.feature.googlefit.GoogleFitLinkingDelegate
import akio.apps.myrun.feature.home.impl.HomeActivity
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.feature.routetracking._di.DaggerRouteTrackingFeatureComponent
import akio.apps.myrun.feature.routetracking.view.ActivitySettingsView
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap

class RouteTrackingActivity : AppCompatActivity(), ActivitySettingsView.EventListener {

    private val dialogDelegate by lazy { DialogDelegate(this) }

    private val viewBinding by lazy { ActivityRouteTrackingBinding.inflate(layoutInflater) }

    private val routeTrackingViewModel: RouteTrackingViewModel by viewModel {
        DaggerRouteTrackingFeatureComponent.factory().create(application)
    }

    private val googleFitLinkingDelegate = GoogleFitLinkingDelegate()

    private lateinit var mapView: GoogleMap

    private val checkLocationServiceDelegate by lazy {
        CheckLocationServiceDelegate(
            this,
            listOf(RouteTrackingService.createLocationTrackingRequest())
        )
    }

    private var routePolyline: Polyline? = null
    private var drawnLocationCount: Int = 0
    private val trackingRouteLatLngBounds = LatLngBounds.builder()

    private val requiredPermissionsDelegate = RequiredPermissionsDelegate()
    private val requisiteJobs = lifecycleScope.launchWhenCreated {
        // onCreate: check location permissions -> check location service availability -> allow user to use this screen
        val missingRequiredPermission = !requiredPermissionsDelegate.requestPermissions(
            locationPermissions,
            RC_LOCATION_PERMISSIONS,
            this@RouteTrackingActivity
        ) || !checkLocationServiceDelegate.checkLocationServiceAvailability(
            this@RouteTrackingActivity,
            RC_LOCATION_SERVICE
        )
        if (missingRequiredPermission) {
            finish()
        }

        googleFitLinkingDelegate.requestGoogleFitPermissions(
            this@RouteTrackingActivity,
            RC_ACTIVITY_REGCONITION_PERMISSION,
            RC_FITNESS_DATA_PERMISSIONS
        )

        initMap()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
    }

    override fun onStart() {
        super.onStart()

        routeTrackingViewModel.resumeDataUpdates()
    }

    override fun onStop() {
        super.onStop()

        routeTrackingViewModel.cancelDataUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()

        requisiteJobs.cancel()
    }

    private fun initObservers() {
        observe(routeTrackingViewModel.isInProgress, dialogDelegate::toggleProgressDialog)
        observe(routeTrackingViewModel.trackingLocationBatch, ::onTrackingLocationUpdate)
        observe(routeTrackingViewModel.trackingStats, viewBinding.trackingStatsView::update)
        observe(routeTrackingViewModel.trackingStatus, ::updateViewForTrackingStatus)
        observeEvent(routeTrackingViewModel.mapInitialLocation) { initLocation ->
            mapView.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    initLocation.toGmsLatLng(),
                    MAP_DEFAULT_ZOOM_LEVEL
                )
            )
        }
        observeEvent(routeTrackingViewModel.error, dialogDelegate::showExceptionAlert)
        observeEvent(routeTrackingViewModel.saveActivitySuccess) { onSaveActivitySuccess() }
        observe(
            routeTrackingViewModel.activityType,
            viewBinding.activitySettingsView::setActivityType
        )
        observe(routeTrackingViewModel.activityType, viewBinding.trackingStatsView::setActivityType)
    }

    private fun onSaveActivitySuccess() {
        startService(RouteTrackingService.stopIntent(this))

        startActivity(HomeActivity.clearTaskIntent(this))
    }

    private fun updateViewForTrackingStatus(@RouteTrackingStatus trackingStatus: Int) {
        when (trackingStatus) {
            RouteTrackingStatus.RESUMED -> updateViewsOnTrackingResumed()
            RouteTrackingStatus.PAUSED -> updateViewsOnTrackingPaused()
            RouteTrackingStatus.STOPPED -> updateViewsOnTrackingStopped()
        }
    }

    private fun onTrackingLocationUpdate(batch: List<TrackingLocationEntity>) {
        drawTrackingLocationUpdate(batch)
        moveMapCameraOnTrackingLocationUpdate(batch)
    }

    private fun moveMapCameraOnTrackingLocationUpdate(batch: List<TrackingLocationEntity>) {
        batch.forEach {
            trackingRouteLatLngBounds.include(it.toGmsLatLng())
        }

        // LatLngBounds doesn't have method to check empty!
        if (batch.isNotEmpty()) {
            recenterMapOnTrackingRoute(true)
        }
    }

    private fun recenterMapOnTrackingRoute(animation: Boolean): Rect {
        val mapWidth =
            supportFragmentManager.findFragmentById(R.id.tracking_map_view)?.view?.measuredWidth
                ?: application.resources.displayMetrics.widthPixels
        val routeImageRatio = resources.getString(R.string.route_tracking_captured_image_ratio)
            .toFloatOrNull()
            ?: ROUTE_IMAGE_RATIO

        val boundingBox = Rect(0, 0, mapWidth, (mapWidth / routeImageRatio).toInt())

        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
            trackingRouteLatLngBounds.build(),
            boundingBox.right,
            boundingBox.bottom,
            MAP_LATLNG_BOUND_PADDING
        )

        if (animation) {
            mapView.animateCamera(cameraUpdate)
        } else {
            mapView.moveCamera(cameraUpdate)
        }

        return boundingBox
    }

    private fun drawTrackingLocationUpdate(batch: List<TrackingLocationEntity>) {
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
            recordButton.setOnClickListener { onStartRouteTracking() }
            pauseButton.setOnClickListener { onPauseRouteTracking() }
            resumeButton.setOnClickListener { onResumeRouteTracking() }
            stopButton.setOnClickListener { onStopRouteTracking() }
            viewBinding.activitySettingsView.eventListener = this@RouteTrackingActivity
        }
    }

    private fun updateViewsOnTrackingStopped() {
        viewBinding.apply {
            recordButton.visibility = View.VISIBLE
            resumeButton.visibility = View.GONE
            stopButton.visibility = View.GONE
            pauseButton.visibility = View.GONE
        }
    }

    private fun updateViewsOnTrackingPaused() {
        viewBinding.apply {
            recordButton.visibility = View.GONE
            resumeButton.visibility = View.VISIBLE
            stopButton.visibility = View.VISIBLE
            pauseButton.visibility = View.GONE

            viewBinding.activitySettingsView.visibility = View.GONE
            viewBinding.trackingStatsView.visibility = View.VISIBLE
            viewBinding.semiTransparentBackdropView.visibility = View.VISIBLE
        }
    }

    private fun updateViewsOnTrackingResumed() {
        viewBinding.apply {
            recordButton.visibility = View.GONE
            resumeButton.visibility = View.GONE
            stopButton.visibility = View.GONE
            pauseButton.visibility = View.VISIBLE

            viewBinding.activitySettingsView.visibility = View.GONE
            viewBinding.trackingStatsView.visibility = View.VISIBLE
            viewBinding.semiTransparentBackdropView.visibility = View.VISIBLE
        }
    }

    private fun onStartRouteTracking() {
        startRouteTrackingService()
        routeTrackingViewModel.requestDataUpdates()
    }

    private fun onPauseRouteTracking() {
        startService(RouteTrackingService.pauseIntent(this))
        routeTrackingViewModel.cancelDataUpdates()
    }

    private fun onResumeRouteTracking() {
        startService(RouteTrackingService.resumeIntent(this))
        routeTrackingViewModel.requestDataUpdates()
    }

    private fun onStopRouteTracking() {
        // we are currently paused, do saving:
        AlertDialog.Builder(this)
            .setTitle(R.string.route_tracking_stop_confirmation_title)
            .setPositiveButton(R.string.action_just_do_it) { _, _ ->
                saveActivity()
            }
            .setNegativeButton(R.string.action_cancel) { _, _ -> }
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun saveActivity() {
        mapView.isMyLocationEnabled = false
        val boundingBox = recenterMapOnTrackingRoute(false)
        mapView.snapshot { mapSnapshot ->
            mapView.isMyLocationEnabled = true
            val cropped = Bitmap.createBitmap(
                mapSnapshot,
                (mapSnapshot.width - boundingBox.width()) / 2,
                (mapSnapshot.height - boundingBox.height()) / 2,
                boundingBox.width(),
                boundingBox.height()
            )
            routeTrackingViewModel.saveActivity(cropped)
        }
    }

    private fun startRouteTrackingService() {
        val serviceIntent = RouteTrackingService.startIntent(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        requiredPermissionsDelegate.verifyPermissionsResult(this, locationPermissions)

        when (requestCode) {
            RC_ACTIVITY_REGCONITION_PERMISSION -> googleFitLinkingDelegate.verifyActivityRecognitionPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_FITNESS_DATA_PERMISSIONS -> googleFitLinkingDelegate.verifyFitnessDataPermission()
            RC_LOCATION_SERVICE -> checkLocationServiceDelegate.verifyLocationServiceResolutionResult(
                resultCode
            )
        }
    }

    private fun initMap() {
        (supportFragmentManager.findFragmentById(R.id.tracking_map_view) as SupportMapFragment).getMapAsync { map ->
            initMapView(map)
            initObservers()
            routeTrackingViewModel.requestInitialData()
        }
    }

    @SuppressLint("MissingPermission")
    private fun initMapView(map: GoogleMap) {
        this.mapView = map
        map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this,
                R.raw.route_tracking_google_map_styles
            )
        )
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
    }

    override fun onActivityTypeSelected(activityType: ActivityType) {
        routeTrackingViewModel.onSelectActivityType(activityType)
    }

    companion object {
        val MAP_LATLNG_BOUND_PADDING = 30.dp2px.toInt()
        const val MAP_DEFAULT_ZOOM_LEVEL = 18f
        const val ROUTE_IMAGE_RATIO = 1.7f

        const val RC_LOCATION_SERVICE = 1
        const val RC_LOCATION_PERMISSIONS = 2
        const val RC_FITNESS_DATA_PERMISSIONS = 3

        const val RC_ACTIVITY_REGCONITION_PERMISSION = 4

        fun launchIntent(context: Context): Intent {
            val intent = Intent(context, RouteTrackingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}

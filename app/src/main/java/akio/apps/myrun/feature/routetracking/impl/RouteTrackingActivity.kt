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
import akio.apps.myrun.feature.home.HomeActivity
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.feature.routetracking._di.DaggerRouteTrackingFeatureComponent
import akio.apps.myrun.feature.routetracking.ui.StopDialogOptionId
import akio.apps.myrun.feature.routetracking.ui.StopOptionsDialog
import akio.apps.myrun.feature.routetracking.view.ActivitySettingsView
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.os.Bundle
import android.util.Size
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.addRepeatingJob
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.JointType
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.LatLngBounds
import com.google.android.libraries.maps.model.MapStyleOptions
import com.google.android.libraries.maps.model.Polyline
import com.google.android.libraries.maps.model.PolylineOptions
import com.google.android.libraries.maps.model.RoundCap
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

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
            RouteTrackingService.createLocationTrackingRequest()
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

    private var trackMapCameraOnLocationUpdateJob: Job? = null

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
        observe(routeTrackingViewModel.trackingStatus, ::onTrackingStatusChanged)
        observeEvent(routeTrackingViewModel.error, dialogDelegate::showExceptionAlert)
        observeEvent(routeTrackingViewModel.isStoreActivityDone) { onStoreActivitySuccess() }
        observe(
            routeTrackingViewModel.activityType,
            viewBinding.activitySettingsView::setActivityType
        )
        observe(routeTrackingViewModel.activityType, viewBinding.trackingStatsView::setActivityType)
        trackMapCameraOnLocationUpdate()
    }

    private fun trackMapCameraOnLocationUpdate() {
        trackMapCameraOnLocationUpdateJob?.cancel()
        trackMapCameraOnLocationUpdateJob = addRepeatingJob(Lifecycle.State.STARTED) {
            val locationRequest = RouteTrackingService.createLocationTrackingRequest()
            routeTrackingViewModel.getLocationUpdate(locationRequest)
                .collect {
                    if (it.isNotEmpty()) {
                        recenterMap(it.last())
                    }
                }
        }
    }

    private fun recenterMap(location: Location) {
        if (!::mapView.isInitialized) {
            return
        }
        val bounds = LatLngBounds.builder().include(location.toGmsLatLng()).build()
        recenterMap(bounds, animation = true)
    }

    private fun onStoreActivitySuccess() {
        startService(RouteTrackingService.stopIntent(this))
        startActivity(HomeActivity.clearTaskIntent(this))
        ActivityUploadWorker.enqueue(this)
    }

    private fun onTrackingStatusChanged(@RouteTrackingStatus trackingStatus: Int) {
        updateViews(trackingStatus)
        changeMapCameraLocationUpdateTracking(trackingStatus)
    }

    /**
     * There are 2 types of camera movements: camera tracking on drawn route and on current location
     * If route tracking is in progress, camera tracks the route, otherwise, camera tracks on
     * location update.
     */
    private fun changeMapCameraLocationUpdateTracking(@RouteTrackingStatus trackingStatus: Int) {
        when (trackingStatus) {
            RouteTrackingStatus.RESUMED -> {
                trackMapCameraOnLocationUpdateJob?.cancel()
                trackMapCameraOnLocationUpdateJob = null
            }
            RouteTrackingStatus.PAUSED -> {
                trackMapCameraOnLocationUpdate()
            }
        }
    }

    private fun updateViews(@RouteTrackingStatus trackingStatus: Int) {
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
            recenterMap(trackingRouteLatLngBounds.build(), true)
        }
    }

    private fun recenterMap(latLngBounds: LatLngBounds, animation: Boolean): Size {
        val mapWidth =
            supportFragmentManager.findFragmentById(R.id.tracking_map_view)?.view?.measuredWidth
                ?: application.resources.displayMetrics.widthPixels
        val routeImageRatio = resources.getString(R.string.route_tracking_captured_image_ratio)
            .toFloatOrNull()
            ?: ROUTE_IMAGE_RATIO

        val cameraViewPortSize = Size(mapWidth, (mapWidth / routeImageRatio).toInt())
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
            latLngBounds,
            cameraViewPortSize.width,
            cameraViewPortSize.height,
            MAP_LATLNG_BOUND_PADDING
        )

        if (animation) {
            mapView.animateCamera(cameraUpdate)
        } else {
            mapView.moveCamera(cameraUpdate)
        }

        return cameraViewPortSize
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

    @OptIn(ExperimentalMaterialApi::class)
    private fun initViews() {
        viewBinding.apply {
            setContentView(root)
            recordButton.setOnClickListener { startRouteTracking() }
            pauseButton.setOnClickListener { pauseRouteTracking() }
            resumeButton.setOnClickListener { resumeRouteTracking() }
            stopButton.setOnClickListener { stopRouteTracking() }
            activitySettingsView.eventListener = this@RouteTrackingActivity
            viewBinding.composableStopOptionsView.setContent {
                StopOptionsDialog(routeTrackingViewModel, ::selectStopOptionItem)
            }
        }
    }

    private fun selectStopOptionItem(selectedOptionId: StopDialogOptionId) =
        when (selectedOptionId) {
            StopDialogOptionId.Save -> saveActivity()
            StopDialogOptionId.Discard -> {
                TODO("not implemented yet")
            }
            StopDialogOptionId.Cancel -> {
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

    private fun startRouteTracking() {
        ContextCompat.startForegroundService(this, RouteTrackingService.startIntent(this))
        routeTrackingViewModel.requestDataUpdates()
    }

    private fun pauseRouteTracking() {
        startService(RouteTrackingService.pauseIntent(this))
        routeTrackingViewModel.cancelDataUpdates()
    }

    private fun resumeRouteTracking() {
        startService(RouteTrackingService.resumeIntent(this))
        routeTrackingViewModel.requestDataUpdates()
    }

    private fun stopRouteTracking() {
        routeTrackingViewModel.isStopOptionDialogShowing.value = true
    }

    @SuppressLint("MissingPermission")
    private fun saveActivity() {
        mapView.isMyLocationEnabled = false
        val cameraViewSize = recenterMap(trackingRouteLatLngBounds.build(), false)
        mapView.snapshot { mapSnapshot ->
            mapView.isMyLocationEnabled = true
            // TODO: do this in background?
            val cropped = Bitmap.createBitmap(
                mapSnapshot,
                (mapSnapshot.width - cameraViewSize.width) / 2,
                (mapSnapshot.height - cameraViewSize.height) / 2,
                cameraViewSize.width,
                cameraViewSize.height
            )
            routeTrackingViewModel.storeActivityData(cropped)
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
            RC_ACTIVITY_REGCONITION_PERMISSION ->
                googleFitLinkingDelegate.verifyActivityRecognitionPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_FITNESS_DATA_PERMISSIONS -> googleFitLinkingDelegate.verifyFitnessDataPermission()
            RC_LOCATION_SERVICE ->
                checkLocationServiceDelegate.verifyLocationServiceResolutionResult(resultCode)
        }
    }

    private fun initMap() {
        (supportFragmentManager.findFragmentById(R.id.tracking_map_view) as SupportMapFragment)
            .getMapAsync { map ->
                initMapView(map)
                initObservers()
                routeTrackingViewModel.requestInitialData()
            }
    }

    @SuppressLint("MissingPermission")
    private fun initMapView(map: GoogleMap) {
        this.mapView = map
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.setAllGesturesEnabled(false)
        map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this,
                R.raw.google_map_styles
            )
        )
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

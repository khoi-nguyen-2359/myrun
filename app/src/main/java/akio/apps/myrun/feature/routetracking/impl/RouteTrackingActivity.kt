package akio.apps.myrun.feature.routetracking.impl

import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps._base.ui.dp2px
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.DialogDelegate
import akio.apps.myrun._base.utils.LatLngBoundsBuilder
import akio.apps.myrun._base.utils.LocationServiceChecker
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun._di.viewModel
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.location.LocationEntity
import akio.apps.myrun.data.routetracking.RouteTrackingStatus
import akio.apps.myrun.databinding.ActivityRouteTrackingBinding
import akio.apps.myrun.feature.home.HomeActivity
import akio.apps.myrun.feature.routetracking.RouteTrackingViewModel
import akio.apps.myrun.feature.routetracking._di.DaggerRouteTrackingFeatureComponent
import akio.apps.myrun.feature.routetracking.ui.StopDialogOptionId
import akio.apps.myrun.feature.routetracking.ui.StopOptionsDialog
import akio.apps.myrun.feature.routetracking.ui.TrackingControlButtonPanel
import akio.apps.myrun.feature.routetracking.ui.TrackingControlButtonType
import akio.apps.myrun.feature.routetracking.view.ActivitySettingsView
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Size
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import timber.log.Timber
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class RouteTrackingActivity : AppCompatActivity(), ActivitySettingsView.EventListener {

    private val dialogDelegate by lazy { DialogDelegate(this) }

    private val viewBinding by lazy { ActivityRouteTrackingBinding.inflate(layoutInflater) }

    private val routeTrackingViewModel: RouteTrackingViewModel by viewModel {
        DaggerRouteTrackingFeatureComponent.factory().create(application)
    }

    private lateinit var mapView: GoogleMap

    private val locationServiceChecker by lazy {
        LocationServiceChecker(activity = this, RC_LOCATION_SERVICE)
    }

    private var routePolyline: Polyline? = null
    private var drawnLocationCount: Int = 0

    private val trackingRouteLatLngBounds: LatLngBoundsBuilder = LatLngBoundsBuilder()

    private val locationPermissionChecker: LocationPermissionChecker =
        LocationPermissionChecker(activity = this)

    private val requisiteJobs = lifecycleScope.launchWhenCreated {
        // onCreate: check location permissions -> check location service availability -> allow user to use this screen
        val requestConfig = routeTrackingViewModel.getLocationRequestConfig()
        val missingRequiredPermission =
            !locationPermissionChecker.check() || !locationServiceChecker.check(requestConfig)
        if (missingRequiredPermission) {
            finish()
            return@launchWhenCreated
        }
    }

    private var trackMapCameraOnLocationUpdateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        routeTrackingViewModel.requestInitialData()
        initViews()
        initMap()
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
        observe(
            routeTrackingViewModel.activityType,
            viewBinding.activitySettingsView::setActivityType
        )
        observe(routeTrackingViewModel.activityType, viewBinding.trackingStatsView::setActivityType)
        setAutoCameraEnabled(true)
    }

    private fun recenterMap(location: LocationEntity) {
        if (!::mapView.isInitialized) {
            return
        }
        val bounds = LatLngBounds.builder().include(location.toGmsLatLng()).build()
        recenterMap(bounds, animation = true, getCameraViewPortSize())
    }

    private fun stopTrackingServiceAndFinish() {
        startService(RouteTrackingService.stopIntent(this))
        startActivity(HomeActivity.clearTaskIntent(this))
    }

    private fun onTrackingStatusChanged(@RouteTrackingStatus trackingStatus: Int) {
        updateViews(trackingStatus)
        changeMapCameraLocationUpdateTracking(trackingStatus)
    }

    /**
     * There are 2 types of cameLocationDataSourceImpl.ktra movements: camera tracking on drawn route and on current location
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
                setAutoCameraEnabled(true)
            }
        }
    }

    private fun updateViews(@RouteTrackingStatus trackingStatus: Int) {
        when (trackingStatus) {
            RouteTrackingStatus.RESUMED -> updateViewsOnTrackingResumed()
            RouteTrackingStatus.PAUSED -> updateViewsOnTrackingPaused()
        }
    }

    private fun onTrackingLocationUpdate(batch: List<LocationEntity>) {
        drawTrackingLocationUpdate(batch)
        moveMapCameraOnTrackingLocationUpdate(batch)
    }

    private fun moveMapCameraOnTrackingLocationUpdate(batch: List<LocationEntity>) {
        batch.forEach {
            trackingRouteLatLngBounds.include(it.toGmsLatLng())
        }

        // LatLngBounds doesn't have method to check empty!
        if (batch.isNotEmpty()) {
            val latLngBounds = trackingRouteLatLngBounds.build()
            if (latLngBounds != null) {
                recenterMap(latLngBounds, true, getCameraViewPortSize())
            }
        }
    }

    private fun recenterMap(
        latLngBounds: LatLngBounds,
        animation: Boolean,
        cameraViewPortSize: Size
    ) {
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
    }

    private fun getCameraViewPortSize(): Size {
        val mapWidth =
            supportFragmentManager.findFragmentById(R.id.tracking_map_view)?.view?.measuredWidth
                ?: application.resources.displayMetrics.widthPixels
        val routeImageRatio = resources.getString(R.string.route_tracking_captured_image_ratio)
            .toFloatOrNull()
            ?: ROUTE_IMAGE_RATIO

        return Size(mapWidth, (mapWidth / routeImageRatio).toInt())
    }

    private fun drawTrackingLocationUpdate(batch: List<LocationEntity>) {
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

    private fun initViews() = viewBinding.apply {
        setContentView(root)
        activitySettingsView.eventListener = this@RouteTrackingActivity
        composableView.setContent {
            TrackingControlButtonPanel(routeTrackingViewModel, ::onClickControlButton)
            StopOptionsDialog(routeTrackingViewModel, ::selectStopOptionItem)
        }
    }

    private fun onClickControlButton(buttonType: TrackingControlButtonType) = when (buttonType) {
        TrackingControlButtonType.Start -> startRouteTracking()
        TrackingControlButtonType.Pause -> pauseRouteTracking()
        TrackingControlButtonType.Resume -> resumeRouteTracking()
        TrackingControlButtonType.Stop -> stopRouteTracking()
    }

    private fun selectStopOptionItem(selectedOptionId: StopDialogOptionId) =
        when (selectedOptionId) {
            StopDialogOptionId.Save -> saveActivity()
            StopDialogOptionId.Discard -> discardActivity()
            StopDialogOptionId.Cancel -> {
                // dialog closed, no action.
            }
        }

    private fun discardActivity() {
        AlertDialog.Builder(this)
            .setMessage(R.string.route_tracking_discard_activity_confirm_message)
            .setPositiveButton(R.string.action_yes) { _, _ ->
                routeTrackingViewModel.discardActivity()
                stopTrackingServiceAndFinish()
            }
            .setNegativeButton(R.string.action_no) { _, _ -> }
            .show()
    }

    private fun updateViewsOnTrackingPaused() {
        viewBinding.apply {
            viewBinding.activitySettingsView.visibility = View.GONE
            viewBinding.trackingStatsView.visibility = View.VISIBLE
            viewBinding.semiTransparentBackdropView.visibility = View.VISIBLE
        }
    }

    private fun updateViewsOnTrackingResumed() {
        viewBinding.apply {
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

    /**
     * Captures current snapshot of map view, then crops to the part that contains the route.
     * Returns null when map snapshot is failed to be captured.
     */
    @SuppressLint("MissingPermission")
    private suspend fun getRouteImageBitmap(): Bitmap? {
        // hide the my location button
        mapView.isMyLocationEnabled = false
        val cameraViewPortSize = getCameraViewPortSize()
        val bounds = trackingRouteLatLngBounds.build()
        if (bounds != null) {
            recenterMap(
                bounds,
                animation = false,
                cameraViewPortSize
            )
        }
        val mapImageSnapShot: Bitmap? = suspendCancellableCoroutine { continuation ->
            mapView.snapshot { mapSnapshot ->
                Timber.d(Thread.currentThread().name)
                continuation.resume(mapSnapshot)
            }
        }
        mapView.isMyLocationEnabled = true
        if (mapImageSnapShot == null) {
            // null when snapshot can not be taken
            Timber.e(Exception("Map snapshot can not be taken."))
            return null
        }

        return withContext(Dispatchers.IO) {
            Bitmap.createBitmap(
                mapImageSnapShot,
                (mapImageSnapShot.width - cameraViewPortSize.width) / 2,
                (mapImageSnapShot.height - cameraViewPortSize.height) / 2,
                cameraViewPortSize.width,
                cameraViewPortSize.height
            )
        }
    }

    private fun saveActivity() {
        lifecycleScope.launch {
            val routeImageBitmap = getRouteImageBitmap()
            if (routeImageBitmap != null) {
                dialogDelegate.showProgressDialog()
                routeTrackingViewModel.storeActivityData(routeImageBitmap)
                dialogDelegate.dismissProgressDialog()
                ActivityUploadWorker.enqueue(this@RouteTrackingActivity)
                stopTrackingServiceAndFinish()
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_LOCATION_SERVICE ->
                locationServiceChecker.verifyLocationServiceResolutionResult(resultCode)
        }
    }

    private fun initMap() {
        (supportFragmentManager.findFragmentById(R.id.tracking_map_view) as SupportMapFragment)
            .getMapAsync { map ->
                initMapView(map)
                initObservers()
            }
    }

    @SuppressLint("MissingPermission")
    private fun initMapView(map: GoogleMap) {
        this.mapView = map
        map.setOnMyLocationButtonClickListener {
            setAutoCameraEnabled(true)
            true
        }
        map.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                setAutoCameraEnabled(false)
            }
        }
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        map.uiSettings.setAllGesturesEnabled(true)
        map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this,
                R.raw.google_map_styles
            )
        )
    }

    private fun setAutoCameraEnabled(isEnabled: Boolean) {
        trackMapCameraOnLocationUpdateJob?.cancel()
        if (isEnabled) {
            trackMapCameraOnLocationUpdateJob = addRepeatingJob(Lifecycle.State.STARTED) {
                routeTrackingViewModel.getLocationUpdate()
                    .onStart { emit(routeTrackingViewModel.getLastLocationFlow().toList()) }
                    .collect {
                        it.lastOrNull()?.let { lastItem ->
                            recenterMap(lastItem)
                        }
                    }
            }
        }
    }

    override fun onActivityTypeSelected(activityType: ActivityType) {
        routeTrackingViewModel.onSelectActivityType(activityType)
    }

    companion object {
        val MAP_LATLNG_BOUND_PADDING = 30.dp2px.toInt()
        const val ROUTE_IMAGE_RATIO = 1.7f

        const val RC_LOCATION_SERVICE = 1

        fun launchIntent(context: Context): Intent {
            val intent = Intent(context, RouteTrackingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}

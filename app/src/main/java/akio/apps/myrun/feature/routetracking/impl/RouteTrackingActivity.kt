package akio.apps.myrun.feature.routetracking.impl

import akio.apps.common.feature.lifecycle.collectEventRepeatOnStarted
import akio.apps.common.feature.lifecycle.collectRepeatOnStarted
import akio.apps.common.feature.lifecycle.observe
import akio.apps.common.feature.lifecycle.observeEvent
import akio.apps.common.feature.ui.dp2px
import akio.apps.common.feature.viewmodel.LaunchCatchingDelegate
import akio.apps.common.feature.viewmodel.LaunchCatchingDelegateImpl
import akio.apps.common.feature.viewmodel.viewModel
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.LatLngBoundsBuilder
import akio.apps.myrun._base.utils.LocationServiceChecker
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun.data.activity.model.ActivityType
import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.tracking.api.RouteTrackingStatus
import akio.apps.myrun.databinding.ActivityRouteTrackingBinding
import akio.apps.myrun.feature.activityroutemap.ui.ActivityRouteMapActivity
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
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Size
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.drawToBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.BitmapDescriptorFactory
import com.google.android.libraries.maps.model.JointType
import com.google.android.libraries.maps.model.LatLng
import com.google.android.libraries.maps.model.LatLngBounds
import com.google.android.libraries.maps.model.MapStyleOptions
import com.google.android.libraries.maps.model.Marker
import com.google.android.libraries.maps.model.MarkerOptions
import com.google.android.libraries.maps.model.Polyline
import com.google.android.libraries.maps.model.PolylineOptions
import com.google.android.libraries.maps.model.RoundCap
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class RouteTrackingActivity(
    private val launchCatchingDelegate: LaunchCatchingDelegateImpl = LaunchCatchingDelegateImpl()
) :
    AppCompatActivity(),
    ActivitySettingsView.EventListener,
    LaunchCatchingDelegate by launchCatchingDelegate {

    // use this flag to check if map has ever been loaded (or never been due to no internet)
    private var hasMapCameraBeenIdled: Boolean = false

    private val dialogDelegate by lazy { akio.apps.myrun.feature.base.DialogDelegate(this) }

    private val viewBinding by lazy { ActivityRouteTrackingBinding.inflate(layoutInflater) }

    private val routeTrackingViewModel: RouteTrackingViewModel by viewModel {
        DaggerRouteTrackingFeatureComponent.factory().create()
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
        R.string.default_web_client_id
    }

    private var trackMapCameraOnLocationUpdateJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()

        routeTrackingViewModel.requestInitialData()
        initViews()
        initMap()
    }

    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(
            viewBinding.composableView
        ) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParamsWithInsets = (view.layoutParams as? ViewGroup.MarginLayoutParams)
                ?.apply { bottomMargin = insets.bottom }
            view.layoutParams = layoutParamsWithInsets
            WindowInsetsCompat.CONSUMED
        }
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

        routePolyline = null
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
        collectRepeatOnStarted(isInProgress, dialogDelegate::toggleProgressDialog)
        collectEventRepeatOnStarted(error, dialogDelegate::showExceptionAlert)
        setAutoCameraEnabled(true)
    }

    private fun recenterMap(location: Location) {
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
     * There are 2 types of camera movements: camera tracking on drawn route and on current location
     * If route tracking is in progress, camera tracks the route, otherwise, camera tracks on
     * location update.
     */
    private fun changeMapCameraLocationUpdateTracking(@RouteTrackingStatus trackingStatus: Int) {
        when (trackingStatus) {
            RouteTrackingStatus.RESUMED -> {
                setAutoCameraEnabled(false)
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

    private var startPointMarker: Marker? = null
    private fun onTrackingLocationUpdate(batch: List<Location>) {
        addStartPointMarkerIfNotAdded(batch)
        drawTrackingLocationUpdate(batch)
        moveMapCameraOnTrackingLocationUpdate(batch)
    }

    private fun addStartPointMarkerIfNotAdded(batch: List<Location>) {
        if (startPointMarker != null || batch.isEmpty()) {
            return
        }
        val startPointLocation = batch.first()
        val startMarkerBitmap = ActivityRouteMapActivity.createDrawableBitmap(
            context = this,
            drawableResId = R.drawable.ic_start_marker
        )
        val startMarker = MarkerOptions()
            .position(startPointLocation.toGmsLatLng())
            .icon(BitmapDescriptorFactory.fromBitmap(startMarkerBitmap))
            .anchor(0.5f, 0.5f)
        startPointMarker = mapView.addMarker(startMarker)
    }

    private fun moveMapCameraOnTrackingLocationUpdate(batch: List<Location>) {
        batch.forEach {
            trackingRouteLatLngBounds.include(it.toGmsLatLng())
        }

        if (isStickyCamera) {
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

    private fun drawTrackingLocationUpdate(batch: List<Location>) {
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
            TrackingControlButtonPanel(
                routeTrackingViewModel,
                ::onClickControlButton,
                ::onClickMyLocation
            )
            StopOptionsDialog(routeTrackingViewModel, ::selectStopOptionItem)
        }
    }

    private fun onClickMyLocation() {
        isStickyCamera = true
        lifecycleScope.launch {
            val lastLocation = routeTrackingViewModel.getLastLocationFlow().first()
            recenterMap(lastLocation)
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
            StopDialogOptionId.Discard -> showActivityDiscardAlert()
            StopDialogOptionId.Cancel -> {
                // dialog closed, no action.
            }
        }

    private fun showActivityDiscardAlert() {
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
    private suspend fun getRouteImageBitmap(): Bitmap {
        // hide the my location button
        mapView.isMyLocationEnabled = false
        // hide the start point marker
        startPointMarker?.isVisible = false
        val cameraViewPortSize = getCameraViewPortSize()
        val bounds = trackingRouteLatLngBounds.build()
        if (bounds != null) {
            recenterMap(
                bounds,
                animation = false,
                cameraViewPortSize
            )
        }
        delay(500) // for above map adjustments take effects

        val routeImageBitmap: Bitmap = captureGoogleMapViewSnapshot(cameraViewPortSize)
            ?: drawRouteImage(cameraViewPortSize)

        mapView.isMyLocationEnabled = true
        startPointMarker?.isVisible = true

        return routeImageBitmap
    }

    /**
     * Captures Google map view snapshot in a bitmap. Returns null in case map is unavailable
     * (no internet, no drawing happens)
     */
    private suspend fun captureGoogleMapViewSnapshot(cameraViewPortSize: Size): Bitmap? {
        if (!hasMapCameraBeenIdled)
            return null
        val snapshot = suspendCancellableCoroutine<Bitmap> { continuation ->
            mapView.snapshot { mapSnapshot ->
                continuation.resume(mapSnapshot)
            }
        }

        val routeImage = withContext(Dispatchers.IO) {
            Bitmap.createBitmap(
                snapshot,
                (snapshot.width - cameraViewPortSize.width) / 2,
                (snapshot.height - cameraViewPortSize.height) / 2,
                cameraViewPortSize.width,
                cameraViewPortSize.height
            )
        }
        snapshot.recycle()
        return routeImage
    }

    /**
     * In case map view can not generate snapshot, manually draw polyline into a bitmap.
     */
    private suspend fun drawRouteImage(cameraViewPortSize: Size): Bitmap {
        val mapViewBitmap = viewBinding.trackingMapView.drawToBitmap()
        val mapProjection = mapView.projection
        val mapPolypoints = routePolyline?.points ?: emptyList()
        val routeImage = withContext(Dispatchers.IO) {
            val canvas = Canvas(mapViewBitmap)
            canvas.drawColor(Color.parseColor("#e5e5e5"))
            val pts = mapPolypoints.map(mapProjection::toScreenLocation)
                .flatMapIndexed { index, item ->
                    listOf(item.x.toFloat(), item.y.toFloat()) +
                        if (index >= 1 && index < mapPolypoints.size - 1) {
                            listOf(item.x.toFloat(), item.y.toFloat())
                        } else {
                            emptyList()
                        }
                }
                .toFloatArray()
            if (pts.isNotEmpty()) {
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    strokeWidth = 3.dp2px
                    color = ContextCompat.getColor(
                        this@RouteTrackingActivity,
                        R.color.route_tracking_polyline
                    )
                }
                canvas.drawLines(pts, linePaint)
            }

            Bitmap.createBitmap(
                mapViewBitmap,
                (mapViewBitmap.width - cameraViewPortSize.width) / 2,
                (mapViewBitmap.height - cameraViewPortSize.height) / 2,
                cameraViewPortSize.width,
                cameraViewPortSize.height
            )
        }
        mapViewBitmap.recycle()
        return routeImage
    }

    private fun saveActivity() {
        lifecycleScope.launchCatching {
            val routeImageBitmap = getRouteImageBitmap()
            routeTrackingViewModel.storeActivityData(routeImageBitmap)
            ActivityUploadWorker.enqueue(this@RouteTrackingActivity)
            stopTrackingServiceAndFinish()
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

    private var isStickyCamera: Boolean = true

    @SuppressLint("MissingPermission")
    private fun initMapView(map: GoogleMap) {
        this.mapView = map
        map.setMaxZoomPreference(MAX_MAP_ZOOM_LEVEL)
        map.setOnMyLocationButtonClickListener {
            isStickyCamera = true
            false
        }
        map.setOnCameraIdleListener {
            hasMapCameraBeenIdled = true
        }
        map.setOnCameraMoveStartedListener { reason ->
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                isStickyCamera = false
            }
        }
        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false
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
            trackMapCameraOnLocationUpdateJob = lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    routeTrackingViewModel.getLocationUpdate()
                        .onStart { emit(routeTrackingViewModel.getLastLocationFlow().toList()) }
                        .collect {
                            if (isStickyCamera) {
                                it.lastOrNull()?.let { lastItem ->
                                    recenterMap(lastItem)
                                }
                            }
                        }
                }
            }
        }
    }

    override fun onActivityTypeSelected(activityType: ActivityType) {
        routeTrackingViewModel.onSelectActivityType(activityType)
    }

    companion object {
        private val MAP_LATLNG_BOUND_PADDING = 30.dp2px.toInt()
        private const val MAX_MAP_ZOOM_LEVEL = 19f // 20 = buildings level
        const val ROUTE_IMAGE_RATIO = 1.7f

        const val RC_LOCATION_SERVICE = 1

        fun launchIntent(context: Context): Intent {
            val intent = Intent(context, RouteTrackingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}

package akio.apps.myrun.feature.routetracking.impl

import akio.apps.common.data.LaunchCatchingDelegate
import akio.apps.common.data.LaunchCatchingDelegateImpl
import akio.apps.common.feature.lifecycle.collectEventRepeatOnStarted
import akio.apps.common.feature.lifecycle.collectRepeatOnStarted
import akio.apps.common.feature.lifecycle.observe
import akio.apps.common.feature.ui.dp2px
import akio.apps.common.feature.viewmodel.lazyViewModelProvider
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.LatLngBoundsBuilder
import akio.apps.myrun._base.utils.LocationServiceChecker
import akio.apps.myrun._base.utils.toGmsLatLng
import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.location.api.LOG_TAG_LOCATION
import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.tracking.api.RouteTrackingStatus
import akio.apps.myrun.data.tracking.api.RouteTrackingStatus.PAUSED
import akio.apps.myrun.data.tracking.api.RouteTrackingStatus.RESUMED
import akio.apps.myrun.databinding.ActivityRouteTrackingBinding
import akio.apps.myrun.feature.activitydetail.ActivityRouteMapActivity
import akio.apps.myrun.feature.home.HomeActivity
import akio.apps.myrun.feature.routetracking._di.DaggerRouteTrackingFeatureComponent
import akio.apps.myrun.feature.routetracking.ui.StopDialogOptionId
import akio.apps.myrun.feature.routetracking.ui.StopOptionsDialog
import akio.apps.myrun.feature.routetracking.ui.TrackingControlButtonPanel
import akio.apps.myrun.feature.routetracking.ui.TrackingControlButtonType
import akio.apps.myrun.feature.routetracking.view.ActivitySettingsView
import akio.apps.myrun.worker.ActivityUploadWorker
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
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber

class RouteTrackingActivity(
    private val launchCatchingDelegate: LaunchCatchingDelegate = LaunchCatchingDelegateImpl(),
) :
    AppCompatActivity(),
    ActivitySettingsView.EventListener,
    LaunchCatchingDelegate by launchCatchingDelegate {

    // use this flag to check if map has ever been loaded (or never been due to no internet)
    private var hasMapCameraBeenIdled: Boolean = false

    private val dialogDelegate by lazy { akio.apps.myrun.feature.base.DialogDelegate(this) }

    private val viewBinding by lazy { ActivityRouteTrackingBinding.inflate(layoutInflater) }

    private val routeTrackingViewModel: RouteTrackingViewModel by lazyViewModelProvider {
        DaggerRouteTrackingFeatureComponent.factory().create().routeTrackingViewModel()
    }

    private lateinit var mapView: GoogleMap

    private val locationServiceChecker by lazy {
        LocationServiceChecker(activity = this, RC_LOCATION_SERVICE)
    }

    private var routePolyline: Polyline? = null
    private var drawnLocationCount: Int = 0

    /**
     * LatLngBounds that is used for map camera to zoom into the whole route.
     */
    private val trackingRouteCameraBounds: LatLngBoundsBuilder = LatLngBoundsBuilder()

    private val locationPermissionChecker: LocationPermissionChecker =
        LocationPermissionChecker(activity = this)

    private val requisiteJobs = lifecycleScope.launchWhenCreated {
        // onCreate: check location permissions -> check location service availability -> allow user to use this screen
        val requestConfig = routeTrackingViewModel.getLocationRequestConfigFlow().first()
        val missingRequiredPermission =
            !locationPermissionChecker.check() || !locationServiceChecker.check(requestConfig)
        if (missingRequiredPermission) {
            finish()
            return@launchWhenCreated
        }
        R.string.default_web_client_id
    }

    private var cameraMovement: CameraMovement = CameraMovement.StickyLocation

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
        collectRepeatOnStarted(
            routeTrackingViewModel.isLaunchCatchingInProgress,
            dialogDelegate::toggleProgressDialog
        )
        observe(routeTrackingViewModel.trackingLocationBatch, ::onTrackingLocationUpdate)
        observe(routeTrackingViewModel.trackingStats, viewBinding.trackingStatsView::update)
        observe(routeTrackingViewModel.trackingStatus, ::onTrackingStatusChanged)
        collectEventRepeatOnStarted(
            routeTrackingViewModel.launchCatchingError,
            dialogDelegate::showExceptionAlert
        )
        observe(
            routeTrackingViewModel.activityType,
            viewBinding.activitySettingsView::setActivityType
        )
        observe(routeTrackingViewModel.activityType, viewBinding.trackingStatsView::setActivityType)
        collectRepeatOnStarted(isLaunchCatchingInProgress, dialogDelegate::toggleProgressDialog)
        collectEventRepeatOnStarted(launchCatchingError, dialogDelegate::showExceptionAlert)
        collectRepeatOnStarted(
            routeTrackingViewModel.locationUpdateFlow,
            ::moveCameraToLastLocation
        )
    }

    private fun moveCameraToLastLocation(locationUpdate: List<Location>) {
        Timber.d("Sticky Camera $cameraMovement")
        when (cameraMovement) {
            CameraMovement.StickyLocation -> {
                locationUpdate.lastOrNull()?.let(::recenterMap)
            }
            CameraMovement.StickyBounds -> {
                val latLngBounds = trackingRouteCameraBounds.build()
                if (latLngBounds != null) {
                    recenterMap(latLngBounds, getCameraViewPortSize())
                } else {
                    locationUpdate.lastOrNull()?.let(::recenterZoomOutMap)
                }
            }
            else -> {
            } // do nothing
        }
    }

    private fun recenterZoomOutMap(location: Location) {
        if (!::mapView.isInitialized) {
            return
        }

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            location.toGmsLatLng(),
            MAX_MAP_ZOOM_LEVEL - 3
        )
        mapView.animateCamera(cameraUpdate)
    }

    private fun recenterMap(location: Location) {
        if (!::mapView.isInitialized) {
            return
        }
        val bounds = LatLngBounds.builder().include(location.toGmsLatLng()).build()
        recenterMap(bounds, getCameraViewPortSize())
    }

    private fun stopTrackingServiceAndFinish() {
        startService(RouteTrackingService.stopIntent(this))
        startActivity(HomeActivity.clearTaskIntent(this))
    }

    private fun onTrackingStatusChanged(@RouteTrackingStatus trackingStatus: Int) {
        updateViews(trackingStatus)
        when (trackingStatus) {
            RESUMED -> setCameraMovementAndUpdateUi(CameraMovement.StickyBounds)
            PAUSED -> setCameraMovementAndUpdateUi(CameraMovement.StickyLocation)
        }
    }

    private fun setCameraMovementAndUpdateUi(expectedMode: CameraMovement) {
        val prevCameraMovement = this.cameraMovement
        this.cameraMovement = expectedMode
        // move right after set mode value
        lifecycleScope.launch {
            val lastLocation = routeTrackingViewModel.getLastLocationFlow().first()
            moveCameraToLastLocation(listOf(lastLocation))
        }
        val cameraButtonState = when (expectedMode) {
            CameraMovement.StickyLocation -> CameraMovement.StickyBounds
            CameraMovement.StickyBounds -> CameraMovement.StickyLocation
            CameraMovement.None -> prevCameraMovement
        }
        if (cameraButtonState != CameraMovement.None) {
            routeTrackingViewModel.stickyCameraButtonState.value = cameraButtonState
        }
    }

    private fun updateViews(@RouteTrackingStatus trackingStatus: Int) {
        when (trackingStatus) {
            RESUMED -> updateViewsOnTrackingResumed()
            PAUSED -> updateViewsOnTrackingPaused()
        }
    }

    private var startPointMarker: Marker? = null
    private fun onTrackingLocationUpdate(batch: List<ActivityLocation>) {
        Timber.tag(LOG_TAG_LOCATION)
            .d("onTrackingLocationUpdate: ${batch.size}")
        addStartPointMarkerIfNotAdded(batch)
        drawTrackingLocationUpdate(batch)
        moveMapCameraOnTrackingLocationUpdate(batch)
    }

    private fun addStartPointMarkerIfNotAdded(batch: List<ActivityLocation>) {
        if (startPointMarker != null || batch.isEmpty()) {
            return
        }
        val startPointLocation = batch.first()
        val startMarkerBitmap = ActivityRouteMapActivity.createDrawableBitmap(
            context = this,
            drawableResId = R.drawable.ic_start_marker
        )
        if (startMarkerBitmap != null) {
            val startMarker = MarkerOptions()
                .position(startPointLocation.toGmsLatLng())
                .icon(BitmapDescriptorFactory.fromBitmap(startMarkerBitmap))
                .anchor(0.5f, 0.5f)
            startPointMarker = mapView.addMarker(startMarker)
        }
    }

    private fun moveMapCameraOnTrackingLocationUpdate(batch: List<ActivityLocation>) {
        batch.forEach {
            trackingRouteCameraBounds.include(it.toGmsLatLng())
        }
    }

    private fun recenterMap(latLngBounds: LatLngBounds, cameraViewPortSize: Size) {
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
            latLngBounds,
            cameraViewPortSize.width,
            cameraViewPortSize.height,
            MAP_LATLNG_BOUND_PADDING
        )

        mapView.animateCamera(cameraUpdate)
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

    private fun drawTrackingLocationUpdate(batch: List<ActivityLocation>) {
        routePolyline?.let { currentPolyline ->
            // This method will take a copy of the points, so further mutations to points will have
            // no effect on this polyline.
            val appendedPolypoints = currentPolyline.points.toMutableList()
            appendedPolypoints.addAll(batch.map { it.toGmsLatLng() })
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
                ::setCameraMovementAndUpdateUi
            )
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
        setCameraMovementAndUpdateUi(CameraMovement.StickyBounds)
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
        val snapshot = suspendCancellableCoroutine<Bitmap?> { continuation ->
            mapView.snapshot { mapSnapshot ->
                continuation.resume(mapSnapshot)
            }
        } ?: return null

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

    @SuppressLint("MissingPermission")
    private fun initMapView(map: GoogleMap) {
        this.mapView = map
        map.apply {
            setMaxZoomPreference(MAX_MAP_ZOOM_LEVEL)
            setOnCameraIdleListener {
                hasMapCameraBeenIdled = true
            }
            setOnCameraMoveStartedListener { reason ->
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    setCameraMovementAndUpdateUi(CameraMovement.None)
                }
            }
            setOnMarkerClickListener { true } // avoid camera movement on marker click event
            isMyLocationEnabled = true
            setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this@RouteTrackingActivity,
                    R.raw.google_map_styles
                )
            )
            uiSettings.apply {
                isMyLocationButtonEnabled = false
                setAllGesturesEnabled(true)
                isZoomControlsEnabled = false
                isCompassEnabled = false
                isIndoorLevelPickerEnabled = false
                isMapToolbarEnabled = false
                isTiltGesturesEnabled = false
            }
        }
    }

    override fun onActivityTypeSelected(activityType: ActivityType) {
        routeTrackingViewModel.onSelectActivityType(activityType)
    }

    enum class CameraMovement {
        /**
         * Camera is being controlled by user.
         */
        None,

        /**
         * Camera is auto sticky to the current location point.
         */
        StickyLocation,

        /**
         * Camera is auto sticky to the current route lat-lng bounds.
         */
        StickyBounds
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

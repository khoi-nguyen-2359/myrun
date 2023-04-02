package akio.apps.myrun.feature.tracking

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.data.location.api.LOG_TAG_LOCATION
import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus.PAUSED
import akio.apps.myrun.data.tracking.api.model.RouteTrackingStatus.RESUMED
import akio.apps.myrun.feature.core.BitmapUtils.createDrawableBitmap
import akio.apps.myrun.feature.core.DialogDelegate
import akio.apps.myrun.feature.core.ktx.collectRepeatOnStarted
import akio.apps.myrun.feature.core.ktx.dp2px
import akio.apps.myrun.feature.core.ktx.lazyViewModelProvider
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegate
import akio.apps.myrun.feature.core.launchcatching.LaunchCatchingDelegateImpl
import akio.apps.myrun.feature.core.measurement.UnitFormatterSetFactory
import akio.apps.myrun.feature.tracking.di.DaggerRouteTrackingFeatureComponent
import akio.apps.myrun.feature.tracking.ui.ActivitySettingsView
import akio.apps.myrun.feature.tracking.ui.RouteTrackingStatsView
import akio.apps.myrun.feature.tracking.ui.StopDialogOptionId
import akio.apps.myrun.feature.tracking.ui.StopOptionsDialog
import akio.apps.myrun.feature.tracking.ui.TrackingControlButtonPanel
import akio.apps.myrun.feature.tracking.ui.TrackingControlButtonType
import akio.apps.myrun.feature.tracking.utils.LatLngBoundsBuilder
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
import androidx.compose.ui.platform.ComposeView
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
    AppCompatActivity(R.layout.activity_route_tracking),
    ActivitySettingsView.EventListener,
    LaunchCatchingDelegate by launchCatchingDelegate {

    // use this flag to check if map has ever been loaded (or never been due to no internet)
    private var hasMapCameraBeenIdled: Boolean = false
    private var isMapCameraMoving: Boolean = false

    // first camera move will instantly jump to the current location, no animation (map first load)
    private var isFirstCameraMoveFinished: Boolean = false

    private val dialogDelegate by lazy { DialogDelegate(this) }

    private val routeTrackingViewModel: RouteTrackingViewModel by lazyViewModelProvider {
        DaggerRouteTrackingFeatureComponent.factory().create(application).routeTrackingViewModel()
    }

    private lateinit var mapView: GoogleMap

    private val locationServiceChecker by lazy {
        LocationServiceChecker(activity = this, RC_LOCATION_SERVICE)
    }

    private var routePolyline: Polyline? = null

    /**
     * LatLngBounds that is used for map camera to zoom into the whole route.
     */
    private val trackingRouteCameraBounds: LatLngBoundsBuilder = LatLngBoundsBuilder()

    private val locationPermissionChecker: LocationPermissionChecker =
        LocationPermissionChecker(activity = this)

    private var cameraMovement: CameraMovement = CameraMovement.StickyLocation

    private val trackingStatsView: RouteTrackingStatsView by lazy {
        findViewById<RouteTrackingStatsView>(R.id.tracking_stats_view).apply {
            lifecycleScope.launch {
                val preferredSystem =
                    routeTrackingViewModel.currentUserPreferences.getMeasureSystemFlow().first()
                trackUnitFormatterSet =
                    UnitFormatterSetFactory.createUnitFormatterSet(preferredSystem)
            }
        }
    }

    private val composableView: ComposeView by lazy { findViewById(R.id.composable_view) }
    private val activitySettingsView: ActivitySettingsView by lazy {
        findViewById(R.id.activity_settings_view)
    }
    private val semiTransparentBackdropView: View by lazy {
        findViewById(R.id.semi_transparent_backdrop_view)
    }
    private val trackingMapView: View by lazy {
        findViewById(R.id.tracking_map_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startRequiredChecks()
        setupEdgeToEdge()

        routeTrackingViewModel.requestInitialData()
        initViews()
        initMap()
    }

    /**
     * Runs required checks on this screen and exits if there's any missing conditions. Ex:
     * check location permissions
     * -> check location service availability
     * -> allow user to use this screen
     */
    private fun startRequiredChecks() = lifecycleScope.launch {
        val requestConfig = routeTrackingViewModel.getLocationRequestConfigFlow().first()
        val missingRequiredPermission =
            !locationPermissionChecker.check() || !locationServiceChecker.check(requestConfig)
        if (missingRequiredPermission) {
            finish()
        }
    }

    private fun setupEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(composableView) { view, windowInsets ->
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
    }

    private fun initObservers() {
        collectRepeatOnStarted(
            routeTrackingViewModel.launchCatchingLoading,
            dialogDelegate::toggleProgressDialog
        )
        collectRepeatOnStarted(
            routeTrackingViewModel.trackingLocationUpdateValue,
            ::onTrackingLocationUpdate
        )
        collectRepeatOnStarted(routeTrackingViewModel.trackingStats, trackingStatsView::update)
        collectRepeatOnStarted(routeTrackingViewModel.trackingStatus, ::onTrackingStatusChanged)
        dialogDelegate.collectLaunchCatchingError(this, routeTrackingViewModel)
        collectRepeatOnStarted(
            routeTrackingViewModel.activityType,
            activitySettingsView::setActivityType
        )
        collectRepeatOnStarted(launchCatchingLoading, dialogDelegate::toggleProgressDialog)
        dialogDelegate.collectLaunchCatchingError(this, this)
        collectRepeatOnStarted(routeTrackingViewModel.locationUpdateFlow) {
            if (!isMapCameraMoving && it.isNotEmpty()) {
                updateStickyCamera(it.last())
            }
        }
    }

    private fun updateStickyCamera(
        lastLocation: Location,
        animate: Boolean = isFirstCameraMoveFinished,
    ) {
        Timber.d("Sticky Camera $cameraMovement")
        when (cameraMovement) {
            CameraMovement.StickyLocation -> {
                recenterMap(lastLocation, animate)
            }
            CameraMovement.StickyBounds -> {
                val latLngBounds = trackingRouteCameraBounds.build()
                if (latLngBounds != null) {
                    recenterMap(latLngBounds, getCameraViewPortSize(), animate)
                } else {
                    recenterZoomOutMap(lastLocation, animate)
                }
            }
            else -> {
            } // do nothing
        }

        isFirstCameraMoveFinished = true
    }

    private fun recenterZoomOutMap(location: Location, animate: Boolean) {
        if (!::mapView.isInitialized) {
            return
        }

        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
            location.toGmsLatLng(),
            MAX_MAP_ZOOM_LEVEL - 3
        )
        if (animate) {
            mapView.animateCamera(cameraUpdate, DEFAULT_CAMERA_ANIMATE_DURATION, null)
        } else {
            mapView.moveCamera(cameraUpdate)
        }
    }

    private fun recenterMap(location: Location, animate: Boolean) {
        if (!::mapView.isInitialized) {
            return
        }
        val bounds = LatLngBounds.builder().include(location.toGmsLatLng()).build()
        recenterMap(bounds, getCameraViewPortSize(), animate)
    }

    private fun stopTrackingServiceAndFinish() {
        startService(RouteTrackingService.stopIntent(this))
        finish()
    }

    private fun onTrackingStatusChanged(@RouteTrackingStatus trackingStatus: Int) {
        updateViews(trackingStatus)
        when (trackingStatus) {
            RESUMED, PAUSED -> setCameraMovementAndUpdateUi(CameraMovement.StickyLocation)
        }
    }

    private fun setCameraMovementAndUpdateUi(
        expectedMode: CameraMovement,
        animate: Boolean = true,
    ) {
        val prevCameraMovement = this.cameraMovement
        this.cameraMovement = expectedMode
        // move right after set mode value
        lifecycleScope.launch {
            val lastLocation = routeTrackingViewModel.getLastLocation()
            updateStickyCamera(lastLocation, animate)
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
        Timber.tag(LOG_TAG_LOCATION).d(
            "[RouteTrackingActivity] onTrackingLocationUpdate: ${batch.size}"
        )
        drawTrackingLocationUpdate(batch)
        moveMapCameraOnTrackingLocationUpdate(batch)
    }

    private suspend fun addStartPointMarkerIfNotAdded() {
        if (startPointMarker != null) {
            return
        }
        val lastLocation = routeTrackingViewModel.getLastLocation()
        val startMarkerBitmap = withContext(Dispatchers.Default) {
            createDrawableBitmap(context = this@RouteTrackingActivity, R.drawable.ic_start_marker)
        }
        if (startMarkerBitmap != null) {
            val startMarker = MarkerOptions()
                .position(lastLocation.toGmsLatLng())
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

    private fun recenterMap(
        latLngBounds: LatLngBounds,
        cameraViewPortSize: Size,
        animate: Boolean,
    ) {
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(
            latLngBounds,
            cameraViewPortSize.width,
            cameraViewPortSize.height,
            MAP_LATLNG_BOUND_PADDING
        )

        if (animate) {
            mapView.animateCamera(cameraUpdate, DEFAULT_CAMERA_ANIMATE_DURATION, null)
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

    /**
     * Keep a batch of location update up to a threshold before drawing the batch on the map.
     * This accumulation is to have enough points for bsplines converting.
     */
    private val locationUpdateBatch: MutableList<ActivityLocation> = mutableListOf()
    private fun drawTrackingLocationUpdate(batch: List<ActivityLocation>) {
        locationUpdateBatch.addAll(batch)
        if (routePolyline == null &&
            locationUpdateBatch.size >= LOCATION_UPDATE_FIRST_BATCH_THRESHOLD
        ) {
            val polyline = PolylineOptions()
                .addAll(convertToBSpline(locationUpdateBatch.map { LatLng(it.latitude, it.longitude) }))
                .jointType(JointType.ROUND)
                .startCap(RoundCap())
                .endCap(RoundCap())
                .color(ContextCompat.getColor(this, R.color.route_tracking_polyline))
                .width(3.dp2px)
            routePolyline = mapView.addPolyline(polyline)
            locationUpdateBatch.clear()
        } else if (routePolyline != null &&
            locationUpdateBatch.size >= LOCATION_UPDATE_BATCH_THRESHOLD
        ) {
            // This takes a copy of the points, so further mutations to points will have
            // no effect on this polyline.
            val appendedPolypoints = routePolyline?.points?.toMutableList() ?: mutableListOf()
            appendedPolypoints.addAll(
                convertToBSpline(
                    appendedPolypoints.takeLast(2) + locationUpdateBatch.map { it.toGmsLatLng() }
                )
            )
            routePolyline?.points = appendedPolypoints
            locationUpdateBatch.clear()
        }
    }

    @Suppress("ktlint:max-line-length", "ktlint:op-spacing")
    private fun convertToBSpline(points: List<LatLng>): List<LatLng> {
        if (points.size < 4) {
            return points
        }
        return points.flatMapIndexed { i: Int, _ ->
            if (i < 2 || i > points.size - 2) {
                return@flatMapIndexed emptyList()
            }
            var t = 0.0
            val converted = mutableListOf<LatLng>()
            while (t < 1.0) {
                val ax =
                    (-points[i - 2].latitude + 3 * points[i - 1].latitude - 3 * points[i].latitude + points[i + 1].latitude) / 6
                val ay =
                    (-points[i - 2].longitude + 3 * points[i - 1].longitude - 3 * points[i].longitude + points[i + 1].longitude) / 6
                val bx =
                    (points[i - 2].latitude - 2 * points[i - 1].latitude + points[i].latitude) / 2
                val by =
                    (points[i - 2].longitude - 2 * points[i - 1].longitude + points[i].longitude) / 2
                val cx = (-points[i - 2].latitude + points[i].latitude) / 2
                val cy = (-points[i - 2].longitude + points[i].longitude) / 2
                val dx =
                    (points[i - 2].latitude + 4 * points[i - 1].latitude + points[i].latitude) / 6
                val dy =
                    (points[i - 2].longitude + 4 * points[i - 1].longitude + points[i].longitude) / 6
                converted.add(
                    LatLng(
                        ax * Math.pow(t + 0.1, 3.0) + bx * Math.pow(t + 0.1, 2.0) + cx * (t + 0.1) + dx,
                        ay * Math.pow(t + 0.1, 3.0) + by * Math.pow(t + 0.1, 2.0) + cy * (t + 0.1) + dy
                    )
                )

                t += 0.1
            }
            converted
        }.also {
            Timber.d("khoi bspline converted=${it.size}")
        }
    }

    private fun initViews() {
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
        activitySettingsView.visibility = View.GONE
        trackingStatsView.visibility = View.VISIBLE
        semiTransparentBackdropView.visibility = View.VISIBLE
    }

    private fun updateViewsOnTrackingResumed() {
        activitySettingsView.visibility = View.GONE
        trackingStatsView.visibility = View.VISIBLE
        semiTransparentBackdropView.visibility = View.VISIBLE
    }

    private fun startRouteTracking() {
        ContextCompat.startForegroundService(this, RouteTrackingService.startIntent(this))
        routeTrackingViewModel.startDataUpdates()
        lifecycleScope.launch { addStartPointMarkerIfNotAdded() }
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
        setCameraMovementAndUpdateUi(CameraMovement.StickyBounds, animate = false)
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
        if (!hasMapCameraBeenIdled) {
            return null
        }
        val snapshot = suspendCancellableCoroutine<Bitmap?> { continuation ->
            mapView.snapshot { mapSnapshot ->
                continuation.resume(mapSnapshot)
            }
        }
            ?: return null

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
        val mapViewBitmap = trackingMapView.drawToBitmap()
        val mapProjection = mapView.projection
        val mapPolypoints = routePolyline?.points
            ?: emptyList()
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

    @SuppressLint("MissingPermission", "PotentialBehaviorOverride")
    private fun initMapView(map: GoogleMap) {
        this.mapView = map.apply {
            setMaxZoomPreference(MAX_MAP_ZOOM_LEVEL)
            setOnCameraIdleListener {
                hasMapCameraBeenIdled = true
                isMapCameraMoving = false
            }
            setOnCameraMoveStartedListener { reason ->
                if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    setCameraMovementAndUpdateUi(CameraMovement.None, false)
                }
                isMapCameraMoving = true
            }
            setOnMarkerClickListener { true } // avoid camera movement on marker click event
            isMyLocationEnabled = true
            isBuildingsEnabled = false
            isIndoorEnabled = false
            isTrafficEnabled = false
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

        loadMapStyles()
    }

    private fun loadMapStyles() = lifecycleScope.launch {
        val stylesContent = withContext(Dispatchers.IO) {
            val stylesContentStream = resources.openRawResource(R.raw.google_map_styles)
            stylesContentStream.reader().use {
                it.readText()
            }
        }
        mapView.setMapStyle(MapStyleOptions(stylesContent))
    }

    override fun onActivityTypeSelected(activityType: ActivityType) {
        routeTrackingViewModel.onSelectActivityType(activityType)
    }

    private fun Location.toGmsLatLng(): LatLng = LatLng(latitude, longitude)

    private fun ActivityLocation.toGmsLatLng(): LatLng = LatLng(latitude, longitude)

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
        StickyBounds,
    }

    companion object {
        private const val DEFAULT_CAMERA_ANIMATE_DURATION = 1000
        private val MAP_LATLNG_BOUND_PADDING = 30.dp2px.toInt()
        private const val MAX_MAP_ZOOM_LEVEL = 19f // 20 = buildings level

        private const val LOCATION_UPDATE_FIRST_BATCH_THRESHOLD = 4
        private const val LOCATION_UPDATE_BATCH_THRESHOLD = 2

        const val ROUTE_IMAGE_RATIO = 1.7f

        const val RC_LOCATION_SERVICE = 1

        fun launchIntent(context: Context): Intent {
            val intent = Intent(context, RouteTrackingActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}

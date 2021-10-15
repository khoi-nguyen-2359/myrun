package akio.apps.myrun.feature.route.ui

import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.feature.base.BitmapUtils
import akio.apps.myrun.feature.base.DialogDelegate
import akio.apps.myrun.feature.base.ext.dp2px
import akio.apps.myrun.feature.base.ext.extra
import akio.apps.myrun.feature.base.lifecycle.collectEventRepeatOnStarted
import akio.apps.myrun.feature.base.lifecycle.collectRepeatOnStarted
import akio.apps.myrun.feature.base.map.GmsLatLng
import akio.apps.myrun.feature.base.map.toGmsLatLng
import akio.apps.myrun.feature.base.map.toLatLng
import akio.apps.myrun.feature.base.viewmodel.lazySavedStateViewModelProvider
import akio.apps.myrun.feature.route.R
import akio.apps.myrun.feature.route.RoutePlanningViewModel
import akio.apps.myrun.feature.route.model.RoutePlottingState
import akio.apps.myrun.feature.route.wiring.DaggerRoutePlanningFeatureComponent
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Region
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.material.appbar.MaterialToolbar
import com.google.maps.android.PolyUtil
import kotlin.coroutines.resume
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class RoutePlanningActivityObsolete :
    AppCompatActivity(R.layout.activity_draw_route_obsolete),
    GoogleMap.OnMarkerDragListener,
    GoogleMap.OnMapClickListener,
    RoutePaintingView.EventListener,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnPolylineClickListener,
    GoogleMap.OnMapLongClickListener,
    GoogleMap.OnInfoWindowClickListener,
    GoogleMap.OnCameraIdleListener {

    private val saveButton: View by lazy { findViewById(R.id.save_button) }
    private val drawButton: View by lazy { findViewById(R.id.draw_button) }
    private val undoButton: View by lazy { findViewById(R.id.undo_button) }
    private val redoButton: View by lazy { findViewById(R.id.redo_button) }
    private val eraseButton: View by lazy { findViewById(R.id.erase_button) }
    private val routePaintingView: RoutePaintingView by lazy {
        findViewById(R.id.route_painting_view)
    }
    private val topBar: MaterialToolbar by lazy { findViewById(R.id.topbar) }

    private var directionPolyline: Polyline? = null
    private val directionMarkers = mutableListOf<Marker>()
    private lateinit var map: GoogleMap

    private val viewModel: RoutePlanningViewModel by lazySavedStateViewModelProvider(
        savedStateOwner = this
    ) { savedStateHandle ->
        val routeId: String? = extra(EXT_ROUTE_ID, null)
        RoutePlanningViewModel.saveArguments(savedStateHandle, routeId)
        DaggerRoutePlanningFeatureComponent.factory().create(savedStateHandle).drawRouteViewModel()
    }

    private val dialogDelegate: DialogDelegate = DialogDelegate(this)

    private val waypointMarkerIconBitmap: Bitmap? by lazy {
        BitmapUtils.createDrawableBitmap(this, R.drawable.ic_route_plotting_coordinate_dot)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initMap()
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun initMap() = lifecycleScope.launch {
        map = suspendCancellableCoroutine { continuation ->
            (supportFragmentManager.findFragmentById(R.id.draw_route_map) as? SupportMapFragment)
                ?.getMapAsync { googleMap ->
                    runOnUiThread {
                        continuation.resume(googleMap)
                    }
                }
        }

        map.setOnMarkerDragListener(this@RoutePlanningActivityObsolete)
        map.setOnMapClickListener(this@RoutePlanningActivityObsolete)
        map.setOnMarkerClickListener(this@RoutePlanningActivityObsolete)
        map.setOnPolylineClickListener(this@RoutePlanningActivityObsolete)
        map.setOnMapLongClickListener(this@RoutePlanningActivityObsolete)
        map.setOnInfoWindowClickListener(this@RoutePlanningActivityObsolete)
        map.setOnCameraIdleListener(this@RoutePlanningActivityObsolete)

        map.uiSettings.isMyLocationButtonEnabled = false

        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(p0: Marker): View? {
                return null
            }

            override fun getInfoWindow(p0: Marker): View {
                return LayoutInflater.from(this@RoutePlanningActivityObsolete)
                    .inflate(R.layout.create_route_delete_waypoint_info_window, null)
            }
        })

        map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                this@RoutePlanningActivityObsolete,
                R.raw.google_map_styles
            )
        )

        initMapCamera(map)
        initObservers()
    }

    private fun initObservers() {
        collectRepeatOnStarted(viewModel.screenState) { screenState ->
            when (screenState) {
                is RoutePlanningViewModel.ScreenState.RoutePlotting -> {
                    drawDirectionResult(screenState.plottingState.getCurrentState())
                    updateStateControlButtons(screenState.plottingState)
                }
            }
        }

        collectEventRepeatOnStarted(
            viewModel.launchCatchingError,
            dialogDelegate::showExceptionAlert
        )

        collectRepeatOnStarted(
            viewModel.isLaunchCatchingInProgress,
            dialogDelegate::toggleProgressDialog
        )

        collectRepeatOnStarted(viewModel.routeDrawingMode) {
            startReviewing() // call this to reset button state
            when (it) {
                RoutePlanningViewModel.RouteDrawingMode.Draw -> startDrawing()
                RoutePlanningViewModel.RouteDrawingMode.Erase -> startErasing()
                RoutePlanningViewModel.RouteDrawingMode.Review -> {
                    // already done
                }
            }
        }
    }

    private fun initViews() {
        routePaintingView.eventListener = this@RoutePlanningActivityObsolete
        topBar.setNavigationOnClickListener { finish() }

        saveButton.setOnClickListener { openSelectCheckpoint() }

        drawButton.setOnClickListener {
            viewModel.toggleDrawing()
        }

        undoButton.isEnabled = false
        undoButton.setOnClickListener { viewModel.rewindDirectionState() }

        redoButton.isEnabled = false
        redoButton.setOnClickListener { viewModel.forwardDirectionState() }

        eraseButton.setOnClickListener {
            viewModel.toggleErasing()
        }
    }

    private fun openSelectCheckpoint() {
        if (directionMarkers.isEmpty()) {
            return
        }

//        // TODO: later
//        resultRoute.waypoints = directionMarkers.map { it.position }.toLatLongList()
//        val intent = SelectCheckpointActivity.launchIntent(this, resultRoute)
//        startActivity(intent)
    }

    private fun clearDirections() {
        map.clear()
        directionMarkers.clear()
        directionPolyline = null
    }

    private fun startErasing() {
        routePaintingView.mode = RoutePaintingView.Mode.Erase
        drawButton.isSelected = false
        eraseButton.isSelected = true
    }

    private fun startReviewing() {
        routePaintingView.mode = RoutePaintingView.Mode.Review
        drawButton.isSelected = false
        eraseButton.isSelected = false
    }

    private fun startDrawing() {
        routePaintingView.mode = RoutePaintingView.Mode.Draw
        drawButton.isSelected = true
        eraseButton.isSelected = false
    }

    private fun initMapCamera(map: GoogleMap) {
        lifecycleScope.launch {
            val initMapViewBounds = viewModel.getInitialMapViewBoundCoordinates() ?: return@launch
            val displayMetrics = application.resources.displayMetrics
            map.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    initMapViewBounds,
                    displayMetrics.widthPixels,
                    displayMetrics.heightPixels,
                    50.dp2px.toInt()
                )
            )
        }
    }

    override fun onFinishRouteDrawing(routePoints: List<PointF>) {
        if (routePoints.size < 2) {
            return
        }

        val drawnWaypoints = routePoints.map {
            val intPoint = Point(it.x.toInt(), it.y.toInt())
            map.projection.fromScreenLocation(intPoint)
        }
            .map { LatLng(it.latitude, it.longitude) }

        viewModel.plotRoute(drawnWaypoints)
    }

    private fun updateStateControlButtons(stateManagerInfo: RoutePlottingState) {
        undoButton.isEnabled = stateManagerInfo.currentIndex > 0
        redoButton.isEnabled = stateManagerInfo.currentIndex < stateManagerInfo.stateData.size - 1
    }

    private fun drawDirectionResult(polylineLatLongs: List<LatLng>) {
        val polylineMapLatLngs = polylineLatLongs.map { it.toGmsLatLng() }
        clearDirections()
        directionPolyline = map.addPolyline(
            createPolyineOptions(this, polylineMapLatLngs)
        )

        polylineMapLatLngs.forEach {
            val waypointMarker = createWaypointMarker(it)

            map.addMarker(waypointMarker)?.let(directionMarkers::add)
        }
    }

    private fun createWaypointMarker(position: GmsLatLng): MarkerOptions {
        return MarkerOptions()
            .position(position)
            .apply {
                waypointMarkerIconBitmap?.let {
                    icon(BitmapDescriptorFactory.fromBitmap(it))
                }
            }
            .draggable(true)
            .anchor(0.5f, 0.5f)
            .visible(map.cameraPosition.zoom >= MAP_DEFAULT_ZOOM_LEVEL)
    }

    override fun onMarkerDragEnd(marker: Marker) {
        directionPolyline?.let {
            viewModel.recordDirectionState(it.points.map { it.toLatLng() })
        }
    }

    override fun onMarkerDragStart(marker: Marker) {
    }

    override fun onMarkerDrag(marker: Marker) {
        updateMapPolyline(marker)
    }

    private fun updateMapPolyline(marker: Marker) {
        val markerIndex = directionMarkers.indexOf(marker)
        if (markerIndex == -1) {
            return
        }

        directionPolyline?.let {
            val polylinePoints = it.points
            polylinePoints[markerIndex] = marker.position
            it.points = polylinePoints
        }
    }

    override fun onMapClick(clickPoint: GmsLatLng) {
    }

    private fun addWaypoint(clickPoint: LatLng) {
        val searchPath = directionMarkers.map { it.position }.toMutableList()

        if (searchPath.size >= 2) {
            var boundaryEndpoint = getBoundaryWaypoint(searchPath[0], searchPath[1])
            searchPath.add(0, boundaryEndpoint)

            boundaryEndpoint =
                getBoundaryWaypoint(searchPath.last(), searchPath[searchPath.size - 2])
            searchPath.add(boundaryEndpoint)
        }

        var insertIndex = findWaypointInsertIndex(searchPath, clickPoint.toGmsLatLng())

        if (insertIndex != -1) {
            directionPolyline?.let {
                if (searchPath.size != directionMarkers.size) {
                    insertIndex -= 1 // count the fake waypoint
                }

                val polylinePoints = it.points
                polylinePoints.add(insertIndex, clickPoint.toGmsLatLng())
                it.points = polylinePoints

                val marker = map.addMarker(
                    createWaypointMarker(clickPoint.toGmsLatLng())
                )
                if (marker != null) {
                    directionMarkers.add(insertIndex, marker)
                }
            }
        } else {
            drawDirectionResult(listOf(clickPoint))
        }

        directionPolyline?.let {
            viewModel.recordDirectionState(it.points.map { it.toLatLng() })
        }
    }

    override fun onFinishRouteErasing(erasingRegion: Region) {
        startReviewing()
        if (directionMarkers.isEmpty()) {
            return
        }

        directionPolyline?.let {
            val polylinePoints = it.points
            val oldSize = polylinePoints.size
            directionMarkers.filterIndexed { _, marker ->
                val screenPosition = map.projection.toScreenLocation(marker.position)
                erasingRegion.contains(screenPosition.x, screenPosition.y)
            }.forEach { marker ->
                marker.remove()
                directionMarkers.remove(marker)
                polylinePoints.removeAll { it == marker.position }
            }

            it.points = polylinePoints

            if (polylinePoints.size < oldSize) {
                viewModel.recordDirectionState(it.points.map { it.toLatLng() })
            }
        }
    }

    override fun onMarkerClick(clickMarker: Marker): Boolean {
        clickMarker.showInfoWindow()
        return true
    }

    override fun onPolylineClick(p0lyline: Polyline) {
    }

    override fun onMapLongClick(clickPoint: GmsLatLng) {
        addWaypoint(clickPoint.toLatLng())
    }

    override fun onInfoWindowClick(clickMarker: Marker) {
        directionMarkers.indexOf(clickMarker).let { foundIndex ->
            directionMarkers.removeAt(foundIndex)
            directionPolyline?.let {
                val polylinePoints = it.points
                polylinePoints.removeAt(foundIndex)
                it.points = polylinePoints

                viewModel.recordDirectionState(it.points.map { it.toLatLng() })
            }
        }

        clickMarker.remove()
    }

    override fun onCameraIdle() {
        directionMarkers.forEach {
            it.isVisible = map.cameraPosition.zoom >= MAP_DEFAULT_ZOOM_LEVEL
        }
    }

    private fun createPolyineOptions(
        context: Context,
        waypoints: List<GmsLatLng>,
    ): PolylineOptions {
        return PolylineOptions()
            .addAll(waypoints)
            .jointType(JointType.ROUND)
            .startCap(RoundCap())
            .endCap(RoundCap())
            .color(ContextCompat.getColor(context, R.color.route_painting))
            .width(4.dp2px)
    }

    fun getBoundaryWaypoint(endPoint: GmsLatLng, secondPoint: GmsLatLng): GmsLatLng {
        val a = (endPoint.latitude - secondPoint.latitude) /
            (endPoint.longitude - secondPoint.longitude)
        val b = endPoint.latitude - a * endPoint.longitude
        return if (endPoint.longitude <= secondPoint.longitude &&
            endPoint.latitude >= secondPoint.latitude
        ) {
            if (endPoint.longitude == secondPoint.longitude) {
                GmsLatLng(endPoint.latitude + 0.1, endPoint.longitude)
            } else {
                val lng = endPoint.longitude - 1
                GmsLatLng(a * lng + b, lng)
            }
        } else if (endPoint.longitude >= secondPoint.longitude &&
            endPoint.latitude >= secondPoint.latitude
        ) {
            if (endPoint.longitude == secondPoint.longitude) {
                GmsLatLng(endPoint.latitude + 0.1, endPoint.longitude)
            } else {
                val lng = endPoint.longitude + 1
                GmsLatLng(a * lng + b, lng)
            }
        } else if (endPoint.longitude >= secondPoint.longitude &&
            endPoint.latitude <= secondPoint.latitude
        ) {
            if (endPoint.longitude == secondPoint.longitude) {
                GmsLatLng(endPoint.latitude - 0.1, endPoint.longitude)
            } else {
                val lng = endPoint.longitude + 1
                GmsLatLng(a * lng + b, lng)
            }
        } else {
            if (endPoint.longitude == secondPoint.longitude) {
                GmsLatLng(endPoint.latitude - 0.1, endPoint.longitude)
            } else {
                val lng = endPoint.longitude - 1
                GmsLatLng(a * lng + b, lng)
            }
        }
    }

    private fun findWaypointInsertIndex(waypoints: List<GmsLatLng>, insertPoint: GmsLatLng): Int {
        if (waypoints.isEmpty()) {
            return -1
        }

        var insertIndex = 1
        var minDistance = Double.MAX_VALUE

        for (index in 1 until waypoints.size) {
            val distance =
                PolyUtil.distanceToLine(insertPoint, waypoints[index - 1], waypoints[index])
            if (distance < minDistance) {
                minDistance = distance
                insertIndex = index
            }
        }

        return insertIndex
    }

    companion object {
        private const val EXT_ROUTE_ID = "EXT_ROUTE_ID"
        private const val MAP_DEFAULT_ZOOM_LEVEL = 18f

        fun addNewRouteIntent(context: Context): Intent =
            Intent(context, RoutePlanningActivityObsolete::class.java)

        fun editRouteIntent(context: Context, routeId: String): Intent {
            val intent = Intent(context, RoutePlanningActivityObsolete::class.java)
            intent.putExtra(EXT_ROUTE_ID, routeId)
            return intent
        }
    }
}

package akio.apps.myrun.feature.route.ui

import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.feature.base.DialogDelegate
import akio.apps.myrun.feature.base.lifecycle.collectEventRepeatOnStarted
import akio.apps.myrun.feature.base.lifecycle.collectRepeatOnStarted
import akio.apps.myrun.feature.base.lifecycle.observe
import akio.apps.myrun.feature.base.permissions.AppPermissions
import akio.apps.myrun.feature.base.ui.dp2px
import akio.apps.myrun.feature.base.viewmodel.lazyViewModelProvider
import akio.apps.myrun.feature.route.DrawRouteViewModel
import akio.apps.myrun.feature.route.R
import akio.apps.myrun.feature.route._di.DaggerRouteFeatureComponent
import akio.apps.myrun.feature.route.model.DirectionEditingState
import akio.apps.myrun.feature.route.model.Route
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Region
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.material.appbar.MaterialToolbar
import com.google.maps.android.PolyUtil

private typealias GmsLatLng = com.google.android.gms.maps.model.LatLng

class DrawRouteActivity :
    AppCompatActivity(R.layout.activity_draw_route),
    OnMapReadyCallback,
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

    private val resultRoute by lazy { intent.getParcelableExtra(EXT_ROUTE) as? Route? ?: Route() }

    private val initMapViewBounds by lazy {
        intent.getParcelableExtra(EXT_INIT_VIEW) as? LatLngBounds?
    }

    private val isEditMode by lazy { intent.getBooleanExtra(EXT_IS_EDIT_MODE, false) }

    private val viewModel: DrawRouteViewModel by lazyViewModelProvider {
        DaggerRouteFeatureComponent.factory().create().drawRouteViewModel()
    }

    private val dialogDelegate: DialogDelegate = DialogDelegate(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initObservers()

        (supportFragmentManager.findFragmentById(R.id.draw_route_map) as? SupportMapFragment)
            ?.getMapAsync(this)
    }

    private fun initObservers() {
        observe(viewModel.routeWaypoints) { waypoints ->
            drawDirectionResult(waypoints)
        }

        observe(viewModel.directionStateInfo) { stateInfo ->
            updateStateControlButtons(stateInfo)
        }

        collectEventRepeatOnStarted(
            viewModel.launchCatchingError,
            dialogDelegate::showExceptionAlert
        )

        collectRepeatOnStarted(
            viewModel.isLaunchCatchingInProgress,
            dialogDelegate::toggleProgressDialog
        )

        observe(viewModel.routeDrawingMode) {
            startReviewing() // call this to reset button state
            when (it) {
                DrawRouteViewModel.RouteDrawingMode.Draw -> startDrawing()
                DrawRouteViewModel.RouteDrawingMode.Erase -> startErasing()
                DrawRouteViewModel.RouteDrawingMode.Review -> {
                    // already done
                }
            }
        }
    }

    private fun initViews() {
        routePaintingView.eventListener = this@DrawRouteActivity
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

    override fun onMapReady(map: GoogleMap) {
        this.map = map
        this.map.setOnMarkerDragListener(this)
        this.map.setOnMapClickListener(this)
        this.map.setOnMarkerClickListener(this)
        this.map.setOnPolylineClickListener(this)
        this.map.setOnMapLongClickListener(this)
        this.map.setOnInfoWindowClickListener(this)
        this.map.setOnCameraIdleListener(this)

        initMapViewBounds?.let {
            val displayMetrics = application.resources.displayMetrics
            map.moveCamera(
                CameraUpdateFactory.newLatLngBounds(
                    it,
                    displayMetrics.widthPixels,
                    displayMetrics.heightPixels,
                    50.dp2px.toInt()
                )
            )
        }

        this.map.uiSettings.isMyLocationButtonEnabled = false

        this.map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(p0: Marker): View? {
                return null
            }

            override fun getInfoWindow(p0: Marker): View {
                return LayoutInflater.from(this@DrawRouteActivity)
                    .inflate(R.layout.create_route_delete_waypoint_info_window, null)
            }
        })

        // draw route data
        if (isEditMode) {
            // TODO: later
            viewModel.initDirectionData(resultRoute.waypoints)
        }

        if (AppPermissions.locationPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PERMISSION_GRANTED
        }
        ) {
            observe(viewModel.mapInitLocation) { location ->
                this.map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        location.toGmsLatLng(),
                        MAP_DEFAULT_ZOOM_LEVEL
                    )
                )
            }
            viewModel.fetchLastLocation()
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

    private fun updateStateControlButtons(stateInfo: DirectionEditingState.DirectionStateInfo) {
        undoButton.isEnabled = stateInfo.currentState > 0
        redoButton.isEnabled = stateInfo.currentState < stateInfo.stateListSize - 1
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
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.waypoint_pin))
            .draggable(true)
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

    fun createPolyineOptions(
        context: Context,
        waypoints: List<GmsLatLng>,
    ): PolylineOptions {
        return PolylineOptions()
            .addAll(waypoints)
            .jointType(JointType.ROUND)
            .startCap(RoundCap())
            .endCap(RoundCap())
            .color(ContextCompat.getColor(context, R.color.route_painting))
            .width(5.dp2px)
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

    fun findWaypointInsertIndex(waypoints: List<GmsLatLng>, insertPoint: GmsLatLng): Int {
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

    private fun Location.toGmsLatLng(): GmsLatLng = GmsLatLng(latitude, longitude)
    private fun LatLng.toGmsLatLng(): GmsLatLng = GmsLatLng(latitude, longitude)
    private fun GmsLatLng.toLatLng(): LatLng = LatLng(latitude, longitude)

    companion object {
        const val EXT_INIT_VIEW = "EXT_INIT_VIEW"
        const val EXT_ROUTE = "EXT_ROUTE"
        const val EXT_IS_EDIT_MODE = "EXT_IS_EDIT_MODE"
        private const val MAP_DEFAULT_ZOOM_LEVEL = 18f

        fun addNewRouteIntent(context: Context, initMapViewBounds: LatLngBounds? = null): Intent {
            val intent = Intent(context, DrawRouteActivity::class.java)
            intent.putExtra(EXT_INIT_VIEW, initMapViewBounds)
            intent.putExtra(EXT_IS_EDIT_MODE, false)
            return intent
        }

        fun editRouteIntent(context: Context, route: Route): Intent {
            val intent = Intent(context, DrawRouteActivity::class.java)
            intent.putExtra(EXT_ROUTE, route)
            intent.putExtra(EXT_IS_EDIT_MODE, true)
            return intent
        }
    }
}

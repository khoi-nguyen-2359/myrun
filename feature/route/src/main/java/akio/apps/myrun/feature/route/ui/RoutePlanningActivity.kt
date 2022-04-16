package akio.apps.myrun.feature.route.ui

import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.route.DaggerRouteDataComponent
import akio.apps.myrun.feature.base.BitmapUtils
import akio.apps.myrun.feature.base.DialogDelegate
import akio.apps.myrun.feature.base.ktx.collectEventRepeatOnStarted
import akio.apps.myrun.feature.base.ktx.collectRepeatOnStarted
import akio.apps.myrun.feature.base.ktx.dp2px
import akio.apps.myrun.feature.base.ktx.extra
import akio.apps.myrun.feature.base.ktx.getColorCompat
import akio.apps.myrun.feature.base.ktx.lazyViewModelProvider
import akio.apps.myrun.feature.route.R
import akio.apps.myrun.feature.route.RoutePlanningViewModel
import akio.apps.myrun.feature.route.di.DaggerRoutePlanningFeatureComponent
import akio.apps.myrun.feature.route.model.RoutePlottingState
import akio.apps.myrun.feature.route.model.toLatLng
import akio.apps.myrun.feature.route.model.toPoint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Region
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlinx.coroutines.launch

class RoutePlanningActivity :
    AppCompatActivity(R.layout.activity_draw_route),
    RoutePaintingView.EventListener {

    private val saveButton: View by lazy { findViewById(R.id.save_button) }
    private val drawButton: View by lazy { findViewById(R.id.draw_button) }
    private val undoButton: View by lazy { findViewById(R.id.undo_button) }
    private val redoButton: View by lazy { findViewById(R.id.redo_button) }
    private val eraseButton: View by lazy { findViewById(R.id.erase_button) }
    private val mapView: MapView by lazy { findViewById(R.id.map_view) }

    private var routeLineSourceData: LineString = LineString.fromLngLats(emptyList())

    private val routePaintingView: RoutePaintingView by lazy {
        findViewById(R.id.route_painting_view)
    }
    private val topBar: MaterialToolbar by lazy { findViewById(R.id.topbar) }

    private val viewModel: RoutePlanningViewModel by lazyViewModelProvider { savedStateHandle ->
        val routeId: String? = extra(EXT_ROUTE_ID, null)
        RoutePlanningViewModel.saveArguments(savedStateHandle, routeId)
        DaggerRoutePlanningFeatureComponent.factory()
            .create(application, savedStateHandle, DaggerRouteDataComponent.create())
            .drawRouteViewModel()
    }

    private val dialogDelegate: DialogDelegate = DialogDelegate(this)

    private val routeCoordinateMarkerBitmap: Bitmap by lazy {
        BitmapUtils.createDrawableBitmap(this, R.drawable.ic_route_plotting_coordinate_dot)
            ?: Bitmap.createBitmap(
                1 /* width*/,
                1 /* height */,
                Bitmap.Config.ARGB_8888
            )
    }

    private fun createRouteCoordinateMarkerBitmap(): Bitmap {
        return BitmapUtils.createDrawableBitmap(this, R.drawable.ic_route_plotting_coordinate_dot)
            ?: Bitmap.createBitmap(
                1 /* width*/,
                1 /* height */,
                Bitmap.Config.ARGB_8888
            )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initMap()
    }

    private fun initMap() = mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
        mapView.location.updateSettings {
            enabled = false
        }
        mapView.scalebar.enabled = false

        initMapCamera()
        initObservers()

//        map.setOnMarkerDragListener(this@RoutePlanningActivity)
//        map.setOnMapClickListener(this@RoutePlanningActivity)
//        map.setOnMarkerClickListener(this@RoutePlanningActivity)
//        map.setOnPolylineClickListener(this@RoutePlanningActivity)
//        map.setOnMapLongClickListener(this@RoutePlanningActivity)
//        map.setOnInfoWindowClickListener(this@RoutePlanningActivity)
//        map.setOnCameraIdleListener(this@RoutePlanningActivity)
//
//        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
//            override fun getInfoContents(p0: Marker): View? {
//                return null
//            }
//
//            override fun getInfoWindow(p0: Marker): View {
//                return LayoutInflater.from(this@RoutePlanningActivity)
//                    .inflate(R.layout.create_route_delete_waypoint_info_window, null)
//            }
//        })
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
        routePaintingView.eventListener = this@RoutePlanningActivity
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
//        if (directionMarkers.isEmpty()) {
//            return
//        }

//        // TODO: later
//        resultRoute.waypoints = directionMarkers.map { it.position }.toLatLongList()
//        val intent = SelectCheckpointActivity.launchIntent(this, resultRoute)
//        startActivity(intent)
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

    private fun initMapCamera() = lifecycleScope.launch {
        val map = mapView.getMapboxMap()
        val boundCoordinates = viewModel.getInitialMapViewBoundCoordinates()
        val boundPadding = 50.dp2px.toDouble()
        val cameraOptions = map.cameraForCoordinates(
            boundCoordinates.map { it.toPoint() },
            padding = EdgeInsets(boundPadding, boundPadding, boundPadding, boundPadding)
        )
        map.setCamera(cameraOptions)
    }

    override fun onFinishRouteDrawing(routePoints: List<PointF>) {
        // TODO: handle size == 1
        if (routePoints.size < 2) {
            return
        }

        val map = mapView.getMapboxMap()
        val mapLatLngs = routePoints.map { onScreenPoint ->
            map.coordinateForPixel(
                ScreenCoordinate(onScreenPoint.x.toDouble(), onScreenPoint.y.toDouble())
            )
                .toLatLng()
        }

        viewModel.plotRoute(mapLatLngs)
    }

    private fun updateStateControlButtons(stateManagerInfo: RoutePlottingState) {
        undoButton.isEnabled = stateManagerInfo.currentIndex > 0
        redoButton.isEnabled = stateManagerInfo.currentIndex < stateManagerInfo.stateData.size - 1
    }

    private fun drawDirectionResult(polylineLatLngs: List<LatLng>) {
        val polylinePoints = polylineLatLngs.map { it.toPoint() }

        val mapStyle = mapView.getMapboxMap().getStyle()
            ?: return
        mapStyle.removeStyleLayer(MAP_ROUTE_LINE_LAYER_ID)
        mapStyle.removeStyleSource(MAP_ROUTE_LINE_SOURCE_ID)
        mapStyle.addSource(
            geoJsonSource(MAP_ROUTE_LINE_SOURCE_ID) {
                routeLineSourceData = LineString.fromLngLats(polylinePoints)
                feature(Feature.fromGeometry(routeLineSourceData))
            }
        )
        mapStyle.addLayer(
            lineLayer(MAP_ROUTE_LINE_LAYER_ID, MAP_ROUTE_LINE_SOURCE_ID) {
                lineDasharray(listOf(0.01, 1.5))
                lineCap(LineCap.ROUND)
                lineJoin(LineJoin.ROUND)
                lineWidth(3.dp2px.toDouble())
                lineColor(getColorCompat(R.color.route_painting))
            }
        )
    }

    private fun createMarker(mapBoxPoint: Point): PointAnnotationOptions = PointAnnotationOptions()
        .withPoint(mapBoxPoint)
        .withIconImage(routeCoordinateMarkerBitmap)
        .withDraggable(true)
        .withIconAnchor(IconAnchor.CENTER)

//    override fun onMarkerDragEnd(marker: Marker) {
//        directionPolyline?.let {
//            viewModel.recordDirectionState(it.points.map { it.toLatLng() })
//        }
//    }
//
//    override fun onMarkerDragStart(marker: Marker) {
//    }
//
//    override fun onMarkerDrag(marker: Marker) {
//        updateMapPolyline(marker)
//    }
//
//    private fun updateMapPolyline(marker: Marker) {
//        val markerIndex = directionMarkers.indexOf(marker)
//        if (markerIndex == -1) {
//            return
//        }
//
//        directionPolyline?.let {
//            val polylinePoints = it.points
//            polylinePoints[markerIndex] = marker.position
//            it.points = polylinePoints
//        }
//    }
//
//    override fun onMapClick(clickPoint: GmsLatLng) {
//    }
//
//    private fun addWaypoint(clickPoint: LatLng) {
//        val searchPath = directionMarkers.map { it.position }.toMutableList()
//
//        if (searchPath.size >= 2) {
//            var boundaryEndpoint = getBoundaryWaypoint(searchPath[0], searchPath[1])
//            searchPath.add(0, boundaryEndpoint)
//
//            boundaryEndpoint =
//                getBoundaryWaypoint(searchPath.last(), searchPath[searchPath.size - 2])
//            searchPath.add(boundaryEndpoint)
//        }
//
//        var insertIndex = findWaypointInsertIndex(searchPath, clickPoint.toGmsLatLng())
//
//        if (insertIndex != -1) {
//            directionPolyline?.let {
//                if (searchPath.size != directionMarkers.size) {
//                    insertIndex -= 1 // count the fake waypoint
//                }
//
//                val polylinePoints = it.points
//                polylinePoints.add(insertIndex, clickPoint.toGmsLatLng())
//                it.points = polylinePoints
//
//                val marker = map.addMarker(
//                    createMarker(clickPoint.toGmsLatLng())
//                )
//                if (marker != null) {
//                    directionMarkers.add(insertIndex, marker)
//                }
//            }
//        } else {
//            drawDirectionResult(listOf(clickPoint))
//        }
//
//        directionPolyline?.let {
//            viewModel.recordDirectionState(it.points.map { it.toLatLng() })
//        }
//    }
//
//    override fun onMarkerClick(clickMarker: Marker): Boolean {
//        clickMarker.showInfoWindow()
//        return true
//    }
//
//    override fun onPolylineClick(p0lyline: Polyline) {
//    }
//
//    override fun onMapLongClick(clickPoint: GmsLatLng) {
//        addWaypoint(clickPoint.toLatLng())
//    }
//
//    override fun onInfoWindowClick(clickMarker: Marker) {
//        directionMarkers.indexOf(clickMarker).let { foundIndex ->
//            directionMarkers.removeAt(foundIndex)
//            directionPolyline?.let {
//                val polylinePoints = it.points
//                polylinePoints.removeAt(foundIndex)
//                it.points = polylinePoints
//
//                viewModel.recordDirectionState(it.points.map { it.toLatLng() })
//            }
//        }
//
//        clickMarker.remove()
//    }
//
//    override fun onCameraIdle() {
//        directionMarkers.forEach {
//            it.isVisible = map.cameraPosition.zoom >= MAP_DEFAULT_ZOOM_LEVEL
//        }
//    }

    override fun onFinishRouteErasing(erasingRegion: Region) {
        startReviewing()
        val map = mapView.getMapboxMap()
        val erasingPoints = routeLineSourceData.coordinates()
            .filter { point ->
                val screenCoordinate = map.pixelForCoordinate(point)
                erasingRegion.contains(screenCoordinate.x.toInt(), screenCoordinate.y.toInt())
            }

        if (erasingPoints.isNotEmpty()) {
            viewModel.eraseCoordinates(erasingPoints.map { it.toLatLng() })
        }
    }

//    fun getBoundaryWaypoint(endPoint: GmsLatLng, secondPoint: GmsLatLng): GmsLatLng {
//        val a = (endPoint.latitude - secondPoint.latitude) /
//            (endPoint.longitude - secondPoint.longitude)
//        val b = endPoint.latitude - a * endPoint.longitude
//        return if (endPoint.longitude <= secondPoint.longitude &&
//            endPoint.latitude >= secondPoint.latitude
//        ) {
//            if (endPoint.longitude == secondPoint.longitude) {
//                GmsLatLng(endPoint.latitude + 0.1, endPoint.longitude)
//            } else {
//                val lng = endPoint.longitude - 1
//                GmsLatLng(a * lng + b, lng)
//            }
//        } else if (endPoint.longitude >= secondPoint.longitude &&
//            endPoint.latitude >= secondPoint.latitude
//        ) {
//            if (endPoint.longitude == secondPoint.longitude) {
//                GmsLatLng(endPoint.latitude + 0.1, endPoint.longitude)
//            } else {
//                val lng = endPoint.longitude + 1
//                GmsLatLng(a * lng + b, lng)
//            }
//        } else if (endPoint.longitude >= secondPoint.longitude &&
//            endPoint.latitude <= secondPoint.latitude
//        ) {
//            if (endPoint.longitude == secondPoint.longitude) {
//                GmsLatLng(endPoint.latitude - 0.1, endPoint.longitude)
//            } else {
//                val lng = endPoint.longitude + 1
//                GmsLatLng(a * lng + b, lng)
//            }
//        } else {
//            if (endPoint.longitude == secondPoint.longitude) {
//                GmsLatLng(endPoint.latitude - 0.1, endPoint.longitude)
//            } else {
//                val lng = endPoint.longitude - 1
//                GmsLatLng(a * lng + b, lng)
//            }
//        }
//    }
//
//    private fun findWaypointInsertIndex(waypoints: List<GmsLatLng>, insertPoint: GmsLatLng): Int {
//        if (waypoints.isEmpty()) {
//            return -1
//        }
//
//        var insertIndex = 1
//        var minDistance = Double.MAX_VALUE
//
//        for (index in 1 until waypoints.size) {
//            val distance =
//                PolyUtil.distanceToLine(insertPoint, waypoints[index - 1], waypoints[index])
//            if (distance < minDistance) {
//                minDistance = distance
//                insertIndex = index
//            }
//        }
//
//        return insertIndex
//    }

    companion object {
        private const val EXT_ROUTE_ID = "EXT_ROUTE_ID"
        private const val MAP_DEFAULT_ZOOM_LEVEL = 18f
        private const val MAP_COORDINATE_SOURCE = "MAP_COORDINATE_SOURCE"
        private const val MAP_ROUTE_LINE_SOURCE_ID = "MAP_ROUTE_POLYLINE_SOURCE_ID"
        private const val MAP_ROUTE_LINE_LAYER_ID = "MAP_ROUTE_POLYLINE_LAYER_ID"

        fun addNewRouteIntent(context: Context): Intent =
            Intent(context, RoutePlanningActivity::class.java)

        fun editRouteIntent(context: Context, routeId: String): Intent {
            val intent = Intent(context, RoutePlanningActivity::class.java)
            intent.putExtra(EXT_ROUTE_ID, routeId)
            return intent
        }
    }
}

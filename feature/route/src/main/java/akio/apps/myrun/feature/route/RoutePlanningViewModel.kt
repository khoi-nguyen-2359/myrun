package akio.apps.myrun.feature.route

import akio.apps.myrun.data.LaunchCatchingDelegate
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.route.api.RouteRepository
import akio.apps.myrun.data.route.api.model.RouteModel
import akio.apps.myrun.domain.route.impl.RoutePlottingUsecase
import akio.apps.myrun.feature.base.map.LatLngBoundsBuilder
import akio.apps.myrun.feature.base.map.toGmsLatLng
import akio.apps.myrun.feature.route.model.RoutePlottingState
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLngBounds
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class RoutePlanningViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val plotRouteUsecase: RoutePlottingUsecase,
    private val locationDataSource: LocationDataSource,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
    private val routeRepository: RouteRepository,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    private val _routeDrawingMode: MutableStateFlow<RouteDrawingMode> =
        MutableStateFlow(RouteDrawingMode.Review)
    val routeDrawingMode: Flow<RouteDrawingMode> = _routeDrawingMode

    private val _screenState: MutableStateFlow<ScreenState> =
        MutableStateFlow(ScreenState.LoadingRoute)
    val screenState: Flow<ScreenState> = _screenState

    init {
        loadInitialScreenState()
    }

    private fun loadInitialScreenState() = viewModelScope.launch {
        val savedScreenState = savedStateHandle.getScreenState()
        if (savedScreenState != null) {
            _screenState.value = savedScreenState
            return@launch
        }

        val initRouteDetail = savedStateHandle.getRouteId()?.let { routeId ->
            _screenState.value = ScreenState.LoadingRoute
            routeRepository.getRouteDetail(routeId)
        }
        val initWaypoints = initRouteDetail?.waypoints ?: emptyList()
        _screenState.value =
            ScreenState.RoutePlotting(
                initRouteDetail?.routeModel?.toEditingRouteInfo() ?: EditingRouteInfo(),
                RoutePlottingState.createFromRoute(initWaypoints)
            )
    }

    /**
     * Flow that emits the first screen state which contains route waypoints data that can be built
     * a boundary.
     */
    suspend fun getInitialMapViewBounds(): LatLngBounds? =
        _screenState.filterIsInstance<ScreenState.RoutePlotting>()
            .map {
                val waypoints = it.plottingState.getCurrentState()
                val builder = LatLngBoundsBuilder().include(waypoints)
                if (waypoints.isEmpty()) {
                    locationDataSource.getLastLocation()?.toGmsLatLng()?.let(builder::include)
                }
                builder.build()
            }
            .first()

    fun plotRoute(drawnPath: List<LatLng>) {
        val routePlottingScreenState =
            (_screenState.value as? ScreenState.RoutePlotting)
                ?: return
        viewModelScope.launchCatching {
            val plottingResult = plotRouteUsecase.plotRoute(
                drawnPath,
                routePlottingScreenState.plottingState.getCurrentState()
            )
            val screenState = routePlottingScreenState.copy(
                plottingState = routePlottingScreenState.plottingState.record(plottingResult)
            )
            _screenState.value = screenState
        }.invokeOnCompletion {
            _routeDrawingMode.value = RouteDrawingMode.Review
        }
    }

    fun forwardDirectionState() {
        val routePlottingScreenState =
            (_screenState.value as? ScreenState.RoutePlotting)
                ?: return
        val screenState = routePlottingScreenState.copy(
            plottingState = routePlottingScreenState.plottingState.forward()
        )
        _screenState.value = screenState
    }

    fun rewindDirectionState() {
        val routePlottingScreenState =
            (_screenState.value as? ScreenState.RoutePlotting)
                ?: return
        val screenState = routePlottingScreenState.copy(
            plottingState = routePlottingScreenState.plottingState.rewind()
        )
        _screenState.value = screenState
    }

    fun toggleDrawing() {
        if (_routeDrawingMode.value == RouteDrawingMode.Draw) {
            _routeDrawingMode.value = RouteDrawingMode.Review
        } else {
            _routeDrawingMode.value = RouteDrawingMode.Draw
        }
    }

    fun toggleErasing() {
        if (_routeDrawingMode.value == RouteDrawingMode.Erase) {
            _routeDrawingMode.value = RouteDrawingMode.Review
        } else {
            _routeDrawingMode.value = RouteDrawingMode.Erase
        }
    }

    fun recordDirectionState(waypoints: List<LatLng>) {
        val routePlottingScreenState =
            (_screenState.value as? ScreenState.RoutePlotting)
                ?: return
        val screenState = routePlottingScreenState.copy(
            plottingState = routePlottingScreenState.plottingState.record(waypoints)
        )
        _screenState.value = screenState
    }

    private fun SavedStateHandle.getRouteId(): String? = savedStateHandle[STATE_ROUTE_ID_ARG]
    private fun SavedStateHandle.getScreenState(): ScreenState? =
        savedStateHandle[STATE_SCREEN_STATE]

    private fun RouteModel.toEditingRouteInfo(): EditingRouteInfo =
        EditingRouteInfo(routeId, routeName)

    enum class RouteDrawingMode {
        Draw, Erase, Review
    }

    sealed class ScreenState {
        @Parcelize
        object LoadingRoute : ScreenState(), Parcelable

        @Parcelize
        data class RoutePlotting(
            val editingRouteInfo: EditingRouteInfo,
            val plottingState: RoutePlottingState,
        ) : ScreenState(), Parcelable
    }

    @Parcelize
    data class EditingRouteInfo(
        val routeId: String? = null,
        val routeName: String = "",
    ) : Parcelable

    companion object {
        private const val STATE_ROUTE_ID_ARG = "STATE_ROUTE_ID_ARG"
        private const val STATE_SCREEN_STATE = "STATE_SCREEN_STATE"

        fun saveArguments(
            savedStateHandle: SavedStateHandle,
            routeId: String?,
        ) {
            savedStateHandle[STATE_ROUTE_ID_ARG] = routeId
        }
    }
}

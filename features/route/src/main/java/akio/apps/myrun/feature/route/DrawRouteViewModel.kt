package akio.apps.myrun.feature.route

import akio.apps.common.data.LaunchCatchingDelegate
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.data.location.api.model.Location
import akio.apps.myrun.data.location.api.model.PlaceDetails
import akio.apps.myrun.domain.route.api.RoutePlottingUsecase
import akio.apps.myrun.feature.route.model.DirectionEditingState
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.launch

class DrawRouteViewModel @Inject constructor(
    private val plotRouteUsecase: RoutePlottingUsecase,
    private val locationDataSource: LocationDataSource,
    private val launchCatchingDelegate: LaunchCatchingDelegate,
) : ViewModel(), LaunchCatchingDelegate by launchCatchingDelegate {

    private val _routePlaceDetails = MutableLiveData<PlaceDetails>()
    val routePlaceDetails: LiveData<PlaceDetails> = _routePlaceDetails

    private val _routeWaypoints = MutableLiveData<List<LatLng>>()
    val routeWaypoints: LiveData<List<LatLng>> = _routeWaypoints

    private val _routeDrawingMode = MutableLiveData(RouteDrawingMode.Review)
    val routeDrawingMode: LiveData<RouteDrawingMode> = _routeDrawingMode

    private val _directionStateInfo = MutableLiveData<DirectionEditingState.DirectionStateInfo>()
    val directionStateInfo: LiveData<DirectionEditingState.DirectionStateInfo> =
        _directionStateInfo

    private val _mapInitLocation = MutableLiveData<Location>()
    val mapInitLocation: LiveData<Location> = _mapInitLocation

    private val directionEditingState = DirectionEditingState()

    fun fetchLastLocation() {
        viewModelScope.launch {
            val result = locationDataSource.getLastLocation()
            _mapInitLocation.value = result
        }
    }

    fun initDirectionData(initState: List<LatLng>) {
        directionEditingState.reset(initState)
        onDirectionStateChanged()
    }

    private fun onDirectionStateChanged() {
        _directionStateInfo.value = directionEditingState.getStateInfo()
    }

    fun plotRoute(drawnPath: List<LatLng>) {
        viewModelScope.launchCatching {
            val currentDirectionData =
                plotRouteUsecase.plotRoute(drawnPath, directionEditingState.getCurrentStateData())
            _routeWaypoints.value = currentDirectionData

            directionEditingState.record(currentDirectionData)
            onDirectionStateChanged()
        }.invokeOnCompletion {
            _routeDrawingMode.value = RouteDrawingMode.Review
        }
    }

    fun forwardDirectionState() {
        directionEditingState.forward()?.let { currentState ->
            _routeWaypoints.value = currentState
            onDirectionStateChanged()
        }
    }

    fun rewindDirectionState() {
        directionEditingState.rewind()?.let { currentState ->
            _routeWaypoints.value = currentState
            onDirectionStateChanged()
        }
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

    fun recordDirectionState(waypoints: List<LatLng>, placeId: String? = null) {
        Log.d("kh", "placeIOd$placeId")
        directionEditingState.record(waypoints)
        onDirectionStateChanged()
    }

    enum class RouteDrawingMode {
        Draw, Erase, Review
    }
}

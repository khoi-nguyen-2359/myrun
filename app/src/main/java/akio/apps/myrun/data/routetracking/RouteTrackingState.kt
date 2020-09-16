package akio.apps.myrun.data.routetracking

interface RouteTrackingState {
    suspend fun isRouteTrackingInProgress(): Boolean
    suspend fun setRouteTrackingInProgress(isTracking: Boolean)
}
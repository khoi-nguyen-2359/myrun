package akio.apps.myrun.domain.route.impl

import akio.apps.myrun.data.location.api.DirectionDataSource
import akio.apps.myrun.data.location.api.SphericalUtil
import akio.apps.myrun.data.location.api.model.LatLng
import akio.apps.myrun.domain.route.api.RoutePlottingUsecase
import javax.inject.Inject
import kotlin.math.min

class RoutePlottingUsecaseImpl @Inject constructor(
    private val directionDataSource: DirectionDataSource,
    private val sphericalUtil: SphericalUtil,
) : RoutePlottingUsecase {
    override suspend fun plotRoute(
        addingWaypoints: List<LatLng>,
        currentRoute: List<LatLng>
    ): List<LatLng> {
        if (addingWaypoints.size < 2)
            return currentRoute

        val linkMode = detectLinkMode(addingWaypoints, currentRoute)
        val directionWaypoints = makeDirectionWaypoints(addingWaypoints, currentRoute, linkMode)

        val directionResult = directionDataSource.getWalkingDirections(directionWaypoints)

        return linkWaypoints(directionResult, currentRoute, linkMode)
    }

    private fun linkWaypoints(
        directionResult: List<LatLng>,
        currentRoute: List<LatLng>,
        linkMode: DirectionLinkMode,
    ): List<LatLng> = when (linkMode) {
        DirectionLinkMode.ReversedPrepend,
        DirectionLinkMode.Prepend,
        -> directionResult.toMutableList().apply {
            removeLastOrNull()
            addAll(currentRoute)
        }

        DirectionLinkMode.ReversedAppend,
        DirectionLinkMode.Append,
        -> directionResult.toMutableList().apply {
            removeFirstOrNull()
            addAll(0, currentRoute)
        }

        else -> currentRoute
    }

    private fun makeDirectionWaypoints(
        addingWaypoints: List<LatLng>,
        currentRoute: List<LatLng>,
        linkMode: DirectionLinkMode,
    ): List<LatLng> = when (linkMode) {
        DirectionLinkMode.ReversedPrepend -> addingWaypoints.toMutableList()
            .asReversed()
            .apply {
                add(currentRoute.first())
            }

        DirectionLinkMode.Prepend -> addingWaypoints.toMutableList()
            .apply {
                add(currentRoute.first())
            }

        DirectionLinkMode.Append -> addingWaypoints.toMutableList()
            .apply {
                add(0, currentRoute.last())
            }

        DirectionLinkMode.ReversedAppend -> addingWaypoints.toMutableList()
            .asReversed()
            .apply {
                add(0, currentRoute.last())
            }

        else -> addingWaypoints
    }

    private fun detectLinkMode(
        drawnWaypoints: List<LatLng>,
        currentWaypoints: List<LatLng>,
    ): DirectionLinkMode {
        if (currentWaypoints.isEmpty() || drawnWaypoints.isEmpty()) {
            return DirectionLinkMode.None
        }

        val currentHead = currentWaypoints.first()
        val currentTail = currentWaypoints.last()
        val resultHead = drawnWaypoints.first()
        val resultTail = drawnWaypoints.last()
        val head2headDistance = sphericalUtil.computeDistanceBetween(currentHead, resultHead)
        val head2tailDistance = sphericalUtil.computeDistanceBetween(currentHead, resultTail)
        val tail2headDistance = sphericalUtil.computeDistanceBetween(currentTail, resultHead)
        val tail2tailDistance = sphericalUtil.computeDistanceBetween(currentTail, resultTail)

        val minDistance = min(
            min(head2headDistance, head2tailDistance),
            min(tail2headDistance, tail2tailDistance)
        )
        return when (minDistance) {
            head2headDistance -> DirectionLinkMode.ReversedPrepend
            head2tailDistance -> DirectionLinkMode.Prepend
            tail2headDistance -> DirectionLinkMode.Append
            tail2tailDistance -> DirectionLinkMode.ReversedAppend
            else -> DirectionLinkMode.None
        }
    }

    enum class DirectionLinkMode {
        Prepend, // adding new waypoints at the beginning of current route
        Append, // adding new waypoints at the end of current route
        ReversedPrepend, // reverse new waypoints against the plotting direction, then prepend
        ReversedAppend, // reverse new waypoints against the plotting direction, then append
        None // can't link
    }
}

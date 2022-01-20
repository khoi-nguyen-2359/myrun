package akio.apps.myrun.feature.route.model

import akio.apps.myrun.data.location.api.model.LatLng
import android.os.Parcelable
import androidx.annotation.IntRange
import kotlinx.parcelize.Parcelize

@Parcelize
data class RoutePlottingState(
    val stateData: List<List<LatLng>>,
    @IntRange(from = 0, to = Long.MAX_VALUE)
    val currentIndex: Int,
) : Parcelable {

    /**
     * Returns null if state data is invalid.
     */
    fun getCurrentState(): List<LatLng> = stateData[currentIndex]

    fun record(waypoints: List<LatLng>): RoutePlottingState = RoutePlottingState(
        stateData = stateData.subList(0, currentIndex + 1) + listOf(waypoints),
        currentIndex = currentIndex + 1
    )

    fun forward(): RoutePlottingState = if (currentIndex >= stateData.size - 1) {
        this.copy()
    } else {
        RoutePlottingState(
            stateData = stateData,
            currentIndex = currentIndex + 1
        )
    }

    fun rewind(): RoutePlottingState = if (currentIndex <= 0) {
        this.copy()
    } else {
        RoutePlottingState(
            stateData = stateData,
            currentIndex = currentIndex - 1
        )
    }

    companion object {
        fun createFromRoute(waypoints: List<LatLng>): RoutePlottingState = RoutePlottingState(
            stateData = listOf(waypoints),
            currentIndex = 0
        )
    }
}

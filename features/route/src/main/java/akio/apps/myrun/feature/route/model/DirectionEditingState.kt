package akio.apps.myrun.feature.route.model

import akio.apps.myrun.data.location.api.model.LatLng

class DirectionEditingState(initState: List<LatLng>? = null) {
    private val directionsStateList = mutableListOf<List<LatLng>>()
    private var currentState = -1

    init {
        if (initState != null) {
            record(initState)
        } else {
            record(emptyList())
        }
    }

    fun reset(state: List<LatLng>) {
        directionsStateList.clear()
        directionsStateList.add(state)
        currentState = 0
    }

    fun record(state: List<LatLng>) {
        ++currentState
        // new state will override all states right after current state
        directionsStateList.subList(currentState, directionsStateList.size).clear()

        directionsStateList.add(state)
    }

    fun forward(): List<LatLng>? {
        if (currentState >= directionsStateList.size - 1) {
            return null
        }

        return directionsStateList[++currentState]
    }

    fun rewind(): List<LatLng>? {
        if (currentState <= 0) {
            return null
        }

        return directionsStateList[--currentState]
    }

    fun getCurrentStateData(): List<LatLng> {
        return directionsStateList[currentState]
    }

    fun getStateInfo(): DirectionStateInfo {
        return DirectionStateInfo(currentState, directionsStateList.size)
    }

    data class DirectionStateInfo(
        val currentState: Int,
        val stateListSize: Int,
    )
}

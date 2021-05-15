package akio.apps.myrun.data.localconfig

import akio.apps.myrun.data.recentplace.entity.PlaceIdentifier

interface LocalConfiguration {
    suspend fun setRecentPlaceIdentifier(placeIdentifier: PlaceIdentifier)
    suspend fun getRecentPlaceIdentifier(): PlaceIdentifier?
}

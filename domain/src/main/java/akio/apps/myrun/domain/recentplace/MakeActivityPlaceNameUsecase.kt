package akio.apps.myrun.domain.recentplace

import akio.apps.myrun.data.user.api.PlaceIdentifier
import javax.inject.Inject

class MakeActivityPlaceNameUsecase @Inject constructor(
    private val placeIdentifierConverter: PlaceIdentifierConverter,
) {
    /**
     * Gets the activity place name to display, depends on user current place.
     * [currentUserPlaceIdentifier] is null in case current user doesn't have recent place recorded
     * yet.
     * Returns null if activity doesn't have place name.
     */
    operator fun invoke(
        activityPlaceIdentifier: PlaceIdentifier?,
        currentUserPlaceIdentifier: PlaceIdentifier?,
    ): String? {
        val activityAddressList =
            placeIdentifierConverter.toAddressNameList(activityPlaceIdentifier)
        if (activityAddressList.isEmpty()) {
            return null
        }
        val placeNameComponentList = if (activityAddressList.size <= 2) {
            // activity address length <= 2, take all items
            activityAddressList
        } else {
            // activity address length >= 3
            if (currentUserPlaceIdentifier == null) {
                activityAddressList.take(2)
            } else {
                val currentUserAddressList =
                    placeIdentifierConverter.toAddressNameList(currentUserPlaceIdentifier)
                var i = 0
                while (i < currentUserAddressList.size &&
                    i < activityAddressList.size &&
                    currentUserAddressList[i] == activityAddressList[i]
                ) {
                    ++i
                }
                if (i >= activityAddressList.size - 1) {
                    activityAddressList.takeLast(2)
                } else {
                    activityAddressList.subList(i, i + 2)
                }
            }
        }

        return placeNameComponentList.joinToString(", ")
    }
}

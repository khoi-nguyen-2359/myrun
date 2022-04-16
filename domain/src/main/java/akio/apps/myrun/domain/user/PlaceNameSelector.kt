package akio.apps.myrun.domain.user

import javax.inject.Inject

/**
 * Select elements in addresses of a place and present it as a readable place name to user.
 */
class PlaceNameSelector @Inject constructor(
    private val placeIdentifierConverter: PlaceIdentifierConverter,
) {
    /**
     * Gets the place name to display, depends on user current place.
     * [userPlaceIdentifier] is null in case current user doesn't have recent place recorded
     * yet.
     * Returns null if target place doesn't have a name.
     */
    fun select(targetPlaceIdentifier: String?, userPlaceIdentifier: String?): String? {
        val activityAddressList =
            placeIdentifierConverter.toAddressNameList(targetPlaceIdentifier)
        if (activityAddressList.isEmpty()) {
            return null
        }
        val placeNameComponentList = if (activityAddressList.size <= 2) {
            // activity address length <= 2, take all items
            activityAddressList
        } else {
            // activity address length >= 3
            if (userPlaceIdentifier == null) {
                activityAddressList.take(2)
            } else {
                val currentUserAddressList =
                    placeIdentifierConverter.toAddressNameList(userPlaceIdentifier)
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

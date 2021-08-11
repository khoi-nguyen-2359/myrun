package akio.apps.myrun.domain.recentplace

import akio.apps.myrun.data.location.api.model.PlaceAddressComponent
import akio.apps.myrun.data.recentplace.PlaceIdentifier
import akio.apps.myrun.domain.recentplace.PlaceIdentifierConverter.Companion.ADDRESS_SEPARATOR
import javax.inject.Inject

/**
 * A list of place's address names joined by [ADDRESS_SEPARATOR].
 */
class PlaceIdentifierConverter @Inject constructor() {

    fun toAddressNameList(identifier: PlaceIdentifier?): List<String> =
        identifier?.split(ADDRESS_SEPARATOR) ?: emptyList()

    fun fromAddressComponentList(components: List<PlaceAddressComponent>): PlaceIdentifier =
        components.map { it.name }
            .distinct()
            .joinToString(ADDRESS_SEPARATOR)

    companion object {
        private const val ADDRESS_SEPARATOR = "-"
    }
}

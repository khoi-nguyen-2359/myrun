package akio.apps.myrun.data.recentplace.entity

import akio.apps.myrun.data.place.entity.PlaceAddressComponent

data class PlaceIdentifier(val identifier: String) {

    fun toAddressNameList(): List<String> = identifier.split(ADDRESS_SEPARATOR)

    companion object {
        private const val ADDRESS_SEPARATOR = "-"
        fun fromAddressComponentList(components: List<PlaceAddressComponent>): PlaceIdentifier {
            val identifier = components.map { it.name }
                .distinct()
                .joinToString(ADDRESS_SEPARATOR)
            return PlaceIdentifier(identifier)
        }
    }
}

package akio.apps.myrun.data.user.api.model

class PlaceIdentifier private constructor(val addressComponents: List<String>) {
    fun toPlaceIdentifierString(): String =
        addressComponents.joinToString(ADDRESS_SEPARATOR)

    companion object {
        private const val ADDRESS_SEPARATOR = "-"
        fun fromAddressComponents(addressNameList: List<String>?): PlaceIdentifier? =
            addressNameList?.distinct()?.let(::PlaceIdentifier)

        fun fromPlaceIdentifierString(identifier: String?): PlaceIdentifier? =
            identifier?.split(ADDRESS_SEPARATOR)?.let(::PlaceIdentifier)
    }
}

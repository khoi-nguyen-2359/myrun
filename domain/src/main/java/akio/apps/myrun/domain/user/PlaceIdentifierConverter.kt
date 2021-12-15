package akio.apps.myrun.domain.user

import akio.apps.myrun.domain.user.PlaceIdentifierConverter.Companion.ADDRESS_SEPARATOR
import javax.inject.Inject

/**
 * A list of place's address names joined by [ADDRESS_SEPARATOR].
 */
class PlaceIdentifierConverter @Inject constructor() {

    fun toAddressNameList(identifier: String?): List<String> =
        identifier?.split(ADDRESS_SEPARATOR) ?: emptyList()

    fun fromAddressNameList(addressNameList: List<String>): String =
        addressNameList.distinct().joinToString(ADDRESS_SEPARATOR)

    companion object {
        private const val ADDRESS_SEPARATOR = "-"
    }
}

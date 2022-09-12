package akio.apps.myrun.domain.user

import akio.apps.myrun.data.user.api.model.PlaceIdentifier
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlaceNameSelectorTest {
    private lateinit var selector: PlaceNameSelector

    @Before
    fun setup() {
        selector = PlaceNameSelector()
    }

    @Test
    fun testGetActivityDisplayPlaceName_DifferentCountry() {
        val activityPlaceIdentifier = "USA-CA-SJ".toPlaceIdentifier()
        val currentUserPlaceIdentifier = "VN-HCM-D10-W9".toPlaceIdentifier()
        val placeName = selector.select(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("USA, CA", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_DifferentCity() {
        val activityPlaceIdentifier = "VN-HN-CG".toPlaceIdentifier()
        val currentUserPlaceIdentifier = "VN-HCM-D10-W9".toPlaceIdentifier()
        val placeName = selector.select(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("HN, CG", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_DifferentLastAddress() {
        val activityPlaceIdentifier = "VN-HCM-D10-W10".toPlaceIdentifier()
        val currentUserPlaceIdentifier = "VN-HCM-D10-W9".toPlaceIdentifier()
        val placeName = selector.select(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("D10, W10", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_NoUserRecentPlace() {
        val activityPlaceIdentifier = "VN-HCM-D10-W10".toPlaceIdentifier()
        val currentUserPlaceIdentifier = null
        val placeName = selector.select(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("VN, HCM", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_ShortActivityPlaceAddressList() {
        val activityPlaceIdentifier = "HCM-D10".toPlaceIdentifier()
        val currentUserPlaceIdentifier = "HCM-D10-W15".toPlaceIdentifier()
        val placeName = selector.select(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("HCM, D10", placeName)
    }

    private fun String.toPlaceIdentifier(): PlaceIdentifier? =
        PlaceIdentifier.fromPlaceIdentifierString(this)
}

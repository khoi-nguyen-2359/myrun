package akio.apps.myrun.domain.user

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PlaceNameSelectorTest {
    private lateinit var selector: PlaceNameSelector

    @Before
    fun setup() {
        selector = PlaceNameSelector(PlaceIdentifierConverter())
    }

    @Test
    fun testGetActivityDisplayPlaceName_DifferentCountry() {
        val activityPlaceIdentifier = "USA-CA-SJ"
        val currentUserPlaceIdentifier = "VN-HCM-D10-W9"
        val placeName = selector.select(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("USA, CA", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_DifferentCity() {
        val activityPlaceIdentifier = "VN-HN-CG"
        val currentUserPlaceIdentifier = "VN-HCM-D10-W9"
        val placeName = selector.select(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("HN, CG", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_DifferentLastAddress() {
        val activityPlaceIdentifier = "VN-HCM-D10-W10"
        val currentUserPlaceIdentifier = "VN-HCM-D10-W9"
        val placeName = selector.select(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("D10, W10", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_NoUserRecentPlace() {
        val activityPlaceIdentifier = "VN-HCM-D10-W10"
        val currentUserPlaceIdentifier = null
        val placeName = selector.select(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("VN, HCM", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_ShortActivityPlaceAddressList() {
        val activityPlaceIdentifier = "HCM-D10"
        val currentUserPlaceIdentifier = "HCM-D10-W15"
        val placeName = selector.select(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("HCM, D10", placeName)
    }
}

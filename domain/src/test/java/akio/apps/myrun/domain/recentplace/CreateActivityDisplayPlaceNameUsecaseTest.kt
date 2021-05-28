package akio.apps.myrun.domain.recentplace

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CreateActivityDisplayPlaceNameUsecaseTest {
    private lateinit var usecase: CreateActivityDisplayPlaceNameUsecase

    @Before
    fun setup() {
        usecase = CreateActivityDisplayPlaceNameUsecase(PlaceIdentifierConverter())
    }

    @Test
    fun testGetActivityDisplayPlaceName_DifferentCountry() {
        val activityPlaceIdentifier = "USA-CA-SJ"
        val currentUserPlaceIdentifier = "VN-HCM-D10-W9"
        val placeName = usecase(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("USA, CA", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_DifferentCity() {
        val activityPlaceIdentifier = "VN-HN-CG"
        val currentUserPlaceIdentifier = "VN-HCM-D10-W9"
        val placeName = usecase(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("HN, CG", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_DifferentLastAddress() {
        val activityPlaceIdentifier = "VN-HCM-D10-W10"
        val currentUserPlaceIdentifier = "VN-HCM-D10-W9"
        val placeName = usecase(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("D10, W10", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_NoUserRecentPlace() {
        val activityPlaceIdentifier = "VN-HCM-D10-W10"
        val currentUserPlaceIdentifier = null
        val placeName = usecase(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("VN, HCM", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_ShortActivityPlaceAddressList() {
        val activityPlaceIdentifier = "HCM-D10"
        val currentUserPlaceIdentifier = "HCM-D10-W15"
        val placeName = usecase(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("HCM, D10", placeName)
    }
}

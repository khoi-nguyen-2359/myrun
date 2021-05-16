package akio.apps.myrun.domain.recentplace

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CreateActivityDisplayPlaceNameUsecaseTest {
    private lateinit var usecase: CreateActivityDisplayPlaceNameUsecase

    @Before
    fun setup() {
        usecase = CreateActivityDisplayPlaceNameUsecase()
    }

    @Test
    fun testGetActivityDisplayPlaceName_DifferentCountry() {
        val activityPlaceIdentifier = PlaceIdentifierConverter("USA-CA-SJ")
        val currentUserPlaceIdentifier = PlaceIdentifierConverter("VN-HCM-D10-W9")
        val placeName = usecase(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("USA, CA", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_DifferentCity() {
        val activityPlaceIdentifier = PlaceIdentifierConverter("VN-HN-CG")
        val currentUserPlaceIdentifier = PlaceIdentifierConverter("VN-HCM-D10-W9")
        val placeName = usecase(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("HN, CG", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_DifferentLastAddress() {
        val activityPlaceIdentifier = PlaceIdentifierConverter("VN-HCM-D10-W10")
        val currentUserPlaceIdentifier = PlaceIdentifierConverter("VN-HCM-D10-W9")
        val placeName = usecase(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("D10, W10", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_NoUserRecentPlace() {
        val activityPlaceIdentifier = PlaceIdentifierConverter("VN-HCM-D10-W10")
        val currentUserPlaceIdentifier = null
        val placeName = usecase(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("VN, HCM", placeName)
    }

    @Test
    fun testGetActivityDisplayPlaceName_ShortActivityPlaceAddressList() {
        val activityPlaceIdentifier = PlaceIdentifierConverter("HCM-D10")
        val currentUserPlaceIdentifier = PlaceIdentifierConverter("HCM-D10-W15")
        val placeName = usecase(activityPlaceIdentifier, currentUserPlaceIdentifier)
        assertEquals("HCM, D10", placeName)
    }
}

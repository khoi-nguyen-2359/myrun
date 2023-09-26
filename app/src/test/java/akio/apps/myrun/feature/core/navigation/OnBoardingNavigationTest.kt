package akio.apps.myrun.feature.core.navigation

import akio.apps.myrun.feature.core.navigation.OnBoardingNavigation.SIGNIN_ACTIVITY_CLASS_NAME
import akio.apps.myrun.feature.registration.SignInActivity
import kotlin.test.assertEquals
import org.junit.Test

class OnBoardingNavigationTest {
    @Test
    fun testSignInActivityClassNameChangedCorrectly() {
        assertEquals(
            SIGNIN_ACTIVITY_CLASS_NAME,
            SignInActivity::class.java.name
        )
    }
}

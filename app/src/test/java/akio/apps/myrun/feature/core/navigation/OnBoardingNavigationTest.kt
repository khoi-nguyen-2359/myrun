package akio.apps.myrun.feature.core.navigation

import akio.apps.myrun.feature.core.navigation.OnBoardingNavigation.SIGNIN_ACTIVITY_CLASS_NAME
import akio.apps.myrun.feature.core.navigation.OnBoardingNavigation.SPLASH_ACTIVITY_CLASS_NAME
import akio.apps.myrun.feature.registration.SignInActivity
import akio.apps.myrun.feature.splash.SplashActivity
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

    @Test
    fun testSplashActivityClassNameChangedCorrectly() {
        assertEquals(
            SPLASH_ACTIVITY_CLASS_NAME,
            SplashActivity::class.java.name
        )
    }
}

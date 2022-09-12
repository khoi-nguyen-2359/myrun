package akio.apps.myrun.feature.core.navigation

import android.content.Context
import android.content.Intent

object OnBoardingNavigation {
    const val EXT_SIGNIN_OR_REAUTH = "EXT_SIGNIN_OR_REAUTH"
    const val SIGNIN_ACTIVITY_CLASS_NAME = "akio.apps.myrun.feature.registration.SignInActivity"
    const val SPLASH_ACTIVITY_CLASS_NAME = "akio.apps.myrun.feature.splash.SplashActivity"

    fun createReAuthenticationIntent(context: Context): Intent? {
        val activityClass = getActivityClass(SIGNIN_ACTIVITY_CLASS_NAME) ?: return null
        return Intent(context, activityClass).apply {
            putExtra(EXT_SIGNIN_OR_REAUTH, false)
        }
    }

    fun createSignInIntent(context: Context): Intent? {
        val activityClass = getActivityClass(SIGNIN_ACTIVITY_CLASS_NAME) ?: return null
        return Intent(context, activityClass)
    }

    fun createSplashIntent(context: Context): Intent? {
        val activityClass = getActivityClass(SPLASH_ACTIVITY_CLASS_NAME) ?: return null
        return Intent(context, activityClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun getActivityClass(className: String): Class<*>? = try {
        Class.forName(className)
    } catch (ex: ClassNotFoundException) {
        null
    }
}

package akio.apps.myrun.feature.userprofile.impl

import akio.apps.myrun.R
import akio.apps.myrun.data.externalapp._di.ExternalAppDataModule
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber

class LinkStravaDelegate(
    private val activity: Activity,
    private val eventListener: EventListener
) {

    val stravaRedirectUri = createRedirectUri(activity)

    fun checkStravaLoginResult(intent: Intent?) {
        val data = intent?.data ?: return
        if (!data.toString().startsWith(stravaRedirectUri))
            return

        data.getQueryParameter("error")
            ?.let { errorMessage ->
                Timber.e(errorMessage)
                eventListener.onGetStravaLoginError(errorMessage)
                return
            }

        data.getQueryParameter("code")
            ?.let { loginCode ->
                eventListener.onGetStravaLoginCode(loginCode)
            }
    }

    fun openStravaLogin() {
        val intentUri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", ExternalAppDataModule.STRAVA_APP_ID)
            .appendQueryParameter("redirect_uri", stravaRedirectUri)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("approval_prompt", "auto")
            .appendQueryParameter("scope", "activity:write,read")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, intentUri)
        activity.startActivity(intent)
    }

    companion object {
        fun createRedirectUri(context: Context) =
            "${context.getString(R.string.app_scheme)}://" +
                "${context.getString(R.string.strava_callback_host)}"

        fun buildStravaLoginIntent(context: Context): Intent {
            val intentUri = Uri.parse("https://www.strava.com/oauth/mobile/authorize")
                .buildUpon()
                .appendQueryParameter("client_id", ExternalAppDataModule.STRAVA_APP_ID)
                .appendQueryParameter("redirect_uri", createRedirectUri(context))
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("approval_prompt", "auto")
                .appendQueryParameter("scope", "activity:write,read")
                .build()

            return Intent(Intent.ACTION_VIEW, intentUri)
        }
    }

    interface EventListener {
        fun onGetStravaLoginError(errorMessage: String)
        fun onGetStravaLoginCode(loginCode: String)
    }
}

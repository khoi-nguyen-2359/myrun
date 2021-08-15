package akio.apps.myrun.feature.profile

import akio.apps.common.feature.lifecycle.collectEventRepeatOnStarted
import akio.apps.common.feature.lifecycle.collectRepeatOnStarted
import akio.apps.common.feature.lifecycle.observeEvent
import akio.apps.common.feature.viewmodel.lazyViewModelProvider
import akio.apps.myrun.feature.base.DialogDelegate
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * No UI activity to receive result from strava login.
 * Follow Strava document https://developers.strava.com/docs/authentication/
 */
class LinkStravaActivity : AppCompatActivity(), LinkStravaDelegate.EventListener {

    private val dialogDelegate = DialogDelegate(this)
    private val linkStravaDelegate by lazy { LinkStravaDelegate(this, this) }

    private val linkStravaViewModel: LinkStravaViewModel by lazyViewModelProvider {
        DaggerLinkStravaComponent.factory().create().linkStravaViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        linkStravaDelegate.checkStravaLoginResult(intent)
        collectRepeatOnStarted(
            linkStravaViewModel.isInProgress,
            dialogDelegate::toggleProgressDialog
        )
        collectEventRepeatOnStarted(linkStravaViewModel.error) {
            Toast.makeText(this, it.message, Toast.LENGTH_LONG)
                .show()
            finish()
        }
        observeEvent(linkStravaViewModel.stravaTokenExchangedSuccess) {
            Toast.makeText(
                this,
                getString(R.string.edit_user_profile_link_running_app_success_message),
                Toast.LENGTH_LONG
            )
                .show()
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        linkStravaDelegate.checkStravaLoginResult(intent)
    }

    override fun onGetStravaLoginError(errorMessage: String) {
        dialogDelegate.showErrorAlert(errorMessage)
    }

    override fun onGetStravaLoginCode(loginCode: String) {
        linkStravaViewModel.exchangeStravaToken(loginCode)
    }
}

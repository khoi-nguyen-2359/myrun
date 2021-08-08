package akio.apps.myrun.feature.strava.impl

import akio.apps.base.feature.lifecycle.observe
import akio.apps.base.feature.lifecycle.observeEvent
import akio.apps.base.feature.viewmodel.viewModel
import akio.apps.myrun.R
import akio.apps.myrun.feature.strava.LinkStravaViewModel
import akio.apps.myrun.feature.strava._di.DaggerStravaFeatureComponent
import akio.apps.myrun.feature.userprofile.impl.LinkStravaDelegate
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * No UI activity to receive result from strava login.
 * Follow Strava document https://developers.strava.com/docs/authentication/
 */
class LinkStravaActivity : AppCompatActivity(), LinkStravaDelegate.EventListener {

    private val dialogDelegate = akio.apps.myrun.feature.base.DialogDelegate(this)
    private val linkStravaDelegate by lazy { LinkStravaDelegate(this, this) }

    private val linkStravaViewModel: LinkStravaViewModel by viewModel {
        DaggerStravaFeatureComponent.factory().create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        linkStravaDelegate.checkStravaLoginResult(intent)
        observe(linkStravaViewModel.isInProgress, dialogDelegate::toggleProgressDialog)
        observeEvent(linkStravaViewModel.error) {
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

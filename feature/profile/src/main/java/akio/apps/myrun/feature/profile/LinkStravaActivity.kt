package akio.apps.myrun.feature.profile

import akio.apps.myrun.feature.core.Event
import akio.apps.myrun.feature.core.DialogDelegate
import akio.apps.myrun.feature.core.ktx.collectEventRepeatOnStarted
import akio.apps.myrun.feature.core.ktx.collectRepeatOnStarted
import akio.apps.myrun.feature.core.ktx.lazyViewModelProvider
import akio.apps.myrun.feature.profile.di.DaggerLinkStravaComponent
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

/**
 * No UI activity to receive result from strava login.
 * Follow Strava document https://developers.strava.com/docs/authentication/
 */
internal class LinkStravaActivity : AppCompatActivity(), LinkStravaDelegate.EventListener {

    private val dialogDelegate = DialogDelegate(this)
    private val linkStravaDelegate by lazy { LinkStravaDelegate(this, this) }

    private val linkStravaViewModel: LinkStravaViewModel by lazyViewModelProvider {
        DaggerLinkStravaComponent.factory().create(application).linkStravaViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        linkStravaDelegate.checkStravaLoginResult(intent)
        collectRepeatOnStarted(
            linkStravaViewModel.isLaunchCatchingInProgress,
            dialogDelegate::toggleProgressDialog
        )
        collectEventRepeatOnStarted(linkStravaViewModel.launchCatchingError) {
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

    private fun <T> LifecycleOwner.observeEvent(liveData: LiveData<Event<T>>, block: (T) -> Unit) {
        liveData.observe(this, EventObserver { eventData -> block(eventData) })
    }
}

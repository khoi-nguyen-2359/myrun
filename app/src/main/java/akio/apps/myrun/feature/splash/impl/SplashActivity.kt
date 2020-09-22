package akio.apps.myrun.feature.splash.impl

import akio.apps._base.di.BaseInjectionActivity
import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps.myrun.R
import akio.apps.myrun.feature._base.utils.ActivityDialogDelegate
import akio.apps.myrun.feature.usertimeline.impl.UserTimelineActivity
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import akio.apps.myrun.feature.splash.SplashViewModel
import android.os.Bundle

class SplashActivity: BaseInjectionActivity() {

    private val splashViewModel: SplashViewModel by lazy { getViewModel() }

    private val dialogDelegate by lazy { ActivityDialogDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        observeEvent(splashViewModel.error, dialogDelegate::showExceptionAlert)

        observe(splashViewModel.isRouteTrackingInProgress) { isTracking ->
            finish()
            if (isTracking) {
                startActivity(RouteTrackingActivity.launchIntent(this@SplashActivity))
            } else {
                startActivity(UserTimelineActivity.launchIntent(this@SplashActivity))
            }
        }
    }
}
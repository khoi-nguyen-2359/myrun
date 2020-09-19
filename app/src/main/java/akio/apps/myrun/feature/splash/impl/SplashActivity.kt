package akio.apps.myrun.feature.splash.impl

import akio.apps._base.di.BaseInjectionActivity
import akio.apps._base.lifecycle.observe
import akio.apps.myrun.R
import akio.apps.myrun.feature.myworkout.impl.MyWorkoutActivity
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import akio.apps.myrun.feature.splash.SplashViewModel
import android.os.Bundle

class SplashActivity: BaseInjectionActivity() {

    private val splashViewModel: SplashViewModel by lazy { getViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        observe(splashViewModel.isRouteTrackingInProgress) { isTracking ->
            finish()
            if (isTracking) {
                startActivity(RouteTrackingActivity.launchIntent(this@SplashActivity))
            } else {
                startActivity(MyWorkoutActivity.launchIntent(this@SplashActivity))
            }
        }
    }
}
package akio.apps.myrun.feature.routetracking.impl

import akio.apps.common.activity.BaseInjectionActivity
import akio.apps.myrun.databinding.ActivityRouteTrackingBinding
import android.os.Bundle

class RouteTrackingActivity: BaseInjectionActivity() {
    private val viewBinding by lazy { ActivityRouteTrackingBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
    }

    private fun initViews() {
        viewBinding.apply {
            setContentView(root)
        }
    }

}
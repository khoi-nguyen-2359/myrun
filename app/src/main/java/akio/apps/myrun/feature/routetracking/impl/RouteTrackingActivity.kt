package akio.apps.myrun.feature.routetracking.impl

import akio.apps.common.activity.BaseInjectionActivity
import akio.apps.myrun.databinding.ActivityRouteTrackingBinding
import akio.apps.myrun.feature.common.AppPermissions
import android.os.Bundle

class RouteTrackingActivity : BaseInjectionActivity() {

    private val viewBinding by lazy { ActivityRouteTrackingBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()


    }

    override fun onStart() {
        super.onStart()

        requireLocationPermissions()
    }

    private fun initViews() {
        viewBinding.apply {
            setContentView(root)
        }
    }

    private fun requireLocationPermissions() {
        AppPermissions.requestLocationPermissions(this, null, RC_LOCATION_PERMISSIONS) {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        verifyPermissions(permissions)
    }

    private fun verifyPermissions(permissions: Array<String>) {
        AppPermissions.verifyPermissions(this, permissions) {
            finish()
        }
    }

    companion object {
        const val RC_LOCATION_PERMISSIONS = 1
    }
}
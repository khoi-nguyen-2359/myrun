package akio.apps.myrun.feature.home

import akio.apps.myrun.R
import akio.apps.myrun._base.utils.LocationServiceChecker
import akio.apps.myrun._di.viewModel
import akio.apps.myrun.feature.activitydetail.ActivityDetailActivity
import akio.apps.myrun.feature.activityexport.ActivityExportService
import akio.apps.myrun.feature.home._di.DaggerHomeFeatureComponent
import akio.apps.myrun.feature.home._di.HomeFeatureComponent
import akio.apps.myrun.feature.home.ui.HomeScreen
import akio.apps.myrun.feature.routetracking.impl.LocationPermissionChecker
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import akio.apps.myrun.feature.userprofile.impl.UserProfileFragment
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope

class HomeActivity : AppCompatActivity() {

    private val homeFeatureComponent: HomeFeatureComponent by lazy {
        DaggerHomeFeatureComponent.factory().create(application)
    }

    private val userTimelineViewModel: UserTimelineViewModel by viewModel { homeFeatureComponent }
    private val homeViewModel: HomeViewModel by viewModel { homeFeatureComponent }

    private val locationPermissionChecker: LocationPermissionChecker =
        LocationPermissionChecker(activity = this)

    private val locationServiceChecker by lazy {
        LocationServiceChecker(activity = this, RC_LOCATION_SERVICE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HomeScreen(
                userTimelineViewModel,
                onClickUserProfileButton = ::openUserProfile,
                onClickFloatingActionButton = ::openRouteTrackingScreen,
                onClickActivityItemAction = ::openActivityDetail,
                onClickActivityFileAction = ::startActivityExportService
            )
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_LOCATION_SERVICE) {
            locationServiceChecker.verifyLocationServiceResolutionResult(resultCode)
        }
    }

    private fun startActivityExportService(activity: Activity) {
        val intent = ActivityExportService.createAddActivityIntent(
            this,
            ActivityExportService.ActivityInfo(
                activity.id,
                activity.name,
                activity.startTime
            )
        )
        ContextCompat.startForegroundService(this, intent)
    }

    private fun openActivityDetail(activity: Activity) {
        val intent = ActivityDetailActivity.createIntent(this, activity.id)
        startActivity(intent)
    }

    private fun openUserProfile() {
        val launchIntent = UserProfileFragment.launchIntent(this)
        startActivity(launchIntent)
    }

    private fun openRouteTrackingScreen() = lifecycleScope.launchWhenCreated {
        // onCreate: check location permissions -> check location service availability -> allow user to use this screen
        val locationRequest = homeViewModel.getLocationRequest()
        val missingRequiredPermission =
            !locationPermissionChecker.check() || !locationServiceChecker.check(locationRequest)
        if (missingRequiredPermission) {
            Toast.makeText(
                this@HomeActivity,
                R.string.location_permission_is_missing_error,
                Toast.LENGTH_SHORT
            ).show()
            return@launchWhenCreated
        }

        startActivity(RouteTrackingActivity.launchIntent(this@HomeActivity))
    }

    companion object {
        private const val RC_LOCATION_SERVICE = 1

        fun clearTaskIntent(context: Context) = Intent(context, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}

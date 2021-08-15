package akio.apps.myrun.feature.home

import akio.apps.common.feature.ui.px2dp
import akio.apps.myrun.R
import akio.apps.myrun.feature.activitydetail.ActivityDetailActivity
import akio.apps.myrun.feature.activityexport.ActivityExportService
import akio.apps.myrun.feature.home.ui.HomeNavigationHost
import akio.apps.myrun.feature.routetracking.impl.LocationPermissionChecker
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import akio.apps.myrun.feature.usertimeline.model.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeActivity : AppCompatActivity() {

    private val locationPermissionChecker: LocationPermissionChecker =
        LocationPermissionChecker(activity = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(android.R.id.content)
        ) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            setContent {
                HomeNavigationHost(
                    contentPaddings = PaddingValues(
                        top = insets.top.px2dp.dp,
                        bottom = insets.bottom.px2dp.dp
                    ),
                    onClickFloatingActionButton = ::openRouteTrackingOrCheckRequiredPermission,
                    onClickActivityItemAction = ::openActivityDetail,
                    onClickExportActivityFile = ::startActivityExportService
                )
            }

            WindowInsetsCompat.CONSUMED
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

    private fun openUserProfile(userId: String) {
        Timber.d(userId)
        // TODO: Open user profile screen with user id
//        val launchIntent = UserProfileFragment.intentForUserId(this, userId)
//        startActivity(launchIntent)
    }

    private fun openCurrentUserProfile() {
        // TODO: Open user profile screen with current user
//        val launchIntent = UserProfileFragment.intentForCurrentUser(this)
//        startActivity(launchIntent)
    }

    private fun openRouteTrackingOrCheckRequiredPermission() {
        // onCreate: check location permissions -> check location service availability -> allow user to use this screen
        if (locationPermissionChecker.isGranted()) {
            openRouteTracking()
            return
        }

        lifecycleScope.launch {
            val missingRequiredPermission = !locationPermissionChecker.check()
            if (missingRequiredPermission) {
                Toast.makeText(
                    this@HomeActivity,
                    R.string.location_permission_is_missing_error,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            openRouteTracking()
        }
    }

    private fun openRouteTracking() = startActivity(RouteTrackingActivity.launchIntent(this))

    companion object {
        fun clearTaskIntent(context: Context): Intent = Intent(context, HomeActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

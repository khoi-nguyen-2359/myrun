package akio.apps.myrun.feature.home

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.api.model.ActivityModel
import akio.apps.myrun.feature.activitydetail.ActivityExportService
import akio.apps.myrun.feature.home.ui.AppNavHost
import akio.apps.myrun.feature.route.ui.DrawRouteActivity
import akio.apps.myrun.feature.tracking.LocationPermissionChecker
import akio.apps.myrun.feature.tracking.RouteTrackingActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private val locationPermissionChecker: LocationPermissionChecker =
        LocationPermissionChecker(activity = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AppNavHost(
                onClickFloatingActionButton = ::openRouteTrackingOrCheckRequiredPermission,
                onClickExportActivityFile = ::startActivityExportService,
                openRoutePlanningAction = ::openRoutePlanning
            )
        }
    }

    private fun openRoutePlanning() {
        val intent = DrawRouteActivity.addNewRouteIntent(this)
        startActivity(intent)
    }

    private fun startActivityExportService(activity: ActivityModel) {
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

package akio.apps.myrun.feature.main

import akio.apps.myrun.R
import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.feature.activitydetail.ActivityExportService
import akio.apps.myrun.feature.main.ui.MainNavHost
import akio.apps.myrun.feature.route.RoutePlanningFacade
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

class MainActivity : AppCompatActivity() {

    private val locationPermissionChecker: LocationPermissionChecker =
        LocationPermissionChecker(activity = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MainNavHost(
                onClickFloatingActionButton = ::openRouteTrackingOrCheckRequiredPermission,
                onClickExportActivityFile = ::startActivityExportService,
                openRoutePlanningAction = { RoutePlanningFacade.startRoutePlanning(this) }
            )
        }
    }

    private fun startActivityExportService(activity: BaseActivityModel) {
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

    // Check location permissions -> allow user to open tracking screen.
    private fun openRouteTrackingOrCheckRequiredPermission() {
        if (locationPermissionChecker.isGranted()) {
            openRouteTracking()
            return
        }

        lifecycleScope.launch {
            val missingRequiredPermission = !locationPermissionChecker.check()
            if (missingRequiredPermission) {
                Toast.makeText(
                    this@MainActivity,
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
        fun clearTaskIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

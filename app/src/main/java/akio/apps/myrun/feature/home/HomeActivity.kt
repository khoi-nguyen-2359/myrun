package akio.apps.myrun.feature.home

import akio.apps.myrun._di.viewModel
import akio.apps.myrun.feature.activitydetail.ActivityDetailActivity
import akio.apps.myrun.feature.activityexport.ActivityExportService
import akio.apps.myrun.feature.home.ui.HomeScreen
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import akio.apps.myrun.feature.userprofile.impl.UserProfileFragment
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline._di.DaggerUserTimelineFeatureComponent
import akio.apps.myrun.feature.usertimeline.model.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class HomeActivity : AppCompatActivity() {

    private val userTimelineViewModel: UserTimelineViewModel by viewModel {
        DaggerUserTimelineFeatureComponent.factory().create(application)
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

    private fun openRouteTrackingScreen() = startActivity(RouteTrackingActivity.launchIntent(this))

    companion object {
        fun clearTaskIntent(context: Context) = Intent(context, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}

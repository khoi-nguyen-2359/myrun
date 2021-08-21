package akio.apps.myrun.feature.activitydetail

import akio.apps.common.feature.viewmodel.lazyViewModelProvider
import akio.apps.myrun.feature.activitydetail._di.DaggerActivityDetailFeatureComponent
import akio.apps.myrun.feature.activitydetail.impl.ActivityDetailViewModel
import akio.apps.myrun.feature.activitydetail.ui.ActivityDetailScreen
import akio.apps.myrun.feature.activityexport.ActivityExportService
import akio.apps.myrun.feature.activityroutemap.ui.ActivityRouteMapActivity
import akio.apps.myrun.feature.usertimeline.model.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import timber.log.Timber

class ActivityDetailActivity : AppCompatActivity() {
    private val extActivityId: String by lazy { intent.getStringExtra(EXT_ACTIVITY_ID).orEmpty() }

    private val activityDetailViewModel: ActivityDetailViewModel by lazyViewModelProvider {
        DaggerActivityDetailFeatureComponent.factory()
            .create(ActivityDetailViewModel.Params(extActivityId))
            .activityDetailsViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ActivityDetailScreen(
                activityDetailViewModel,
                ::openActivityRouteMap,
                ::startActivityExportService,
                ::openUserProfile,
                ::finish
            )
        }

        activityDetailViewModel.loadActivityDetails()
    }

    private fun openUserProfile(userId: String) {
        Timber.d(userId)
        // TODO: Open user profile screen with user id
//        startActivity(UserProfileFragment.intentForUserId(this, userId))
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

    private fun openActivityRouteMap(encodedPolyline: String) =
        startActivity(ActivityRouteMapActivity.createLaunchIntent(this, encodedPolyline))

    companion object {
        private const val EXT_ACTIVITY_ID = "EXT_ACTIVITY_ID"

        fun createIntent(context: Context, activityId: String) = Intent(
            context,
            ActivityDetailActivity::class.java
        ).apply { putExtra(EXT_ACTIVITY_ID, activityId) }
    }
}

package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun._di.viewModel
import akio.apps.myrun.feature.activitydetail._di.DaggerActivityDetailFeatureComponent
import akio.apps.myrun.feature.activitydetail.ui.ActivityDetailComposable
import akio.apps.myrun.feature.activityroutemap.ui.ActivityRouteMapActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

class ActivityDetailActivity : AppCompatActivity() {
    private val extActivityId: String by lazy { intent.getStringExtra(EXT_ACTIVITY_ID).orEmpty() }

    private val activityDetailViewModel: ActivityDetailViewModel by viewModel {
        DaggerActivityDetailFeatureComponent.factory()
            .create(ActivityDetailViewModel.Params(extActivityId))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ActivityDetailComposable(activityDetailViewModel, ::openActivityRouteMap)
        }

        activityDetailViewModel.loadActivityDetails()
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

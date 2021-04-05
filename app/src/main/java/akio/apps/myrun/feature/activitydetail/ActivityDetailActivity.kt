package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.feature.activitydetail._di.ActivityDetailsModule
import akio.apps.myrun.feature.activitydetail.ui.ActivityDetailComposable
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

class ActivityDetailActivity : AppCompatActivity() {
    private val extActivityId: String by lazy { intent.getStringExtra(EXT_ACTIVITY_ID).orEmpty() }

    private val activityDetailsViewModel: ActivityDetailsViewModel by lazy {
        (application as MyRunApp).appComponent
            .activityDetailsComponent(ActivityDetailsModule(extActivityId))
            .activityDetailsViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ActivityDetailComposable(activityDetailsViewModel)
        }
    }

    companion object {
        private const val EXT_ACTIVITY_ID = "EXT_ACTIVITY_ID"

        fun createIntent(context: Context, activityId: String) = Intent(
            context,
            ActivityDetailActivity::class.java
        ).apply { putExtra(EXT_ACTIVITY_ID, activityId) }
    }
}

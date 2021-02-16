package akio.apps.myrun.feature.activitydetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

class ActivityDetailActivity : AppCompatActivity() {

    private lateinit var extActivityId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        extActivityId = intent.getStringExtra(EXT_ACTIVITY_ID)
            ?: return

        setContent {
            ActivityDetailContent()
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

package akio.apps.myrun.feature.home.impl

import akio.apps.myrun.R
import akio.apps.myrun.feature.usertimeline.impl.UserTimelineFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity(R.layout.activity_home) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, UserTimelineFragment.instantiate())
                .commit()
        }
    }

    companion object {
        fun launchIntent(context: Context) = Intent(context, HomeActivity::class.java)

        fun clearTaskIntent(context: Context) = Intent(context, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
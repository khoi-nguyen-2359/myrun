package akio.apps.myrun.feature.home.impl

import akio.apps.myrun.R
import akio.apps.myrun.databinding.ActivityHomeBinding
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import akio.apps.myrun.feature.usertimeline.impl.UserTimelineFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class HomeActivity : AppCompatActivity() {

    private val viewBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews(savedInstanceState)
    }

    private fun initViews(savedInstanceState: Bundle?) {
        setContentView(viewBinding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, UserTimelineFragment.instantiate())
                .commit()
        }

        viewBinding.addNewButton.setOnClickListener { openRouteTrackingScreen() }
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)

        when (fragment) {
            is UserTimelineFragment -> setupUserTimelineActionBar()
        }
    }

    private fun setupUserTimelineActionBar() {
    }

    private fun openRouteTrackingScreen() {
        startActivity(RouteTrackingActivity.launchIntent(this))
    }

    companion object {
        fun launchIntent(context: Context) = Intent(context, HomeActivity::class.java)

        fun clearTaskIntent(context: Context) = Intent(context, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
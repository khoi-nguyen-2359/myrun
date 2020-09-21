package akio.apps.myrun.feature.usertimeline.impl

import akio.apps._base.di.BaseInjectionActivity
import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps.myrun.databinding.ActivityUserTimelineBinding
import akio.apps.myrun.feature._base.utils.ActivityDialogDelegate
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class UserTimelineActivity : BaseInjectionActivity() {

    private val viewBinding by lazy { ActivityUserTimelineBinding.inflate(layoutInflater) }

    private val viewModel: UserTimelineViewModel by lazy { getViewModel() }

    private val activityPagingAdapter: ActivityPagingAdapter = ActivityPagingAdapter()

    private val dialogDelegate by lazy { ActivityDialogDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initObservers() {
        observe(viewModel.myActivityList) {
            lifecycleScope.launch {
                activityPagingAdapter.submitData(it)
            }
        }

        observeEvent(viewModel.error, dialogDelegate::showExceptionAlert)
    }

    private fun initViews() {
        viewBinding.apply {
            setContentView(root)
            addNewButton.setOnClickListener { openRouteTrackingScreen() }
            activityRecyclerView.adapter = activityPagingAdapter.withLoadStateFooter(
                footer = ActivityLoadStateAdapter()
            )
        }
    }

    private fun openRouteTrackingScreen() {
        finish()
        startActivity(RouteTrackingActivity.launchIntent(this))
    }

    companion object {
        fun launchIntent(context: Context) = Intent(context, UserTimelineActivity::class.java)
    }
}
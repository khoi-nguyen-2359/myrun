package akio.apps.myrun.feature.usertimeline.impl

import akio.apps._base.di.BaseInjectionActivity
import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps.myrun.databinding.ActivityUserTimelineBinding
import akio.apps.myrun.feature._base.utils.DialogDelegate
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserTimelineActivity : BaseInjectionActivity() {

    private val viewBinding by lazy { ActivityUserTimelineBinding.inflate(layoutInflater) }

    private val viewModel: UserTimelineViewModel by lazy { getViewModel() }

    private val activityPagingAdapter: ActivityPagingAdapter = ActivityPagingAdapter()

    private val dialogDelegate by lazy { DialogDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initObservers() {
        observe(viewModel.myActivityList) {
            activityPagingAdapter.submitData(this@UserTimelineActivity.lifecycle, it)
        }

        observeEvent(viewModel.error, dialogDelegate::showExceptionAlert)
    }

    private fun initViews() {
        viewBinding.apply {
            setContentView(root)
            addNewButton.setOnClickListener { openRouteTrackingScreen() }
            activityRecyclerView.adapter = activityPagingAdapter.withLoadStateFooter(
                footer = ActivityLoadStateAdapter(activityPagingAdapter::retry)
            )

            lifecycleScope.launch {
                activityPagingAdapter.loadStateFlow.collectLatest {
                    if (it.refresh is LoadState.NotLoading && activityPagingAdapter.itemCount == 0) {
                        viewBinding.welcomeTextView.visibility = View.VISIBLE
                    } else {
                        viewBinding.welcomeTextView.visibility = View.GONE
                    }
                }
            }
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
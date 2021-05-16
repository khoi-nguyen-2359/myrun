package akio.apps.myrun.feature.usertimeline.impl

import akio.apps._base.lifecycle.observe
import akio.apps._base.ui.ViewBindingDelegate
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.DialogDelegate
import akio.apps.myrun._di.viewModel
import akio.apps.myrun.databinding.FragmentUserTimelineBinding
import akio.apps.myrun.feature.activitydetail.ActivityDetailActivity
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import akio.apps.myrun.feature.usertimeline._di.DaggerUserTimelineFeatureComponent
import akio.apps.myrun.feature.usertimeline.model.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserTimelineFragment : Fragment(R.layout.fragment_user_timeline) {

    private val viewBinding by ViewBindingDelegate(FragmentUserTimelineBinding::bind)

    private val viewModel: UserTimelineViewModel by viewModel {
        DaggerUserTimelineFeatureComponent.factory().create(requireActivity().application)
    }

    private val selectActivityAction: (Activity) -> Unit = { activity ->
        openActivityDetail(activity)
    }

    private fun openActivityDetail(activity: Activity) {
        ActivityDetailActivity.createIntent(
            requireContext(),
            activity.id
        ).also(::startActivity)
    }

    private val activityPagingAdapter: ActivityPagingAdapter = ActivityPagingAdapter(
        selectActivityAction
    )

    private val dialogDelegate by lazy { DialogDelegate(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

//    private fun initObservers() {
//        observe(viewModel.myActivityList) {
//            activityPagingAdapter.submitData(lifecycle, it)
//        }
//    }
//
//    private fun initViews() = viewBinding.apply {
//        activityRecyclerView.adapter = activityPagingAdapter.withLoadStateFooter(
//            footer = ActivityLoadStateAdapter(activityPagingAdapter::retry)
//        )
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            activityPagingAdapter.loadStateFlow.collectLatest {
//                if (it.refresh is LoadState.NotLoading && activityPagingAdapter.itemCount == 0) {
//                    welcomeTextView.visibility = View.VISIBLE
//                } else {
//                    welcomeTextView.visibility = View.GONE
//                }
//            }
//        }
//    }

    companion object {
        fun instantiate(): UserTimelineFragment = UserTimelineFragment()
    }
}

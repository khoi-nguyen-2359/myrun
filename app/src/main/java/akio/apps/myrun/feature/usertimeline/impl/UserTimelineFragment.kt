package akio.apps.myrun.feature.usertimeline.impl

import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps._base.ui.ViewBindingDelegate
import akio.apps.myrun.R
import akio.apps.myrun._base.utils.DialogDelegate
import akio.apps.myrun._di.getViewModel
import akio.apps.myrun.databinding.FragmentUserTimelineBinding
import akio.apps.myrun.feature.usertimeline.UserTimelineViewModel
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserTimelineFragment : Fragment(R.layout.fragment_user_timeline) {

    private val viewBindingDelegate = ViewBindingDelegate(FragmentUserTimelineBinding::bind)
    private val viewBinding by viewBindingDelegate

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: UserTimelineViewModel by lazy {
        getViewModel(viewModelFactory, this)
    }

    private val activityPagingAdapter: ActivityPagingAdapter = ActivityPagingAdapter()

    private val dialogDelegate by lazy { DialogDelegate(requireContext()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initObservers() {
        observe(viewModel.myActivityList) {
            activityPagingAdapter.submitData(lifecycle, it)
        }

        observeEvent(viewModel.error, dialogDelegate::showExceptionAlert)
    }

    private fun initViews() = viewBinding.apply {
        activityRecyclerView.adapter = activityPagingAdapter.withLoadStateFooter(
            footer = ActivityLoadStateAdapter(activityPagingAdapter::retry)
        )

        viewLifecycleOwner.lifecycleScope.launch {
            activityPagingAdapter.loadStateFlow.collectLatest {
                if (it.refresh is LoadState.NotLoading && activityPagingAdapter.itemCount == 0) {
                    welcomeTextView.visibility = View.VISIBLE
                } else {
                    welcomeTextView.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBindingDelegate.release()
    }

    companion object {
        fun instantiate(): UserTimelineFragment = UserTimelineFragment()
    }
}

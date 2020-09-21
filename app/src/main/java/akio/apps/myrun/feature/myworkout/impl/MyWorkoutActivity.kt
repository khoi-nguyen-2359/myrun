package akio.apps.myrun.feature.myworkout.impl

import akio.apps._base.di.BaseInjectionActivity
import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps.myrun.R
import akio.apps.myrun.databinding.ActivityMyWorkoutBinding
import akio.apps.myrun.feature._base.utils.ActivityDialogDelegate
import akio.apps.myrun.feature.myworkout.MyWorkoutViewModel
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MyWorkoutActivity : BaseInjectionActivity() {

    private val viewBinding by lazy { ActivityMyWorkoutBinding.inflate(layoutInflater) }

    private val viewModel: MyWorkoutViewModel by lazy { getViewModel() }

    private val workoutPagingAdapter: WorkoutPagingAdapter = WorkoutPagingAdapter()

    private val dialogDelegate by lazy { ActivityDialogDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initObservers() {
        observe(viewModel.myWorkoutList) {
            lifecycleScope.launch {
                workoutPagingAdapter.submitData(it)
            }
        }

        observeEvent(viewModel.error, dialogDelegate::showExceptionAlert)
    }

    private fun initViews() {
        viewBinding.apply {
            setContentView(root)
            addNewButton.setOnClickListener { openRouteTrackingScreen() }
            workoutRecyclerView.adapter = workoutPagingAdapter.withLoadStateFooter(
                footer = WorkoutLoadStateAdapter()
            )
        }
    }

    private fun openRouteTrackingScreen() {
        finish()
        startActivity(RouteTrackingActivity.launchIntent(this))
    }

    companion object {
        fun launchIntent(context: Context) = Intent(context, MyWorkoutActivity::class.java)
    }
}
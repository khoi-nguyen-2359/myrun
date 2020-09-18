package akio.apps.myrun.feature.myworkout.impl

import akio.apps._base.activity.BaseInjectionActivity
import akio.apps._base.lifecycle.observe
import akio.apps._base.lifecycle.observeEvent
import akio.apps.myrun.databinding.ActivityMyWorkoutBinding
import akio.apps.myrun.feature._base.ActivityDialogDelegate
import akio.apps.myrun.feature.myworkout.MyWorkoutViewModel
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import android.content.Context
import android.content.Intent
import android.os.Bundle

class MyWorkoutActivity: BaseInjectionActivity() {

    private val viewBinding by lazy { ActivityMyWorkoutBinding.inflate(layoutInflater) }

    private val viewModel: MyWorkoutViewModel by lazy { getViewModel() }

    private val workoutAdapter: WorkoutAdapter = WorkoutAdapter()

    private val dialogDelegate by lazy { ActivityDialogDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViews()
        initObservers()
    }

    private fun initObservers() {
        observe(viewModel.myWorkoutList) {
            workoutAdapter.submitList(it)
        }

        observeEvent(viewModel.error, dialogDelegate::showExceptionAlert)
    }

    private fun initViews() {
        viewBinding.apply {
            setContentView(root)
            recordButton.root.setOnClickListener { openRouteTrackingScreen() }
            myWorkoutRecyclerView.adapter = workoutAdapter
        }
    }

    private fun openRouteTrackingScreen() {
        startActivity(RouteTrackingActivity.launchIntent(this))
    }

    companion object {
        fun launchIntent(context: Context) = Intent(context, MyWorkoutActivity::class.java)
    }
}
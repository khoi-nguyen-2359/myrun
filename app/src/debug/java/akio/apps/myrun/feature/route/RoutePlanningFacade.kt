package akio.apps.myrun.feature.route

import akio.apps.myrun.feature.route.ui.RoutePlanningActivity
import android.content.Context

object RoutePlanningFacade {
    fun startRoutePlanning(context: Context) {
        val intent = RoutePlanningActivity.addNewRouteIntent(context)
        context.startActivity(intent)
    }
}

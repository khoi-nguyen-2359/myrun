package akio.apps.myrun.feature.feed.ui

import akio.apps.myrun.feature.core.navigation.HomeNavDestination
import akio.apps.myrun.feature.core.navigation.HomeTabNavDestination
import androidx.navigation.NavController

class FeedNavigator(val appNavController: NavController, val homeTabController: NavController) {

    fun navigateUserPreferences() {
        appNavController.navigate(HomeNavDestination.UserPreferences.route)
    }

    fun navigateNormalUserStats(userId: String) {
        val route = HomeNavDestination.NormalUserStats.routeWithUserId(userId)
        appNavController.navigate(route)
    }

    fun navigateUserStats(isCurrentUser: Boolean, userId: String) {
        if (isCurrentUser) {
            navigateCurrentUserStats()
        } else {
            navigateNormalUserStats(userId)
        }
    }

    fun navigateActivityDetail(activityId: String) {
        val route = HomeNavDestination.ActivityDetail.routeWithActivityId(activityId)
        appNavController.navigate(route)
    }

    private fun navigateCurrentUserStats() {
        val route = HomeTabNavDestination.Stats.route
        homeTabController.navigate(route)
    }
}

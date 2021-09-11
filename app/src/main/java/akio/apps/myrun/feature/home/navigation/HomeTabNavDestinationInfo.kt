package akio.apps.myrun.feature.home.navigation

import akio.apps.myrun.feature.base.navigation.NavDestinationInfo

sealed class HomeTabNavDestinationInfo(routeName: String) : NavDestinationInfo(routeName) {
    object ActivityFeedTab : HomeTabNavDestinationInfo(routeName = "activityFeed")
    object UserHomeTab : HomeTabNavDestinationInfo(routeName = "userHome")
}

package akio.apps.myrun.feature.routetracking

import akio.apps.myrun.feature.routetracking.impl.RouteTrackingActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface TrackingFeatureModule {
    @ContributesAndroidInjector
    fun trackingActivity(): RouteTrackingActivity
}
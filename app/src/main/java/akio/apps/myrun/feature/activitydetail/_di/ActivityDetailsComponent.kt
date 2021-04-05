package akio.apps.myrun.feature.activitydetail._di

import akio.apps.myrun.feature.activitydetail.ActivityDetailsViewModel
import dagger.Subcomponent

@Subcomponent(modules = [ActivityDetailsModule::class])
interface ActivityDetailsComponent {
    fun activityDetailsViewModel(): ActivityDetailsViewModel
}

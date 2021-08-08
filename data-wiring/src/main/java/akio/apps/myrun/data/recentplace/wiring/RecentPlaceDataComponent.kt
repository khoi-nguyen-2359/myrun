package akio.apps.myrun.data.recentplace.wiring

import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import dagger.Component

@Component(modules = [RecentPlaceDataModule::class, DispatchersModule::class])
interface RecentPlaceDataComponent {
    fun recentPlaceRepository(): UserRecentPlaceRepository
}

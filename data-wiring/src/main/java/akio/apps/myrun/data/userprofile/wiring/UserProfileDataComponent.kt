package akio.apps.myrun.data.userprofile.wiring

import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data._base.wiring.FirebaseDataModule
import akio.apps.myrun.data.userprofile.UserProfileRepository
import dagger.Component

@Component(
    modules = [UserProfileDataModule::class, FirebaseDataModule::class, DispatchersModule::class]
)
interface UserProfileDataComponent {
    fun userProfileRepository(): UserProfileRepository
}

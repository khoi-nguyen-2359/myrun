package akio.apps.myrun.data.user.wiring

import akio.apps.myrun.data.user.api.AppMigrationState
import akio.apps.myrun.data.user.api.UserFollowRepository
import akio.apps.myrun.data.user.api.UserProfileRepository
import akio.apps.myrun.data.user.api.UserRecentPlaceRepository
import akio.apps.myrun.data.user.impl.FirebaseUserFollowRepository
import akio.apps.myrun.data.user.impl.FirebaseUserProfileRepository
import akio.apps.myrun.data.user.impl.FirebaseUserRecentPlaceRepository
import akio.apps.myrun.data.user.impl.PreferenceAppMigrationState
import akio.apps.myrun.data.wiring.ApplicationModule
import akio.apps.myrun.data.wiring.DispatchersModule
import akio.apps.myrun.data.wiring.FirebaseDataModule
import dagger.Binds
import dagger.Component
import dagger.Module

@Component(
    modules = [
        UserDataModule::class,
        FirebaseDataModule::class,
        DispatchersModule::class,
        ApplicationModule::class
    ]
)
interface UserDataComponent {
    fun userRecentPlaceRepository(): UserRecentPlaceRepository
    fun userFollowRepository(): UserFollowRepository
    fun userProfileRepository(): UserProfileRepository
    fun appMigrationState(): AppMigrationState
}

@Module
internal interface UserDataModule {
    @Binds
    fun userProfileRepo(repo: FirebaseUserProfileRepository): UserProfileRepository

    @Binds
    fun userFollowRepository(firebaseUserFollowRepository: FirebaseUserFollowRepository):
        UserFollowRepository

    @Binds
    fun recentPlaceRepo(firebaseRecentPlaceRepository: FirebaseUserRecentPlaceRepository):
        UserRecentPlaceRepository

    @Binds
    fun appVersionMigrationState(impl: PreferenceAppMigrationState): AppMigrationState
}

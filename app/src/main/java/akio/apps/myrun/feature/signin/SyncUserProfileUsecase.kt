package akio.apps.myrun.feature.signin

/**
 * In case user doesn't make a profile update in onboarding process (for new user), this usecase will clone info from auth account to user profile.
 */
interface SyncUserProfileUsecase {
    suspend fun syncUserProfile()
}

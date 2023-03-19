package akio.apps.myrun.data.user.api

import akio.apps.myrun.data.user.api.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getUserPreferencesFlow(userId: String): Flow<UserPreferences>
    fun setShowActivityMapOnFeed(userId: String, isShow: Boolean)
}

package akio.apps.myrun.data.tracking.api

import kotlinx.coroutines.flow.Flow

interface LocationPresentConfiguration {
    suspend fun setBSplinesEnabled(isEnabled: Boolean)
    fun isBSplinesEnabledFlow(): Flow<Boolean>
}

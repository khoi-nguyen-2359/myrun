package akio.apps.myrun.data.user.api

import akio.apps.myrun.data.user.api.model.MeasureSystem
import kotlinx.coroutines.flow.Flow

interface UserPreferences {
    fun getMeasureSystem(): Flow<MeasureSystem>
    suspend fun setMeasureSystem(measureSystem: MeasureSystem)
}

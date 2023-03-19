package akio.apps.myrun.data.user.api

import akio.apps.myrun.data.user.api.model.MeasureSystem
import kotlinx.coroutines.flow.Flow

interface CurrentUserPreferences {
    fun getMeasureSystemFlow(): Flow<MeasureSystem>

    suspend fun setMeasureSystem(measureSystem: MeasureSystem)
}

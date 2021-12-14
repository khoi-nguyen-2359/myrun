package akio.apps.myrun.domain.activity.api

import akio.apps.myrun.domain.activity.api.model.ActivityModel

interface UploadActivitiesUsecase {
    suspend fun uploadAll(onUploadActivityStarted: ((ActivityModel) -> Unit)? = null): Boolean
}

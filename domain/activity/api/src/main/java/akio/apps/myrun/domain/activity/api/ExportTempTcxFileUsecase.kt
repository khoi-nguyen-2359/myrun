package akio.apps.myrun.domain.activity.api

import java.io.File

interface ExportTempTcxFileUsecase {
    suspend operator fun invoke(activityId: String): File?
}

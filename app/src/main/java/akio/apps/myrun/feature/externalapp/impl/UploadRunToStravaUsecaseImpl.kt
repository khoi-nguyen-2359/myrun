package akio.apps.myrun.feature.externalapp.impl

import akio.apps.myrun.data.externalapp.StravaApi
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.feature.externalapp.UploadRunToStravaUsecase
import akio.apps.myrun._base.runfile.TCX_MIME_TYPE
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class UploadRunToStravaUsecaseImpl @Inject constructor(
    private val stravaTokenStorage: StravaTokenStorage,
    private val stravaApi: StravaApi
) : UploadRunToStravaUsecase {
    override suspend fun uploadRunToStrava(runTitle: String, runFile: File) {
        val stravaToken = stravaTokenStorage.getToken()
            ?: return

        val uploadFile = runFile.asRequestBody(TCX_MIME_TYPE.toMediaType())
        val uploadBody = MultipartBody.Part.createFormData("file", runFile.name, uploadFile)
        val runNamePart = runTitle.toRequestBody(MultipartBody.FORM)
        val dataTypePart = "tcx".toRequestBody(MultipartBody.FORM)

        stravaApi.uploadRun("Bearer ${stravaToken.accessToken}", uploadBody, runNamePart, dataTypePart)
    }
}
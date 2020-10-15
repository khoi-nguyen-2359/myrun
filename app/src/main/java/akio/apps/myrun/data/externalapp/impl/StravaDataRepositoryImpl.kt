package akio.apps.myrun.data.externalapp.impl

import akio.apps.myrun._base.runfile.TCX_MIME_TYPE
import akio.apps.myrun.data.externalapp.StravaDataRepository
import akio.apps.myrun.data.externalapp.StravaTokenStorage
import akio.apps.myrun.data.externalapp.mapper.StravaRouteEntityMapper
import akio.apps.myrun.data.externalapp.model.StravaRoute
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class StravaDataRepositoryImpl @Inject constructor(
    private val stravaApi: StravaApi,
    private val stravaTokenStorage: StravaTokenStorage,
    private val stravaRouteEntityMapper: StravaRouteEntityMapper
) : StravaDataRepository {
    override suspend fun saveActivity(activityTitle: String, activityFile: File) {
        val stravaToken = stravaTokenStorage.getToken()
            ?: return

        val uploadFile = activityFile.asRequestBody(TCX_MIME_TYPE.toMediaType())
        val uploadBody = MultipartBody.Part.createFormData("file", activityFile.name, uploadFile)
        val runNamePart = activityTitle.toRequestBody(MultipartBody.FORM)
        val dataTypePart = "tcx".toRequestBody(MultipartBody.FORM)

        stravaApi.uploadActivity("Bearer ${stravaToken.accessToken}", uploadBody, runNamePart, dataTypePart)
    }

    override suspend fun getRoutes(athleteId: Long): List<StravaRoute> {
        val stravaToken = stravaTokenStorage.getToken()
            ?: return emptyList()

        return stravaApi.getAthleteRoutes("Bearer ${stravaToken.accessToken}", stravaToken.athlete.id)
            .map(stravaRouteEntityMapper::map)
    }
}
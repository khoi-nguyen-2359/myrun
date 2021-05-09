package akio.apps.myrun.data.externalapp.impl

import akio.apps.myrun.data.externalapp.StravaDataRepository
import akio.apps.myrun.data.externalapp.mapper.StravaRouteEntityMapper
import akio.apps.myrun.data.externalapp.model.ExternalAppToken
import akio.apps.myrun.data.externalapp.model.StravaRoute
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class StravaDataRepositoryImpl @Inject constructor(
    private val stravaApi: StravaApi,
    private val stravaRouteEntityMapper: StravaRouteEntityMapper
) : StravaDataRepository {
    override suspend fun saveActivity(
        stravaToken: ExternalAppToken.StravaToken,
        activityTitle: String,
        activityFile: File
    ) {
        val uploadFile = activityFile.asRequestBody(TCX_MIME_TYPE.toMediaType())
        val uploadBody = MultipartBody.Part.createFormData("file", activityFile.name, uploadFile)
        val runNamePart = activityTitle.toRequestBody(MultipartBody.FORM)
        val dataTypePart = "tcx".toRequestBody(MultipartBody.FORM)

        stravaApi.uploadActivity(
            "Bearer ${stravaToken.accessToken}",
            uploadBody,
            runNamePart,
            dataTypePart
        )
    }

    override suspend fun getRoutes(stravaToken: ExternalAppToken.StravaToken): List<StravaRoute> {
        return stravaApi.getAthleteRoutes(
            "Bearer ${stravaToken.accessToken}",
            stravaToken.athlete.id
        )
            .map(stravaRouteEntityMapper::map)
    }

    companion object {
        const val TCX_MIME_TYPE = "application/vnd.garmin.tcx+xml"
    }
}

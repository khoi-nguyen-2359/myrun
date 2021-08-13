package akio.apps.myrun.data.eapps.impl

import akio.apps.myrun.data.eapps.api.model.ExternalAppToken
import akio.apps.myrun.data.eapps.api.model.StravaRouteModel
import akio.apps.myrun.data.eapps.impl.mapper.StravaStravaRouteMapper
import java.io.File
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class StravaDataRepositoryImpl @Inject constructor(
    private val stravaApi: StravaApi,
    private val stravaRouteMapper: StravaStravaRouteMapper,
) : akio.apps.myrun.data.eapps.api.StravaDataRepository {
    override suspend fun saveActivity(
        stravaToken: ExternalAppToken.StravaToken,
        activityTitle: String,
        activityFile: File,
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

    override suspend fun getRoutes(
        stravaToken: ExternalAppToken.StravaToken,
    ): List<StravaRouteModel> {
        return stravaApi.getAthleteRoutes(
            "Bearer ${stravaToken.accessToken}",
            stravaToken.athlete.id
        )
            .map(stravaRouteMapper::map)
    }

    companion object {
        const val TCX_MIME_TYPE = "application/vnd.garmin.tcx+xml"
    }
}

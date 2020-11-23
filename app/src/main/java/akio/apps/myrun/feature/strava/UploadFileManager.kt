package akio.apps.myrun.feature.strava

import java.io.File

interface UploadFileManager {
    fun createUploadFile(fileId: String): File
    fun getNextUploadFile(): File?
}
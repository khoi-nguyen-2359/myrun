package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun.feature.strava.UploadFileManager
import java.io.File

class UploadFileManagerImpl(
    private val fileDir: File
) : UploadFileManager {

    private val prefix = "upload_"

    override fun createUploadFile(fileId: String): File {
        return File.createTempFile("$prefix$fileId", null, fileDir)
    }

    override fun getNextUploadFile(): File? {
        return fileDir.listFiles()?.firstOrNull { it.name.startsWith(prefix) }
    }
}
package akio.apps.myrun.feature.strava.impl

import akio.apps.myrun.feature.strava.ActivityFileManager
import android.content.Context
import java.io.File
import javax.inject.Inject

class ActivityFileManagerImpl @Inject constructor(
    appContext: Context
) : ActivityFileManager {

    private val fileDir: File = File(appContext.filesDir, "activity/")

    private val prefix = "activity_"

    override fun createFile(fileId: String): File {
        return File.createTempFile("$prefix$fileId", null, fileDir)
    }

    override fun getAllFiles(): List<File> {
        return fileDir.listFiles()?.filter {
            it.isFile && it.exists() && it.name.startsWith(prefix)
        }
            ?: emptyList()
    }
}
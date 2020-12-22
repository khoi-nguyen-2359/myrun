package akio.apps.myrun.feature.strava

import java.io.File

interface ActivityFileManager {
    fun createFile(fileId: String): File
    fun getAllFiles(): List<File>
}
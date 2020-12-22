package akio.apps.myrun._base.runfile

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipFileSerializer(file: File, entryExtension: String): CloseableSerializer {
	
	private val zipOutputStream: ZipOutputStream
	private val streamWriter: OutputStreamWriter
	
	init {
		val fileOutputStream = FileOutputStream(file)
		val bufferedStream = BufferedOutputStream(fileOutputStream)
		zipOutputStream = ZipOutputStream(bufferedStream)
		zipOutputStream.putNextEntry(ZipEntry(file.nameWithoutExtension + entryExtension))
		streamWriter = OutputStreamWriter(zipOutputStream)
	}

	@Suppress("DEPRECATION")
	override fun print(line: String) {
		try {
			streamWriter.write(line)
			streamWriter.appendln()
		} catch (ignore: IOException) {
		}
	}
	
	override fun close() {
		streamWriter.flush()
		zipOutputStream.closeEntry()
		streamWriter.close()
	}
}
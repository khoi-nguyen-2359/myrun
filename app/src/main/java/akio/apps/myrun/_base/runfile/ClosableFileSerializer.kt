package akio.apps.myrun.helper.runfile

import com.sweetzpot.tcxzpot.serializers.FileSerializer
import java.io.File

class ClosableFileSerializer(file: File): FileSerializer(file), CloseableSerializer {
	override fun close() {
		save()
	}
}
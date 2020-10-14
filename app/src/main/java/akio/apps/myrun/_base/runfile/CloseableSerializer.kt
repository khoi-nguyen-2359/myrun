package akio.apps.myrun.helper.runfile

import com.sweetzpot.tcxzpot.Serializer
import java.io.Closeable

interface CloseableSerializer: Closeable, Serializer {
}
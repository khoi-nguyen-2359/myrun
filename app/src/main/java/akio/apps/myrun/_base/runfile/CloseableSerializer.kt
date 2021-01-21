package akio.apps.myrun._base.runfile

import com.sweetzpot.tcxzpot.Serializer
import java.io.Closeable

interface CloseableSerializer : Closeable, Serializer

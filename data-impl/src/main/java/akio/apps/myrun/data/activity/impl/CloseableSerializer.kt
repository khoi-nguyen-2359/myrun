package akio.apps.myrun.data.activity.impl

import com.sweetzpot.tcxzpot.Serializer
import java.io.Closeable

interface CloseableSerializer : Closeable, Serializer

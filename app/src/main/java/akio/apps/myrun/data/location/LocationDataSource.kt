package akio.apps.myrun.data.location

import android.location.Location

interface LocationDataSource {
    suspend fun getLastLocation(): Location
}
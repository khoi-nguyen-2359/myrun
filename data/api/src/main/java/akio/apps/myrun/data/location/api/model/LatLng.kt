package akio.apps.myrun.data.location.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LatLng(
    val latitude: Double,
    val longitude: Double
) : Parcelable

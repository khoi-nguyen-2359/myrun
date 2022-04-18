package akio.apps.myrun.feature.route.model

import android.net.Uri
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class Checkpoint(
    val position: LatLng,
    val distance: Double,
    var updatePhotoContentUri: Uri? = null,
    var currentPhoto: String? = null,
) : Parcelable

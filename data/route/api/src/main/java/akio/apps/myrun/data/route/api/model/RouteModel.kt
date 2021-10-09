package akio.apps.myrun.data.route.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RouteModel(
    val routeId: String,
    val routeName: String
) : Parcelable

package akio.apps.myrun.feature.routetracking

import android.location.Location

interface GetMapInitialLocationUsecase {
    suspend fun getMapInitialLocation(): Location
}
package akio.apps.myrun.domain.tracking.api

import android.graphics.Bitmap

interface StoreTrackingActivityDataUsecase {
    suspend operator fun invoke(activityName: String, routeImageBitmap: Bitmap)
}

package akio.apps.myrun.feature.strava

import akio.apps.myrun.data.activity.ActivityEntity
import akio.apps.myrun.data.fitness.SingleDataPoint
import akio.apps.myrun.data.location.LocationEntity
import java.io.File

interface ActivityFileExporter {
    fun export(
        saveDir: File,
        activity: ActivityEntity,
        locationDataPoints: List<SingleDataPoint<LocationEntity>>,
        speedDataPoints: List<SingleDataPoint<Float>>,
        stepCadenceDataPoints: List<SingleDataPoint<Int>>?,
        zip: Boolean
    )
}
package akio.apps.myrun.domain.activity.api

import akio.apps.myrun.domain.activity.api.model.ActivityLocation

interface RunSplitsCalculator {
    fun createRunSplits(locationDataPoints: List<ActivityLocation>): List<Double>
}

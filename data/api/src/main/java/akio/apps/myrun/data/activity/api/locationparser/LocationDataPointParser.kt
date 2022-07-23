package akio.apps.myrun.data.activity.api.locationparser

import akio.apps.myrun.data.activity.api.model.ActivityLocation
import akio.apps.myrun.data.activity.api.model.DataPointVersion

sealed interface LocationDataPointParser {
    fun flatten(dataPoint: List<ActivityLocation>): List<Double>
    fun build(firestoreDataPoints: List<Double>): List<ActivityLocation>
}

object LocationDataPointParserFactory {
    private val parserRegistry: Map<DataPointVersion, LocationDataPointParser> = mapOf(
        DataPointVersion.V1 to LocationDataPointParserV1()
    )

    fun getParser(version: Int): LocationDataPointParser {
        val dataPointVersion = DataPointVersion.fromValue(version)
        return parserRegistry[dataPointVersion] ?: LocationDataPointParserV1()
    }

    fun getWriteParser(): LocationDataPointParser = getParser(DataPointVersion.max().value)
}

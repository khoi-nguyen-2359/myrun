package akio.apps.myrun.domain.activity.api

interface ActivityDateTimeFormatter {
    fun formatActivityDateTime(startTime: Long): Result

    sealed class Result(open val formattedValue: String) {
        class WithinToday(formattedValue: String) : Result(formattedValue)
        class WithinYesterday(formattedValue: String) : Result(formattedValue)
        class FullDateTime(formattedValue: String) : Result(formattedValue)
    }
}

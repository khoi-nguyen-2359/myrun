package akio.apps.myrun.domain.activity

import akio.apps.myrun.domain.time.TimeProvider
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ActivityDateTimeFormatter @Inject constructor(
    private val timeProvider: TimeProvider
) {
    private val timeFormatter: SimpleDateFormat = SimpleDateFormat("h:mm a")
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")

    fun formatActivityDateTime(startTime: Long): Result {
        val todayDays = TimeUnit.MILLISECONDS.toDays(timeProvider.currentTimeMillisWithOffset())
        val activityDays = TimeUnit.MILLISECONDS.toDays(startTime + timeProvider.rawOffset())
        val formattedTime = timeFormatter.format(startTime)
        return when {
            activityDays == todayDays -> Result.WithinToday(formattedTime)
            todayDays - activityDays == 1L -> Result.WithinYesterday(formattedTime)
            else -> {
                val formattedDate = dateFormatter.format(startTime)
                Result.FullDateTime("$formattedDate $formattedTime")
            }
        }
    }

    sealed class Result(open val formattedValue: String) {
        class WithinToday(formattedValue: String) : Result(formattedValue)
        class WithinYesterday(formattedValue: String) : Result(formattedValue)
        class FullDateTime(formattedValue: String) : Result(formattedValue)
    }
}

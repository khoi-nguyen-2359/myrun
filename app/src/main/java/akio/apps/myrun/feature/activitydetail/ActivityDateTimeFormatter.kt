package akio.apps.myrun.feature.activitydetail

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ActivityDateTimeFormatter @Inject constructor() {

    private val timeFormatter: SimpleDateFormat = SimpleDateFormat("h:mm a")
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")

    fun formatActivityDateTime(startTime: Long): Result {
        val calendar = Calendar.getInstance()
        val todayDate = TimeUnit.MILLISECONDS.toDays(calendar.timeInMillis)
        val activityDate = TimeUnit.MILLISECONDS.toDays(startTime)
        val formattedTime = timeFormatter.format(Date(startTime))
        return when {
            activityDate == todayDate -> Result.WithinToday(formattedTime)
            todayDate - activityDate == 1L -> Result.WithinYesterday(formattedTime)
            else -> {
                val formattedDate = dateFormatter.format(Date(startTime))
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

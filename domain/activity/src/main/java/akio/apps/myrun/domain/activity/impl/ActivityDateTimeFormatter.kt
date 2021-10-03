package akio.apps.myrun.domain.activity.impl

import akio.apps.myrun.data.time.Now
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ActivityDateTimeFormatter @Inject constructor() {

    private val timeFormatter: SimpleDateFormat = SimpleDateFormat("h:mm a")
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")

    fun formatActivityDateTime(startTime: Long): Result {
        val todayDate = TimeUnit.MILLISECONDS.toDays(Now.currentTimeMillis())
        val activityDate = TimeUnit.MILLISECONDS.toDays(startTime)
        val formattedTime = timeFormatter.format(startTime)
        return when {
            activityDate == todayDate -> Result.WithinToday(formattedTime)
            todayDate - activityDate == 1L -> Result.WithinYesterday(formattedTime)
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

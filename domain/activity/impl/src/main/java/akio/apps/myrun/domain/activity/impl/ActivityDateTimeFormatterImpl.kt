package akio.apps.myrun.domain.activity.impl

import akio.apps.myrun.data.time.Now
import akio.apps.myrun.domain.activity.api.ActivityDateTimeFormatter
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ActivityDateTimeFormatterImpl @Inject constructor() : ActivityDateTimeFormatter {
    private val timeFormatter: SimpleDateFormat = SimpleDateFormat("h:mm a")
    private val dateFormatter: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy")

    override fun formatActivityDateTime(startTime: Long): ActivityDateTimeFormatter.Result {
        val todayDate = TimeUnit.MILLISECONDS.toDays(Now.currentTimeMillis())
        val activityDate = TimeUnit.MILLISECONDS.toDays(startTime)
        val formattedTime = timeFormatter.format(startTime)
        return when {
            activityDate == todayDate -> ActivityDateTimeFormatter.Result.WithinToday(formattedTime)
            todayDate - activityDate == 1L -> ActivityDateTimeFormatter.Result.WithinYesterday(
                formattedTime
            )
            else -> {
                val formattedDate = dateFormatter.format(startTime)
                ActivityDateTimeFormatter.Result.FullDateTime("$formattedDate $formattedTime")
            }
        }
    }
}

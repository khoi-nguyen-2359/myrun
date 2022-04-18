package akio.apps.myrun.log

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.R
import akio.apps.myrun.feature.core.AppNotificationChannel
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class MyDebugTree(private val application: MyRunApp) : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val customizedTag = "MyRun/$tag"
        super.log(priority, customizedTag, message, t)

        if (priority == Log.ERROR && t != null) {
            recordException(message, t)
            notifyExceptionReport(t)
        }
    }

    private fun recordException(message: String, t: Throwable) {
        if (message.isNotEmpty()) {
            FirebaseCrashlytics.getInstance().log(message)
        }
        FirebaseCrashlytics.getInstance().recordException(t)
        FirebaseCrashlytics.getInstance().sendUnsentReports()
    }

    private fun notifyExceptionReport(cause: Throwable) {
        val stackTrace = cause.stackTraceToString()
        val notification = NotificationCompat.Builder(application, AppNotificationChannel.Debug.id)
            .setContentTitle("A ${cause::class.simpleName} was recorded.")
            .setContentText(stackTrace)
            .setSmallIcon(R.drawable.ic_run_circle)
            .build()
        NotificationManagerCompat.from(application)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}

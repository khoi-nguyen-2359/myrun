package akio.apps._base.utils

import akio.apps.myrun.MyRunApp
import akio.apps.myrun.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class MyDebugTree(private val application: MyRunApp) : Timber.DebugTree() {

    init {
        createNotificationChannel()
    }

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
        val notification = NotificationCompat.Builder(application, CHANNEL_ID)
            .setContentTitle("A ${cause::class.simpleName} was recorded.")
            .setContentText(stackTrace)
            .setSmallIcon(R.drawable.ic_run_circle)
            .build()
        NotificationManagerCompat.from(application)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Debug Error Report",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Debug mode only."
        val notificationManager = application.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "akio.apps._base.utils.MyDebugTree"
    }
}

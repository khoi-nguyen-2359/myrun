package akio.apps.myrun._base.configurator

import akio.apps.myrun.BuildConfig
import akio.apps.myrun.R
import akio.apps.myrun._base.notification.AppNotificationChannel
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object ConfiguratorGate {
    private const val CONFIGURATOR_ACTIVITY_NAME =
        "akio.apps.myrun.configurator.ConfiguratorActivity"
    private val NOTIFICATION_ID = AppNotificationChannel.Debug.nextNotificationStaticId()

    fun notifyInDebugMode(context: Context) {
        if (!BuildConfig.DEBUG) {
            return
        }

        val notMan = NotificationManagerCompat.from(context)
        val activityClass = Class.forName(CONFIGURATOR_ACTIVITY_NAME)
        val intent = Intent(context, activityClass)
        val pendingIntentFlag = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlag)
        val notification = NotificationCompat.Builder(context, AppNotificationChannel.Debug.id)
            .setContentTitle("My Run Configurator")
            .setAutoCancel(false)
            .setDefaults(Notification.DEFAULT_ALL)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_run_circle)
            .build()

        notMan.notify(NOTIFICATION_ID, notification)
    }
}

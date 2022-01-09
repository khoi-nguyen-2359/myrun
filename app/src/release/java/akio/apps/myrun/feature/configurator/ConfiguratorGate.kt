package akio.apps.myrun.feature.configurator

import akio.apps.myrun.BuildConfig
import akio.apps.myrun.R
import akio.apps.myrun.feature.base.AppNotificationChannel
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object ConfiguratorGate {
    @Suppress("UNUSED_PARAMETER")
    fun notifyInDebugMode(context: Context) {
        // do no enable configurator in release build.
    }
}

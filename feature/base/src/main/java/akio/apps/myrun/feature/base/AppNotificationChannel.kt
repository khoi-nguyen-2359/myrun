package akio.apps.myrun.feature.base

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat

enum class AppNotificationChannel(
    val id: String,
    val baseNotificationId: Int,
    val importance: Int,
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int
) {
    TrackingService(
        "TrackingService",
        100,
        NotificationManagerCompat.IMPORTANCE_DEFAULT,
        R.string.notification_channel_name_tracking_service,
        R.string.notification_channel_description_tracking_service
    ),

    General(
        "General",
        200,
        NotificationManagerCompat.IMPORTANCE_DEFAULT,
        R.string.notification_channel_name_general,
        R.string.notification_channel_description_general
    ),

    Debug(
        "Debug",
        300,
        NotificationManagerCompat.IMPORTANCE_MIN,
        R.string.notification_channel_name_debug,
        R.string.notification_channel_description_debug
    );

    private var currentStaticId = 0

    /**
     * Generates a new notification id for simple static usage, not to create id programmatically.
     */
    fun nextNotificationStaticId(): Int = baseNotificationId + currentStaticId++

    fun createChannelCompat(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(id, context.getString(nameRes), importance)
        channel.description = context.getString(descriptionRes)
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}

package com.njord.mobile.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.njord.mobile.MainActivity
import com.njord.mobile.R

object NjordLocalNotifier {
    private const val ChannelId = "njord_operations"

    fun notify(context: Context, notificationId: Int, content: LocalNotificationContent) {
        if (!canPostNotifications(context)) return
        val manager = context.getSystemService(NotificationManager::class.java)
        ensureChannel(manager)
        manager.notify(notificationId, buildNotification(context, content))
    }

    private fun buildNotification(context: Context, content: LocalNotificationContent): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(context, ChannelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(content.title)
            .setContentText(content.body)
            .setStyle(Notification.BigTextStyle().bigText(content.body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
    }

    private fun ensureChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            ChannelId,
            "Njord operations",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Local alerts for Njord incidents, activity, and heartbeat health."
        }
        manager.createNotificationChannel(channel)
    }

    private fun canPostNotifications(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
}

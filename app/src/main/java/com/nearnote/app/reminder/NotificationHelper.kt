package com.nearnote.app.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nearnote.app.R
import com.nearnote.app.data.model.ReminderTask
import kotlin.math.abs

class NotificationHelper(
    private val context: Context
) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "NearNote reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications triggered when you are near a saved place."
        }
        manager.createNotificationChannel(channel)
    }

    fun showReminder(task: ReminderTask, transitionLabel: String) {
        ensureChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val contentText = buildString {
            append(task.placeName)
            append(" • ")
            append(transitionLabel)
            if (task.note.isNotBlank()) {
                append("\n")
                append(task.note)
            }
        }

        val notificationId = abs(task.id.toInt())
        val completeIntent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_MARK_COMPLETED
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(task.title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(0, "Complete", completePendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    companion object {
        const val CHANNEL_ID = "nearnote-reminders"
        const val ACTION_MARK_COMPLETED = "com.nearnote.app.action.MARK_COMPLETED"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}

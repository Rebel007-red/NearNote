package com.nearnote.app.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(task.title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(abs(task.id.toInt()), notification)
    }

    companion object {
        const val CHANNEL_ID = "nearnote-reminders"
    }
}

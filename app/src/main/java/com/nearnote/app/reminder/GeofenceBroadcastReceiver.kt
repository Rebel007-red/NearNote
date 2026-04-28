package com.nearnote.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.nearnote.app.data.db.AppDatabase
import com.nearnote.app.data.repo.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NotificationHelper.ACTION_MARK_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val taskId = intent.getLongExtra(NotificationHelper.EXTRA_TASK_ID, -1L)
                    if (taskId > 0L) {
                        val repository = ReminderRepository(AppDatabase.getInstance(context).nearNoteDao())
                        repository.setTaskCompleted(taskId, true, System.currentTimeMillis())
                        GeofenceScheduler(context).remove(taskId)
                    }
                    val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, -1)
                    if (notificationId >= 0) {
                        NotificationManagerCompat.from(context).cancel(notificationId)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = ReminderRepository(AppDatabase.getInstance(context).nearNoteDao())
                val notifier = NotificationHelper(context)
                val transitionLabel = transitionLabel(event.geofenceTransition)
                val nowMs = System.currentTimeMillis()

                event.triggeringGeofences
                    ?.mapNotNull { geofence -> geofence.requestId.removePrefix("reminder-").toLongOrNull() }
                    ?.forEach { taskId ->
                        repository.getTaskById(taskId)?.takeIf { it.isEnabled && !it.isCompleted }?.let { task ->
                            if (shouldNotifyForRecurrence(task, nowMs)) {
                                notifier.showReminder(task, transitionLabel)
                                repository.setLastFiredAt(taskId, nowMs)
                            }
                        }
                    }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun transitionLabel(transition: Int): String {
        return when (transition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "Arrived nearby"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "Still nearby"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "Leaving area"
            else -> "Location reminder"
        }
    }

    private fun shouldNotifyForRecurrence(task: com.nearnote.app.data.model.ReminderTask, nowMs: Long): Boolean {
        if (task.recurrenceType == "ONCE") {
            return task.lastFiredAt == 0L
        }

        val lastFired = task.lastFiredAt
        if (lastFired == 0L) return true

        val cooldownMs = when (task.recurrenceType) {
            "DAILY" -> TimeUnit.DAYS.toMillis(1)
            "WEEKLY" -> TimeUnit.DAYS.toMillis(7)
            "MONTHLY" -> TimeUnit.DAYS.toMillis(30)
            "CUSTOM" -> (task.recurrenceInterval ?: 1).toLong() * TimeUnit.DAYS.toMillis(1)
            else -> 0L
        }

        return (nowMs - lastFired) >= cooldownMs
    }
}

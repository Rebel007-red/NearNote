package com.nearnote.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.nearnote.app.data.db.AppDatabase
import com.nearnote.app.data.repo.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = ReminderRepository(AppDatabase.getInstance(context).nearNoteDao())
                val notifier = NotificationHelper(context)
                val transitionLabel = transitionLabel(event.geofenceTransition)

                event.triggeringGeofences
                    ?.mapNotNull { geofence -> geofence.requestId.removePrefix("reminder-").toLongOrNull() }
                    ?.forEach { taskId ->
                        repository.getTaskById(taskId)?.takeIf { it.isEnabled }?.let { task ->
                            notifier.showReminder(task, transitionLabel)
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
}

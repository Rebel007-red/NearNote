package com.nearnote.app.reminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.nearnote.app.data.model.ReminderTask

class GeofenceScheduler(
    private val context: Context
) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    suspend fun refreshAll(tasks: List<ReminderTask>) {
        geofencingClient.removeGeofences(geofencePendingIntent)
        if (!hasLocationPermission()) return
        tasks.filter { it.isEnabled }.forEach { task ->
            upsert(task)
        }
    }

    suspend fun remove(taskId: Long) {
        geofencingClient.removeGeofences(listOf(requestId(taskId)))
    }

    @SuppressLint("MissingPermission")
    suspend fun upsert(task: ReminderTask) {
        if (!task.isEnabled || !hasLocationPermission()) return
        geofencingClient.removeGeofences(listOf(requestId(task.id)))
        geofencingClient.addGeofences(
            GeofencingRequest.Builder()
                .setInitialTrigger(initialTrigger(task))
                .addGeofence(buildGeofence(task))
                .build(),
            geofencePendingIntent
        )
    }

    private fun buildGeofence(task: ReminderTask): Geofence {
        return Geofence.Builder()
            .setRequestId(requestId(task.id))
            .setCircularRegion(task.latitude, task.longitude, task.radiusMeters.toFloat())
            .setTransitionTypes(transitionTypes(task))
            .setLoiteringDelay((task.dwellMinutes.coerceAtLeast(1)) * 60 * 1000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }

    private fun transitionTypes(task: ReminderTask): Int {
        return when (task.triggerMode) {
            "ENTER" -> Geofence.GEOFENCE_TRANSITION_ENTER
            "EXIT" -> Geofence.GEOFENCE_TRANSITION_EXIT
            else -> Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL
        }
    }

    private fun initialTrigger(task: ReminderTask): Int {
        return when (task.triggerMode) {
            "EXIT" -> GeofencingRequest.INITIAL_TRIGGER_EXIT
            else -> GeofencingRequest.INITIAL_TRIGGER_ENTER
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    companion object {
        fun requestId(taskId: Long): String = "reminder-$taskId"
    }
}

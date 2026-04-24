package com.nearnote.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_tasks")
data class ReminderTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Int = 250,
    val triggerMode: String = "ENTER_DWELL",
    val dwellMinutes: Int = 2,
    val recurrenceType: String = "ONCE",
    val recurrenceInterval: Int? = null,
    val isEnabled: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)

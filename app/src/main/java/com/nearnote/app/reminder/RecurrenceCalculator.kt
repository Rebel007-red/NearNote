package com.nearnote.app.reminder

import com.nearnote.app.data.model.ReminderTask
import java.util.Calendar

object RecurrenceCalculator {
    fun calculateNextFire(task: ReminderTask): ReminderTask? {
        if (task.recurrenceType == "ONCE") return null
        
        val calendar = Calendar.getInstance().apply {
            timeInMillis = task.updatedAt
        }
        
        when (task.recurrenceType) {
            "DAILY" -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
            "CUSTOM" -> {
                if (task.recurrenceInterval != null) {
                    calendar.add(Calendar.DAY_OF_MONTH, task.recurrenceInterval)
                } else {
                    return null
                }
            }
            else -> return null
        }
        
        return task.copy(updatedAt = calendar.timeInMillis)
    }
}

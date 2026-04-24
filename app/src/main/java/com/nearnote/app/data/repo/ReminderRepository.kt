package com.nearnote.app.data.repo

import com.nearnote.app.data.db.NearNoteDao
import com.nearnote.app.data.model.ReminderTask
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val dao: NearNoteDao
) {
    fun observeTasks(): Flow<List<ReminderTask>> = dao.observeTasks()

    suspend fun saveTask(task: ReminderTask): Long = dao.insertTask(task)

    suspend fun getTaskById(taskId: Long): ReminderTask? = dao.getTaskById(taskId)

    suspend fun getEnabledTasks(): List<ReminderTask> = dao.getEnabledTasks()

    suspend fun getTasks(): List<ReminderTask> = dao.getTasks()

    suspend fun deleteTask(taskId: Long) {
        dao.deleteTask(taskId)
    }

    suspend fun setTaskEnabled(taskId: Long, enabled: Boolean, updatedAt: Long) {
        dao.setTaskEnabled(taskId, enabled, updatedAt)
    }
}

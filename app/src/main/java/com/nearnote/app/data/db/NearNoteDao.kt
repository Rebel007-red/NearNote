package com.nearnote.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nearnote.app.data.model.ReminderTask
import kotlinx.coroutines.flow.Flow

@Dao
interface NearNoteDao {
    @Query("SELECT * FROM reminder_tasks ORDER BY updatedAt DESC")
    fun observeTasks(): Flow<List<ReminderTask>>

    @Query("SELECT * FROM reminder_tasks ORDER BY updatedAt DESC")
    suspend fun getTasks(): List<ReminderTask>

    @Query("SELECT * FROM reminder_tasks WHERE isEnabled = 1 ORDER BY updatedAt DESC")
    suspend fun getEnabledTasks(): List<ReminderTask>

    @Query("SELECT * FROM reminder_tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Long): ReminderTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ReminderTask): Long

    @Update
    suspend fun updateTask(task: ReminderTask)

    @Query("DELETE FROM reminder_tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    @Query("UPDATE reminder_tasks SET isEnabled = :enabled, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun setTaskEnabled(taskId: Long, enabled: Boolean, updatedAt: Long)

    @Query("UPDATE reminder_tasks SET lastFiredAt = :firedAt WHERE id = :taskId")
    suspend fun setLastFiredAt(taskId: Long, firedAt: Long)
}

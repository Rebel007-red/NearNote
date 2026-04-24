package com.nearnote.app.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.nearnote.app.data.db.AppDatabase
import com.nearnote.app.data.model.ReminderTask
import com.nearnote.app.data.repo.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NearNoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReminderRepository(AppDatabase.getInstance(application).nearNoteDao())
    private val editorState = MutableStateFlow<ReminderEditorState?>(null)
    private val statusMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NearNoteUiState> = combine(
        repository.observeTasks(),
        editorState,
        statusMessage
    ) { tasks, editor, message ->
        NearNoteUiState(
            tasks = tasks,
            editorState = editor,
            statusMessage = message
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NearNoteUiState()
        )

    fun startCreateReminder() {
        editorState.value = ReminderEditorState()
        statusMessage.value = null
    }

    fun startEditReminder(task: ReminderTask) {
        editorState.value = ReminderEditorState(
            id = task.id,
            title = task.title,
            note = task.note,
            placeName = task.placeName,
            latitude = task.latitude.toString(),
            longitude = task.longitude.toString(),
            radiusMeters = task.radiusMeters.toString(),
            triggerMode = task.triggerMode,
            dwellMinutes = task.dwellMinutes.toString(),
            recurrenceType = task.recurrenceType,
            recurrenceInterval = task.recurrenceInterval?.toString().orEmpty(),
            isEnabled = task.isEnabled
        )
        statusMessage.value = null
    }

    fun setEditorState(updatedState: ReminderEditorState) {
        editorState.value = updatedState
    }

    fun dismissEditor() {
        editorState.value = null
        statusMessage.value = null
    }

    fun toggleTask(task: ReminderTask) {
        viewModelScope.launch {
            repository.setTaskEnabled(
                taskId = task.id,
                enabled = !task.isEnabled,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    fun saveReminder() {
        val current = editorState.value ?: return
        val validation = validate(current)
        if (validation != null) {
            statusMessage.value = validation
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.saveTask(
                task = ReminderTask(
                    id = current.id ?: 0,
                    title = current.title.trim(),
                    note = current.note.trim(),
                    placeName = current.placeName.trim(),
                    latitude = current.latitude.trim().toDouble(),
                    longitude = current.longitude.trim().toDouble(),
                    radiusMeters = current.radiusMeters.trim().toInt(),
                    triggerMode = current.triggerMode,
                    dwellMinutes = if (current.triggerMode == TRIGGER_ENTER) 0 else current.dwellMinutes.trim().toInt(),
                    recurrenceType = current.recurrenceType,
                    recurrenceInterval = current.recurrenceInterval.trim().takeIf { current.recurrenceType == RECURRENCE_CUSTOM && it.isNotEmpty() }?.toInt(),
                    isEnabled = current.isEnabled,
                    createdAt = now,
                    updatedAt = now
                )
            )
            statusMessage.value = if (current.id == null) "Reminder added" else "Reminder updated"
            editorState.value = null
        }
    }

    fun deleteReminder() {
        val taskId = editorState.value?.id ?: return
        viewModelScope.launch {
            repository.deleteTask(taskId)
            statusMessage.value = "Reminder deleted"
            editorState.value = null
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    private fun validate(editor: ReminderEditorState): String? {
        if (editor.title.isBlank()) return "Add a reminder title"
        if (editor.placeName.isBlank()) return "Add a place name"
        val latitudeValue = editor.latitude.trim().toDoubleOrNull() ?: return "Latitude must be a valid number"
        val longitudeValue = editor.longitude.trim().toDoubleOrNull() ?: return "Longitude must be a valid number"
        if (latitudeValue !in -90.0..90.0) return "Latitude must be between -90 and 90"
        if (longitudeValue !in -180.0..180.0) return "Longitude must be between -180 and 180"
        val radiusValue = editor.radiusMeters.trim().toIntOrNull() ?: return "Radius must be a whole number"
        if (radiusValue !in 50..5000) return "Radius must be between 50m and 5000m"
        if (editor.triggerMode != TRIGGER_ENTER) {
            val dwellValue = editor.dwellMinutes.trim().toIntOrNull() ?: return "Dwell must be a whole number"
            if (dwellValue !in 1..120) return "Dwell must be between 1 and 120 minutes"
        }
        if (editor.recurrenceType == RECURRENCE_CUSTOM) {
            val interval = editor.recurrenceInterval.trim().toIntOrNull() ?: return "Custom interval must be a whole number"
            if (interval !in 1..365) return "Custom interval must be between 1 and 365 days"
        }
        return null
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return NearNoteViewModel(application) as T
            }
        }

        const val TRIGGER_ENTER = "ENTER"
        const val TRIGGER_ENTER_DWELL = "ENTER_DWELL"
        const val TRIGGER_EXIT = "EXIT"

        const val RECURRENCE_ONCE = "ONCE"
        const val RECURRENCE_DAILY = "DAILY"
        const val RECURRENCE_WEEKLY = "WEEKLY"
        const val RECURRENCE_MONTHLY = "MONTHLY"
        const val RECURRENCE_CUSTOM = "CUSTOM"
    }
}

data class NearNoteUiState(
    val tasks: List<ReminderTask> = emptyList(),
    val editorState: ReminderEditorState? = null,
    val statusMessage: String? = null
)

data class ReminderEditorState(
    val id: Long? = null,
    val title: String = "",
    val note: String = "",
    val placeName: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val radiusMeters: String = "250",
    val triggerMode: String = NearNoteViewModel.TRIGGER_ENTER_DWELL,
    val dwellMinutes: String = "2",
    val recurrenceType: String = NearNoteViewModel.RECURRENCE_ONCE,
    val recurrenceInterval: String = "",
    val isEnabled: Boolean = true
)

@Composable
fun NearNoteApp() {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: NearNoteViewModel = viewModel(factory = NearNoteViewModel.factory(application))
    NearNoteHomeScreen(viewModel = viewModel)
}

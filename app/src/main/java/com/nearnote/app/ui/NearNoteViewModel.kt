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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NearNoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReminderRepository(AppDatabase.getInstance(application).nearNoteDao())

    val uiState: StateFlow<NearNoteUiState> = repository.observeTasks()
        .map { tasks ->
            NearNoteUiState(
                tasks = if (tasks.isEmpty()) sampleTasks() else tasks
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NearNoteUiState(tasks = sampleTasks())
        )

    fun toggleTask(task: ReminderTask) {
        viewModelScope.launch {
            repository.setTaskEnabled(
                taskId = task.id,
                enabled = !task.isEnabled,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return NearNoteViewModel(application) as T
            }
        }

        private fun sampleTasks(): List<ReminderTask> {
            val now = System.currentTimeMillis()
            return listOf(
                ReminderTask(
                    id = 1,
                    title = "Pick up prescription",
                    note = "Ask about the refill window.",
                    placeName = "City Pharmacy",
                    latitude = 28.6139,
                    longitude = 77.2090,
                    radiusMeters = 250,
                    triggerMode = "ENTER_DWELL",
                    dwellMinutes = 2,
                    recurrenceType = "MONTHLY",
                    createdAt = now,
                    updatedAt = now
                ),
                ReminderTask(
                    id = 2,
                    title = "Drop off documents",
                    note = "Reception closes at 6 PM.",
                    placeName = "South Block Office",
                    latitude = 28.6111,
                    longitude = 77.2163,
                    radiusMeters = 150,
                    triggerMode = "ENTER",
                    dwellMinutes = 0,
                    recurrenceType = "ONCE",
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }
}

data class NearNoteUiState(
    val tasks: List<ReminderTask> = emptyList()
)

@Composable
fun NearNoteApp() {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: NearNoteViewModel = viewModel(factory = NearNoteViewModel.factory(application))
    NearNoteHomeScreen(viewModel = viewModel)
}

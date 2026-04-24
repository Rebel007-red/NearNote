package com.nearnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearnote.app.data.model.ReminderTask

@Composable
fun NearNoteHomeScreen(viewModel: NearNoteViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF8F1E7),
                            Color(0xFFE6EEF5),
                            Color(0xFFF7FBF8)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeroCard(taskCount = uiState.tasks.count { it.isEnabled })
                }
                items(uiState.tasks, key = { it.id }) { task ->
                    ReminderTaskCard(
                        task = task,
                        onToggle = { viewModel.toggleTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCard(taskCount: Int) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF17324D),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "NearNote",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFFFF4E8),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Location-first reminders for errands, pickups, drop-offs, and the random things that are easy to forget until you are already nearby.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFD9E5F2)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "$taskCount active reminders",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFFC66D)
            )
        }
    }
}

@Composable
private fun ReminderTaskCard(task: ReminderTask, onToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF14293F)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.placeName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF496580)
                    )
                }
                Switch(checked = task.isEnabled, onCheckedChange = { onToggle() })
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (task.note.isNotBlank()) {
                Text(
                    text = task.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4D5966)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Chip(label = "${task.radiusMeters}m")
                Chip(label = task.triggerMode.replace('_', ' '))
                Chip(label = task.recurrenceType)
            }
        }
    }
}

@Composable
private fun Chip(label: String) {
    Surface(
        color = Color(0xFFE7EEF6),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF234261)
        )
    }
}

package com.nearnote.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nearnote.app.data.model.ReminderTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearNoteHomeScreen(viewModel: NearNoteViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editorState = uiState.editorState
    val context = LocalContext.current
    var permissionVersion by remember { mutableIntStateOf(0) }
    val hasForegroundLocation = remember(permissionVersion) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    val hasBackgroundLocation = remember(permissionVersion) {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    val hasNotifications = remember(permissionVersion) {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
    val foregroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionVersion++ }
    )
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { permissionVersion++ }
    )
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { permissionVersion++ }
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editorState == null) "NearNote" else if (editorState.id == null) "Add reminder" else "Edit reminder"
                    )
                },
                actions = {
                    if (editorState != null) {
                        TextButton(onClick = viewModel::dismissEditor) {
                            Text("Close")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (editorState == null) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::startCreateReminder
                ) {
                    Text("Add reminder")
                }
            }
        }
    ) { innerPadding ->
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
            if (editorState == null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        HeroCard(taskCount = uiState.tasks.count { it.isEnabled })
                    }
                    if (!hasForegroundLocation || !hasBackgroundLocation || !hasNotifications) {
                        item {
                            PermissionStatusCard(
                                hasForegroundLocation = hasForegroundLocation,
                                hasBackgroundLocation = hasBackgroundLocation,
                                hasNotifications = hasNotifications,
                                onRequestForeground = {
                                    foregroundPermissionLauncher.launch(
                                        buildList {
                                            add(Manifest.permission.ACCESS_FINE_LOCATION)
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                add(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        }.toTypedArray()
                                    )
                                },
                                onRequestBackground = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                    }
                                },
                                onRequestNotifications = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                },
                                onOpenSettings = {
                                    context.startActivity(
                                        Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null)
                                        )
                                    )
                                }
                            )
                        }
                    }
                    uiState.statusMessage?.let { message ->
                        item {
                            StatusCard(message = message, onDismiss = viewModel::clearStatusMessage)
                        }
                    }
                    if (uiState.tasks.isEmpty()) {
                        item {
                            EmptyStateCard(onAddReminder = viewModel::startCreateReminder)
                        }
                    }
                    items(uiState.tasks, key = { it.id }) { task ->
                        ReminderTaskCard(
                            task = task,
                            onToggle = { viewModel.toggleTask(task) },
                            onEdit = { viewModel.startEditReminder(task) }
                        )
                    }
                }
            } else {
                ReminderEditorScreen(
                    editorState = editorState,
                    statusMessage = uiState.statusMessage,
                    onEditorChange = viewModel::setEditorState,
                    onSave = viewModel::saveReminder,
                    onDelete = viewModel::deleteReminder,
                    onCancel = viewModel::dismissEditor,
                    onClearStatus = viewModel::clearStatusMessage
                )
            }
        }
    }
}

@Composable
private fun PermissionStatusCard(
    hasForegroundLocation: Boolean,
    hasBackgroundLocation: Boolean,
    hasNotifications: Boolean,
    onRequestForeground: () -> Unit,
    onRequestBackground: () -> Unit,
    onRequestNotifications: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFFF7E9),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Permissions still needed",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF14293F),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Geofenced reminders need location access in the background, and notifications must be allowed to surface alerts.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4D5966)
            )
            PermissionRow(label = "Foreground location", granted = hasForegroundLocation)
            PermissionRow(label = "Background location", granted = hasBackgroundLocation)
            PermissionRow(label = "Notifications", granted = hasNotifications)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!hasForegroundLocation) {
                    OutlinedButton(onClick = onRequestForeground) {
                        Text("Grant location")
                    }
                }
                if (!hasNotifications && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    OutlinedButton(onClick = onRequestNotifications) {
                        Text("Grant notifications")
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!hasBackgroundLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    OutlinedButton(onClick = onRequestBackground) {
                        Text("Grant background")
                    }
                }
                TextButton(onClick = onOpenSettings) {
                    Text("Open app settings")
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(label: String, granted: Boolean) {
    Text(
        text = if (granted) "$label: granted" else "$label: missing",
        style = MaterialTheme.typography.bodyMedium,
        color = if (granted) Color(0xFF17633A) else Color(0xFF9C3E00)
    )
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
private fun EmptyStateCard(onAddReminder: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFFDFBF7),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "No reminders yet",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF14293F),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your first location reminder with a place, radius, trigger mode, and recurrence rule.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF4D5966)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onAddReminder) {
                Text("Create reminder")
            }
        }
    }
}

@Composable
private fun StatusCard(message: String, onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF3D6),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF7A5400)
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun ReminderTaskCard(task: ReminderTask, onToggle: () -> Unit, onEdit: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFBF7)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.clickable(onClick = onEdit)
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
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onEdit) {
                    Text("Edit")
                }
                Text(
                    text = "${task.latitude}, ${task.longitude}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
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

@Composable
private fun ReminderEditorScreen(
    editorState: ReminderEditorState,
    statusMessage: String?,
    onEditorChange: (ReminderEditorState) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onClearStatus: () -> Unit
) {
    var showPlacePicker by remember { mutableStateOf(false) }

    if (showPlacePicker) {
        PlacePickerSheet(
            initialPlace = editorState.placeName,
            onPlaceSelected = { place ->
                onEditorChange(
                    editorState.copy(
                        placeName = place.name,
                        latitude = place.latitude.toString(),
                        longitude = place.longitude.toString()
                    )
                )
            },
            onDismiss = { showPlacePicker = false }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF17324D),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (editorState.id == null) "Plan a new location reminder" else "Update this reminder",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFFFF4E8),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Search for a place or enter coordinates manually. Everything stays on your device.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFD9E5F2)
                )
            }
        }

        statusMessage?.let { message ->
            StatusCard(message = message, onDismiss = onClearStatus)
        }

        EditorTextField(
            label = "Task title",
            value = editorState.title,
            onValueChange = { onEditorChange(editorState.copy(title = it)) },
            placeholder = "Pick up prescription"
        )
        EditorTextField(
            label = "Notes",
            value = editorState.note,
            onValueChange = { onEditorChange(editorState.copy(note = it)) },
            placeholder = "Ask for refill timing",
            minLines = 3
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            EditorTextField(
                label = "Place name",
                value = editorState.placeName,
                onValueChange = { onEditorChange(editorState.copy(placeName = it)) },
                placeholder = "City Pharmacy",
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = { showPlacePicker = true },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Search")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EditorTextField(
                label = "Latitude",
                value = editorState.latitude,
                onValueChange = { onEditorChange(editorState.copy(latitude = it)) },
                placeholder = "28.6139",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f)
            )
            EditorTextField(
                label = "Longitude",
                value = editorState.longitude,
                onValueChange = { onEditorChange(editorState.copy(longitude = it)) },
                placeholder = "77.2090",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EditorTextField(
                label = "Radius (m)",
                value = editorState.radiusMeters,
                onValueChange = { onEditorChange(editorState.copy(radiusMeters = it.filter(Char::isDigit))) },
                placeholder = "250",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
            EditorTextField(
                label = "Dwell (min)",
                value = editorState.dwellMinutes,
                onValueChange = { onEditorChange(editorState.copy(dwellMinutes = it.filter(Char::isDigit))) },
                placeholder = "2",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
                enabled = editorState.triggerMode != NearNoteViewModel.TRIGGER_ENTER
            )
        }

        OptionGroup(
            title = "Trigger",
            options = listOf(
                NearNoteViewModel.TRIGGER_ENTER to "Enter",
                NearNoteViewModel.TRIGGER_ENTER_DWELL to "Enter + dwell",
                NearNoteViewModel.TRIGGER_EXIT to "Exit"
            ),
            selected = editorState.triggerMode,
            onSelected = {
                onEditorChange(
                    editorState.copy(
                        triggerMode = it,
                        dwellMinutes = if (it == NearNoteViewModel.TRIGGER_ENTER) "0" else editorState.dwellMinutes.ifBlank { "2" }
                    )
                )
            }
        )

        OptionGroup(
            title = "Recurrence",
            options = listOf(
                NearNoteViewModel.RECURRENCE_ONCE to "Once",
                NearNoteViewModel.RECURRENCE_DAILY to "Daily",
                NearNoteViewModel.RECURRENCE_WEEKLY to "Weekly",
                NearNoteViewModel.RECURRENCE_MONTHLY to "Monthly",
                NearNoteViewModel.RECURRENCE_CUSTOM to "Custom"
            ),
            selected = editorState.recurrenceType,
            onSelected = {
                onEditorChange(
                    editorState.copy(
                        recurrenceType = it,
                        recurrenceInterval = if (it == NearNoteViewModel.RECURRENCE_CUSTOM) editorState.recurrenceInterval else ""
                    )
                )
            }
        )

        if (editorState.recurrenceType == NearNoteViewModel.RECURRENCE_CUSTOM) {
            EditorTextField(
                label = "Custom interval (days)",
                value = editorState.recurrenceInterval,
                onValueChange = { onEditorChange(editorState.copy(recurrenceInterval = it.filter(Char::isDigit))) },
                placeholder = "14",
                keyboardType = KeyboardType.Number
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            OutlinedButton(onClick = onSave, modifier = Modifier.weight(1f)) {
                Text(if (editorState.id == null) "Save reminder" else "Update reminder")
            }
        }

        if (editorState.id != null) {
            TextButton(onClick = onDelete) {
                Text("Delete reminder")
            }
        }
    }
}

@Composable
private fun EditorTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = minLines == 1
    )
}

@Composable
private fun OptionGroup(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF14293F),
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEach { (value, label) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelected(value) },
                    label = { Text(label) }
                )
            }
        }
    }
}

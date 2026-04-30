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
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.nearnote.app.data.model.ReminderTask

// Glassmorphism Color Palette
private val charcoalBg = Color(0xFF121212)
private val charcoalSecondary = Color(0xFF1D1A18)
private val glassPanel = Color(0xCCCCCCCC)  // 80% opaque frosted glass
private val gradientStart = Color(0xFFC48A3A)
private val gradientEnd = Color(0xFFB8564A)
private val accentPrimary = Color(0xFFF0B35E)
private val accentGold = Color(0xFFFFC107)
private val accentRed = Color(0xFFEF5350)
private val textLight = Color(0xFFE0E0E0)
private val textMuted = Color(0xFF9E9E9E)
private val glassStroke = Color(0x4DFFFFFF)  // Glass border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearNoteHomeScreen(viewModel: NearNoteViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCompletedSection by rememberSaveable { mutableStateOf(false) }
    val activeTasks = uiState.tasks.filter { !it.isCompleted }
    val completedTasks = uiState.tasks.filter { it.isCompleted }
    val completedCount = completedTasks.size
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(gradientStart, gradientEnd)))
            ) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = textLight,
                        actionIconContentColor = textLight
                    ),
                    title = {
                        Text(
                            text = if (editorState == null) "NearNote" else if (editorState.id == null) "Add reminder" else "Edit reminder"
                        )
                    },
                    actions = {
                        if (editorState != null) {
                            TextButton(onClick = viewModel::dismissEditor) {
                                Text("Close", color = textLight)
                            }
                        }
                    }
                )
            }
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
                        colors = listOf(charcoalBg, charcoalSecondary, charcoalBg)
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
                        HeroCard(taskCount = activeTasks.count { it.isEnabled })
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
                    if (activeTasks.isEmpty()) {
                        item {
                            EmptyStateCard(onAddReminder = viewModel::startCreateReminder)
                        }
                    }
                    items(activeTasks, key = { it.id }) { task ->
                        ReminderTaskCard(
                            task = task,
                            onDelete = { viewModel.deleteTask(task.id) },
                            onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                            onEdit = { viewModel.startEditReminder(task) }
                        )
                    }
                    if (completedCount > 0) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = showCompletedSection,
                                    onClick = { showCompletedSection = !showCompletedSection },
                                    label = {
                                        Text(
                                            if (showCompletedSection) {
                                                "Hide completed ($completedCount)"
                                            } else {
                                                "Show completed ($completedCount)"
                                            }
                                        )
                                    }
                                )
                                if (showCompletedSection) {
                                    TextButton(onClick = viewModel::clearAllCompleted) {
                                        Text(
                                            text = "Clear all",
                                            color = Color(0xFFDC2626),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (showCompletedSection) {
                        items(completedTasks, key = { "completed-${it.id}" }) { task ->
                            ReminderTaskCard(
                                task = task,
                                onDelete = { viewModel.deleteTask(task.id) },
                                onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                                onEdit = { viewModel.startEditReminder(task) }
                            )
                        }
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
        shape = RoundedCornerShape(20.dp),
        color = glassPanel,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = glassStroke,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Permissions needed",
                style = MaterialTheme.typography.titleLarge,
                color = textLight,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Geofence & location background access",
                style = MaterialTheme.typography.bodyMedium,
                color = textMuted
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
        color = if (granted) accentGold else accentRed
    )
}

@Composable
private fun HeroCard(taskCount: Int) {
    val heroGradient = Brush.horizontalGradient(
        colors = listOf(gradientStart, gradientEnd)
    )
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = glassPanel,
        modifier = Modifier
            .fillMaxWidth()
            .background(heroGradient)
            .border(
                width = 1.dp,
                color = glassStroke,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "NearNote",
                style = MaterialTheme.typography.headlineMedium,
                color = textLight,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Location reminders",
                style = MaterialTheme.typography.bodyLarge,
                color = textMuted
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$taskCount active",
                style = MaterialTheme.typography.titleMedium,
                color = accentGold
            )
        }
    }
}

@Composable
private fun EmptyStateCard(onAddReminder: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = glassPanel,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = glassStroke,
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "No reminders yet",
                style = MaterialTheme.typography.titleLarge,
                color = textLight,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create location reminders",
                style = MaterialTheme.typography.bodyLarge,
                color = textMuted
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onAddReminder) {
                Text("Add reminder")
            }
        }
    }
}

@Composable
private fun StatusCard(message: String, onDismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = glassPanel,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = glassStroke,
                shape = RoundedCornerShape(20.dp)
            )
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
                color = accentGold
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun ReminderTaskCard(
    task: ReminderTask,
    onDelete: () -> Unit,
    onToggleCompleted: () -> Unit,
    onEdit: () -> Unit
) {
    var isExpanded by rememberSaveable(task.id) { mutableStateOf(false) }

    val titleColor = if (task.isCompleted) textMuted else textLight
    val subtitleColor = if (task.isCompleted) Color(0xFF616161) else Color(0xFFBBBBBB)
    
    // Gradient for card background (left to right)
    val gradientBrush = Brush.horizontalGradient(
        colors = if (task.isCompleted) 
            listOf(Color(0x66666666), Color(0x4D666666))
        else
            listOf(gradientStart, gradientEnd)
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) 
                Color(0x99333333) 
            else 
                glassPanel
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = glassStroke,
                shape = RoundedCornerShape(20.dp)
            )
            .combinedClickable(
                onClick = { /* no-op */ },
                onLongClick = { isExpanded = !isExpanded }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isExpanded && !task.isCompleted) gradientBrush else Brush.solidColor(Color.Transparent)
                )
        ) {
            if (!isExpanded) {
                // Collapsed state: Title + Icon buttons only
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = titleColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Long press to expand",
                            style = MaterialTheme.typography.labelSmall,
                            color = textMuted
                        )
                    }
                    Row(
                        modifier = Modifier.padding(start = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = onEdit, modifier = Modifier.width(40.dp)) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit",
                                tint = accentPrimary,
                                modifier = Modifier.width(20.dp)
                            )
                        }
                        IconButton(onClick = onToggleCompleted, modifier = Modifier.width(40.dp)) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = if (task.isCompleted) "Reopen" else "Complete",
                                tint = if (task.isCompleted) textMuted else accentGold,
                                modifier = Modifier.width(20.dp)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.width(40.dp)) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = accentRed,
                                modifier = Modifier.width(20.dp)
                            )
                        }
                    }
                }
            } else {
                // Expanded state: Full details
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
                                color = titleColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.placeName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = subtitleColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Details grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Chip(label = "${task.radiusMeters}m")
                        Chip(label = task.priority)
                        Chip(label = task.triggerMode.replace('_', ' '))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Chip(label = task.recurrenceType)
                        if (task.isCompleted) {
                            Chip(label = "Completed")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "📍 ${task.latitude}, ${task.longitude}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                            Text("Edit")
                        }
                        OutlinedButton(onClick = onToggleCompleted, modifier = Modifier.weight(1f)) {
                            Text(if (task.isCompleted) "Reopen" else "Complete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(label: String) {
    Surface(
        color = Color(0x4D87CEEB),
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = glassStroke,
            shape = RoundedCornerShape(999.dp)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textLight
        )
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
    var showMapPicker by remember { mutableStateOf(false) }

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

    if (showMapPicker) {
        MapPickerScreen(
            initialLatitude = editorState.latitude.toDoubleOrNull() ?: 0.0,
            initialLongitude = editorState.longitude.toDoubleOrNull() ?: 0.0,
            radiusMeters = editorState.radiusMeters.toIntOrNull() ?: 250,
            placeName = editorState.placeName,
            onPlaceSelected = { lat, lon, place ->
                onEditorChange(
                    editorState.copy(
                        placeName = place,
                        latitude = lat.toString(),
                        longitude = lon.toString()
                    )
                )
            },
            onDismiss = { showMapPicker = false }
        )
        return
    }

    val titleError = if (editorState.title.isBlank()) "Title is required" else null
    val placeError = if (editorState.placeName.isBlank()) "Place is required" else null
    val latitudeValue = editorState.latitude.trim().toDoubleOrNull()
    val latitudeError = when {
        editorState.latitude.isBlank() -> "Latitude is required"
        latitudeValue == null -> "Enter a valid latitude"
        latitudeValue !in -90.0..90.0 -> "Must be between -90 and 90"
        else -> null
    }
    val longitudeValue = editorState.longitude.trim().toDoubleOrNull()
    val longitudeError = when {
        editorState.longitude.isBlank() -> "Longitude is required"
        longitudeValue == null -> "Enter a valid longitude"
        longitudeValue !in -180.0..180.0 -> "Must be between -180 and 180"
        else -> null
    }
    val radiusValue = editorState.radiusMeters.trim().toIntOrNull()
    val radiusError = when {
        editorState.radiusMeters.isBlank() -> "Radius is required"
        radiusValue == null -> "Enter a whole number"
        radiusValue !in 50..5000 -> "Must be 50m to 5000m"
        else -> null
    }
    val dwellValue = editorState.dwellMinutes.trim().toIntOrNull()
    val dwellError = if (editorState.triggerMode != NearNoteViewModel.TRIGGER_ENTER) {
        when {
            editorState.dwellMinutes.isBlank() -> "Dwell is required"
            dwellValue == null -> "Enter a whole number"
            dwellValue !in 1..120 -> "Must be 1 to 120"
            else -> null
        }
    } else {
        null
    }
    val intervalValue = editorState.recurrenceInterval.trim().toIntOrNull()
    val intervalError = if (editorState.recurrenceType == NearNoteViewModel.RECURRENCE_CUSTOM) {
        when {
            editorState.recurrenceInterval.isBlank() -> "Interval is required"
            intervalValue == null -> "Enter a whole number"
            intervalValue !in 1..365 -> "Must be 1 to 365"
            else -> null
        }
    } else {
        null
    }
    val canSave = listOf(titleError, placeError, latitudeError, longitudeError, radiusError, dwellError, intervalError).all { it == null }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val editorGradient = Brush.horizontalGradient(
            colors = listOf(gradientStart, gradientEnd)
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = glassPanel,
            modifier = Modifier
                .fillMaxWidth()
                .background(editorGradient)
                .border(
                    width = 1.dp,
                    color = glassStroke,
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (editorState.id == null) "Add reminder" else "Edit reminder",
                    style = MaterialTheme.typography.headlineSmall,
                    color = textLight,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Location & timing",
                    style = MaterialTheme.typography.bodyLarge,
                    color = textMuted
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
            placeholder = "Pick up prescription",
            error = titleError
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
                modifier = Modifier.weight(1f),
                error = placeError
            )
            OutlinedButton(
                onClick = { showPlacePicker = true },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Search")
            }
            OutlinedButton(
                onClick = { showMapPicker = true },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Map")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EditorTextField(
                label = "Latitude",
                value = editorState.latitude,
                onValueChange = { onEditorChange(editorState.copy(latitude = it)) },
                placeholder = "28.6139",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
                error = latitudeError
            )
            EditorTextField(
                label = "Longitude",
                value = editorState.longitude,
                onValueChange = { onEditorChange(editorState.copy(longitude = it)) },
                placeholder = "77.2090",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
                error = longitudeError
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            EditorTextField(
                label = "Radius (m)",
                value = editorState.radiusMeters,
                onValueChange = { onEditorChange(editorState.copy(radiusMeters = it.filter(Char::isDigit))) },
                placeholder = "250",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
                error = radiusError
            )
            EditorTextField(
                label = "Dwell (min)",
                value = editorState.dwellMinutes,
                onValueChange = { onEditorChange(editorState.copy(dwellMinutes = it.filter(Char::isDigit))) },
                placeholder = "2",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
                enabled = editorState.triggerMode != NearNoteViewModel.TRIGGER_ENTER,
                error = dwellError
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("100", "250", "500", "1000").forEach { radiusPreset ->
                FilterChip(
                    selected = editorState.radiusMeters == radiusPreset,
                    onClick = { onEditorChange(editorState.copy(radiusMeters = radiusPreset)) },
                    label = { Text("${radiusPreset}m") }
                )
            }
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
            title = "Priority",
            options = listOf(
                NearNoteViewModel.PRIORITY_LOW to "Low",
                NearNoteViewModel.PRIORITY_MEDIUM to "Medium",
                NearNoteViewModel.PRIORITY_HIGH to "High"
            ),
            selected = editorState.priority,
            onSelected = { onEditorChange(editorState.copy(priority = it)) }
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
                keyboardType = KeyboardType.Number,
                error = intervalError
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
            OutlinedButton(onClick = onSave, modifier = Modifier.weight(1f), enabled = canSave) {
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
    minLines: Int = 1,
    error: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        minLines = minLines,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
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

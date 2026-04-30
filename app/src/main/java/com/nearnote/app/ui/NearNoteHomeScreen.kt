package com.nearnote.app.ui

import android.Manifest
import android.content.Context
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
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.lerp
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
private val charcoalSecondary = Color(0xFF1A202A)
private val glassPanel = Color(0xCC1E232C)
private val gradientStart = Color(0xFFC48A3A)
private val gradientEnd = Color(0xFFB8564A)
private val accentPrimary = Color(0xFFF0B35E)
private val accentGold = Color(0xFFFFC107)
private val accentRed = Color(0xFFEF5350)
private val textLight = Color(0xFFF8FAFC)
private val textMuted = Color(0xFFCBD5E1)
private val glassStroke = Color(0x66FFFFFF)
private const val GLASS_STYLE_PREFS = "glass_style_prefs"
private const val GLASS_INTENSITY_KEY = "glass_intensity"
private const val GLASS_BORDER_KEY = "glass_border"
private const val GLASS_SHADOW_KEY = "glass_shadow"
private const val GLASS_TINT_KEY = "glass_tint"
private const val GLASS_BUTTON_SIZE_KEY = "glass_button_size"
private const val GLASS_INPUT_SIZE_KEY = "glass_input_size"
private const val GLASS_INPUT_INTENSITY_KEY = "glass_input_intensity"

private data class GlassStyle(
    val intensity: String = "medium",
    val border: String = "subtle",
    val shadow: String = "md",
    val tint: String = "none",
    val buttonSize: String = "md",
    val inputSize: String = "md",
    val inputIntensity: String = "medium"
)

private data class GlassPreset(
    val name: String,
    val style: GlassStyle
)

private val DefaultGlassStyle = GlassStyle()
private val GlassPresets = listOf(
    GlassPreset("Smoked", GlassStyle(intensity = "strong", border = "none", shadow = "md", tint = "none")),
    GlassPreset("Crystal", GlassStyle(intensity = "subtle", border = "strong", shadow = "sm", tint = "none")),
    GlassPreset("Aurora", GlassStyle(intensity = "medium", border = "subtle", shadow = "lg", tint = "teal"))
)

private fun tintedPanelColor(style: GlassStyle): Color {
    val base = when (style.intensity) {
        "subtle" -> Color(0x991E232C)
        "strong" -> Color(0xE61E232C)
        else -> Color(0xCC1E232C)
    }
    val tint = when (style.tint) {
        "blue" -> Color(0xFF3B82F6)
        "pink" -> Color(0xFFEC4899)
        "orange" -> Color(0xFFF97316)
        "teal" -> Color(0xFF14B8A6)
        else -> Color.Transparent
    }
    return if (style.tint == "none") base else lerp(base, tint.copy(alpha = 0.35f), 0.18f)
}

private fun strokeFor(style: GlassStyle): Color {
    return when (style.border) {
        "none" -> Color.Transparent
        "strong" -> Color(0x88FFFFFF)
        else -> glassStroke
    }
}

private fun elevationFor(style: GlassStyle): androidx.compose.ui.unit.Dp {
    return when (style.shadow) {
        "none" -> 0.dp
        "sm" -> 2.dp
        "lg" -> 10.dp
        else -> 6.dp
    }
}

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
    val glassPrefs = remember { context.getSharedPreferences(GLASS_STYLE_PREFS, Context.MODE_PRIVATE) }
    var glassIntensity by rememberSaveable {
        mutableStateOf(glassPrefs.getString(GLASS_INTENSITY_KEY, DefaultGlassStyle.intensity) ?: DefaultGlassStyle.intensity)
    }
    var glassBorder by rememberSaveable {
        mutableStateOf(glassPrefs.getString(GLASS_BORDER_KEY, DefaultGlassStyle.border) ?: DefaultGlassStyle.border)
    }
    var glassShadow by rememberSaveable {
        mutableStateOf(glassPrefs.getString(GLASS_SHADOW_KEY, DefaultGlassStyle.shadow) ?: DefaultGlassStyle.shadow)
    }
    var glassTint by rememberSaveable {
        mutableStateOf(glassPrefs.getString(GLASS_TINT_KEY, DefaultGlassStyle.tint) ?: DefaultGlassStyle.tint)
    }
    var glassButtonSize by rememberSaveable {
        mutableStateOf(glassPrefs.getString(GLASS_BUTTON_SIZE_KEY, DefaultGlassStyle.buttonSize) ?: DefaultGlassStyle.buttonSize)
    }
    var glassInputSize by rememberSaveable {
        mutableStateOf(glassPrefs.getString(GLASS_INPUT_SIZE_KEY, DefaultGlassStyle.inputSize) ?: DefaultGlassStyle.inputSize)
    }
    var glassInputIntensity by rememberSaveable {
        mutableStateOf(
            glassPrefs.getString(GLASS_INPUT_INTENSITY_KEY, DefaultGlassStyle.inputIntensity)
                ?: DefaultGlassStyle.inputIntensity
        )
    }
    val glassStyle = GlassStyle(
        intensity = glassIntensity,
        border = glassBorder,
        shadow = glassShadow,
        tint = glassTint,
        buttonSize = glassButtonSize,
        inputSize = glassInputSize,
        inputIntensity = glassInputIntensity
    )
    val contentGlassStyle = glassStyle.copy(tint = "none")
    var showClearCompletedConfirm by rememberSaveable { mutableStateOf(false) }
    var pendingDeleteTaskId by rememberSaveable { mutableStateOf<Long?>(null) }
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
    val hasMissingPermissions = !hasForegroundLocation || !hasBackgroundLocation || !hasNotifications
    var showPermissionPopup by rememberSaveable { mutableStateOf(hasMissingPermissions) }
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
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .padding(bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        HeroCard(taskCount = activeTasks.count { it.isEnabled }, style = glassStyle)
                    }
                    item {
                        GlassStyleSettingsCard(
                            style = glassStyle,
                            onApplyPreset = { preset ->
                                glassIntensity = preset.style.intensity
                                glassBorder = preset.style.border
                                glassShadow = preset.style.shadow
                                glassTint = preset.style.tint
                                glassButtonSize = preset.style.buttonSize
                                glassInputSize = preset.style.inputSize
                                glassInputIntensity = preset.style.inputIntensity
                                glassPrefs.edit()
                                    .putString(GLASS_INTENSITY_KEY, preset.style.intensity)
                                    .putString(GLASS_BORDER_KEY, preset.style.border)
                                    .putString(GLASS_SHADOW_KEY, preset.style.shadow)
                                    .putString(GLASS_TINT_KEY, preset.style.tint)
                                    .putString(GLASS_BUTTON_SIZE_KEY, preset.style.buttonSize)
                                    .putString(GLASS_INPUT_SIZE_KEY, preset.style.inputSize)
                                    .putString(GLASS_INPUT_INTENSITY_KEY, preset.style.inputIntensity)
                                    .apply()
                            },
                            onIntensityChange = {
                                glassIntensity = it
                                glassPrefs.edit().putString(GLASS_INTENSITY_KEY, it).apply()
                            },
                            onBorderChange = {
                                glassBorder = it
                                glassPrefs.edit().putString(GLASS_BORDER_KEY, it).apply()
                            },
                            onShadowChange = {
                                glassShadow = it
                                glassPrefs.edit().putString(GLASS_SHADOW_KEY, it).apply()
                            },
                            onTintChange = {
                                glassTint = it
                                glassPrefs.edit().putString(GLASS_TINT_KEY, it).apply()
                            },
                            onButtonSizeChange = {
                                glassButtonSize = it
                                glassPrefs.edit().putString(GLASS_BUTTON_SIZE_KEY, it).apply()
                            },
                            onInputSizeChange = {
                                glassInputSize = it
                                glassPrefs.edit().putString(GLASS_INPUT_SIZE_KEY, it).apply()
                            },
                            onInputIntensityChange = {
                                glassInputIntensity = it
                                glassPrefs.edit().putString(GLASS_INPUT_INTENSITY_KEY, it).apply()
                            }
                        )
                    }
                    if (hasMissingPermissions) {
                        item {
                            GlassButton(
                                text = "Permissions required",
                                onClick = { showPermissionPopup = true },
                                style = glassStyle,
                                size = glassStyle.buttonSize,
                                variant = "outline"
                            )
                        }
                    }
                    if (activeTasks.isEmpty()) {
                        item {
                            EmptyStateCard(onAddReminder = viewModel::startCreateReminder, style = contentGlassStyle)
                        }
                    }
                    items(activeTasks, key = { it.id }) { task ->
                        ReminderTaskCard(
                            style = contentGlassStyle,
                            task = task,
                            onDelete = { pendingDeleteTaskId = task.id },
                            onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                            onEdit = { viewModel.startEditReminder(task) }
                        )
                    }
                    if (showCompletedSection) {
                        items(completedTasks, key = { "completed-${it.id}" }) { task ->
                            ReminderTaskCard(
                                style = contentGlassStyle,
                                task = task,
                                onDelete = { pendingDeleteTaskId = task.id },
                                onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                                onEdit = { viewModel.startEditReminder(task) }
                            )
                        }
                    }
                }
            } else {
                ReminderEditorScreen(
                    style = contentGlassStyle,
                    editorState = editorState,
                    statusMessage = uiState.statusMessage,
                    onEditorChange = viewModel::setEditorState,
                    onSave = viewModel::saveReminder,
                    onDelete = viewModel::deleteReminder,
                    onCancel = viewModel::dismissEditor,
                    onClearStatus = viewModel::clearStatusMessage
                )
            }

            if (showClearCompletedConfirm) {
                GlassConfirmModal(
                    style = glassStyle,
                    title = "Clear completed reminders?",
                    description = "This removes all completed reminders from history.",
                    confirmLabel = "Clear all",
                    dismissLabel = "Cancel",
                    onConfirm = {
                        viewModel.clearAllCompleted()
                        showClearCompletedConfirm = false
                    },
                    onDismiss = { showClearCompletedConfirm = false }
                )
            }

            if (hasMissingPermissions && showPermissionPopup) {
                GlassPermissionPopup(
                    style = glassStyle,
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
                    },
                    onDismiss = { showPermissionPopup = false }
                )
            }

            if (editorState == null && completedCount > 0) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassTabBar(
                        style = glassStyle,
                        selectedIndex = if (showCompletedSection) 1 else 0,
                        items = listOf(
                            "Active (${activeTasks.size})",
                            "Completed ($completedCount)"
                        ),
                        onSelected = { selectedIndex -> showCompletedSection = selectedIndex == 1 },
                        modifier = Modifier.weight(1f)
                    )
                    if (showCompletedSection) {
                        GlassButton(
                            text = "Clear all",
                            onClick = { showClearCompletedConfirm = true },
                            style = glassStyle,
                            size = glassStyle.buttonSize,
                            variant = "outline",
                            textColor = accentRed
                        )
                    }
                }
            }

            uiState.statusMessage?.let { message ->
                GlassStatusPopup(
                    style = glassStyle,
                    message = message,
                    onDismiss = viewModel::clearStatusMessage,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 86.dp, start = 20.dp, end = 20.dp)
                )
            }

            pendingDeleteTaskId?.let { taskId ->
                GlassConfirmModal(
                    style = glassStyle,
                    title = "Delete reminder?",
                    description = "This reminder will be removed immediately.",
                    confirmLabel = "Delete",
                    dismissLabel = "Cancel",
                    onConfirm = {
                        viewModel.deleteTask(taskId)
                        pendingDeleteTaskId = null
                    },
                    onDismiss = { pendingDeleteTaskId = null }
                )
            }
        }
    }
}

@Composable
private fun GlassStyleSettingsCard(
    style: GlassStyle,
    onApplyPreset: (GlassPreset) -> Unit,
    onIntensityChange: (String) -> Unit,
    onBorderChange: (String) -> Unit,
    onShadowChange: (String) -> Unit,
    onTintChange: (String) -> Unit,
    onButtonSizeChange: (String) -> Unit,
    onInputSizeChange: (String) -> Unit,
    onInputIntensityChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = tintedPanelColor(style),
        shadowElevation = elevationFor(style),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = strokeFor(style),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Glass style",
                style = MaterialTheme.typography.titleMedium,
                color = textLight,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Adjust intensity, border, shadow, and tint",
                style = MaterialTheme.typography.bodySmall,
                color = textMuted
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                GlassPresets.forEach { preset ->
                    FilterChip(
                        selected = style == preset.style,
                        onClick = { onApplyPreset(preset) },
                        label = { Text(preset.name) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("subtle", "medium", "strong").forEach { option ->
                    FilterChip(
                        selected = style.intensity == option,
                        onClick = { onIntensityChange(option) },
                        label = { Text(option.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("none", "subtle", "strong").forEach { option ->
                    FilterChip(
                        selected = style.border == option,
                        onClick = { onBorderChange(option) },
                        label = { Text("Border: ${option.replaceFirstChar { it.uppercase() }}") }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("none", "sm", "md", "lg").forEach { option ->
                    FilterChip(
                        selected = style.shadow == option,
                        onClick = { onShadowChange(option) },
                        label = { Text("Shadow: ${option.uppercase()}") }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("none", "blue", "pink", "orange", "teal").forEach { option ->
                    FilterChip(
                        selected = style.tint == option,
                        onClick = { onTintChange(option) },
                        label = { Text("Tint: ${option.replaceFirstChar { it.uppercase() }}") }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("sm", "md", "lg").forEach { option ->
                    FilterChip(
                        selected = style.buttonSize == option,
                        onClick = { onButtonSizeChange(option) },
                        label = { Text("Button: ${option.uppercase()}") }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("sm", "md", "lg").forEach { option ->
                    FilterChip(
                        selected = style.inputSize == option,
                        onClick = { onInputSizeChange(option) },
                        label = { Text("Input size: ${option.uppercase()}") }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("subtle", "medium", "strong").forEach { option ->
                    FilterChip(
                        selected = style.inputIntensity == option,
                        onClick = { onInputIntensityChange(option) },
                        label = { Text("Input: ${option.replaceFirstChar { it.uppercase() }}") }
                    )
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
private fun HeroCard(taskCount: Int, style: GlassStyle = DefaultGlassStyle) {
    val heroGradient = Brush.horizontalGradient(
        colors = listOf(gradientStart, gradientEnd)
    )
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = tintedPanelColor(style),
        shadowElevation = elevationFor(style),
        modifier = Modifier
            .fillMaxWidth()
            .background(heroGradient)
            .border(
                width = 1.dp,
                color = strokeFor(style),
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
private fun EmptyStateCard(onAddReminder: () -> Unit, style: GlassStyle = DefaultGlassStyle) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = tintedPanelColor(style),
        shadowElevation = elevationFor(style),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = strokeFor(style),
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
private fun StatusCard(
    message: String,
    onDismiss: () -> Unit,
    style: GlassStyle = DefaultGlassStyle
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = tintedPanelColor(style),
        shadowElevation = elevationFor(style),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = strokeFor(style),
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
@OptIn(ExperimentalFoundationApi::class)
private fun ReminderTaskCard(
    style: GlassStyle = DefaultGlassStyle,
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
                tintedPanelColor(style)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevationFor(style)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = strokeFor(style),
                shape = RoundedCornerShape(20.dp)
            )
            .combinedClickable(
                onClick = { /* no-op */ },
                onLongClick = { isExpanded = !isExpanded }
            )
    ) {
        val expandedBackground = if (isExpanded && !task.isCompleted) {
            Modifier.background(gradientBrush)
        } else {
            Modifier
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(expandedBackground)
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
                        Chip(label = "${task.radiusMeters}m", style = style)
                        Chip(label = task.priority, style = style)
                        Chip(label = task.triggerMode.replace('_', ' '), style = style)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Chip(label = task.recurrenceType, style = style)
                        if (task.isCompleted) {
                            Chip(label = "Completed", style = style)
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
private fun Chip(label: String, style: GlassStyle = DefaultGlassStyle) {
    Surface(
        color = tintedPanelColor(style).copy(alpha = 0.55f),
        shape = RoundedCornerShape(999.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = strokeFor(style),
            shape = RoundedCornerShape(999.dp)
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = textLight
        )
    }
}

@Composable
private fun GlassButton(
    text: String,
    onClick: () -> Unit,
    style: GlassStyle,
    size: String,
    variant: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textColor: Color? = null
) {
    val minHeight = when (size) {
        "sm" -> 36.dp
        "lg" -> 52.dp
        else -> 44.dp
    }
    val contentColor = textColor ?: if (variant == "solid") Color(0xFF101828) else textLight
    val bgColor = if (variant == "solid") accentPrimary else Color.Transparent

    when (variant) {
        "solid" -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier.heightIn(min = minHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = bgColor,
                    contentColor = contentColor,
                    disabledContainerColor = bgColor.copy(alpha = 0.45f),
                    disabledContentColor = contentColor.copy(alpha = 0.6f)
                )
            ) {
                Text(text)
            }
        }

        "outline" -> {
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier.heightIn(min = minHeight),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (style.border == "strong") 2.dp else 1.dp,
                    color = strokeFor(style)
                )
            ) {
                Text(text = text, color = contentColor)
            }
        }

        else -> {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier.heightIn(min = minHeight)
            ) {
                Text(text = text, color = contentColor)
            }
        }
    }
}

@Composable
private fun GlassTabBar(
    style: GlassStyle,
    selectedIndex: Int,
    items: List<String>,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = tintedPanelColor(style),
        shadowElevation = elevationFor(style),
        modifier = modifier.border(
            width = 1.dp,
            color = strokeFor(style),
            shape = RoundedCornerShape(999.dp)
        )
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val segmentBg = if (selected) accentPrimary else Color.Transparent
                Surface(
                    color = segmentBg,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    TextButton(onClick = { onSelected(index) }) {
                        Text(
                            text = label,
                            color = if (selected) Color(0xFF101828) else textLight,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassConfirmModal(
    style: GlassStyle,
    title: String,
    description: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = tintedPanelColor(style),
            shadowElevation = elevationFor(style),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .border(
                    width = 1.dp,
                    color = strokeFor(style),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = textLight
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textMuted
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(dismissLabel)
                    }
                    OutlinedButton(onClick = onConfirm, modifier = Modifier.weight(1f)) {
                        Text(confirmLabel, color = accentRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassStatusPopup(
    style: GlassStyle,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = tintedPanelColor(style),
        shadowElevation = elevationFor(style),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = strokeFor(style),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = textLight,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            GlassButton(
                text = "OK",
                onClick = onDismiss,
                style = style,
                size = "sm",
                variant = "outline"
            )
        }
    }
}

@Composable
private fun GlassPermissionPopup(
    style: GlassStyle,
    hasForegroundLocation: Boolean,
    hasBackgroundLocation: Boolean,
    hasNotifications: Boolean,
    onRequestForeground: () -> Unit,
    onRequestBackground: () -> Unit,
    onRequestNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = tintedPanelColor(style),
            shadowElevation = elevationFor(style),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .border(
                    width = 1.dp,
                    color = strokeFor(style),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Permissions needed",
                    style = MaterialTheme.typography.titleLarge,
                    color = textLight,
                    fontWeight = FontWeight.SemiBold
                )
                PermissionRow(label = "Foreground location", granted = hasForegroundLocation)
                PermissionRow(label = "Background location", granted = hasBackgroundLocation)
                PermissionRow(label = "Notifications", granted = hasNotifications)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    if (!hasForegroundLocation) {
                        GlassButton(
                            text = "Grant location",
                            onClick = onRequestForeground,
                            style = style,
                            size = style.buttonSize,
                            variant = "outline",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (!hasNotifications && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        GlassButton(
                            text = "Grant notifications",
                            onClick = onRequestNotifications,
                            style = style,
                            size = style.buttonSize,
                            variant = "outline",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    if (!hasBackgroundLocation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        GlassButton(
                            text = "Grant background",
                            onClick = onRequestBackground,
                            style = style,
                            size = style.buttonSize,
                            variant = "outline",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    GlassButton(
                        text = "Open settings",
                        onClick = onOpenSettings,
                        style = style,
                        size = style.buttonSize,
                        variant = "outline",
                        modifier = Modifier.weight(1f)
                    )
                }

                GlassButton(
                    text = "Close",
                    onClick = onDismiss,
                    style = style,
                    size = style.buttonSize,
                    variant = "ghost"
                )
            }
        }
    }
}

@Composable
private fun ReminderEditorScreen(
    style: GlassStyle = DefaultGlassStyle,
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
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }

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
            onPlaceSelected = { lat, lon, place, radius ->
                onEditorChange(
                    editorState.copy(
                        placeName = place,
                        latitude = lat.toString(),
                        longitude = lon.toString(),
                        radiusMeters = radius.toString()
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
            StatusCard(message = message, onDismiss = onClearStatus, style = style)
        }

        EditorTextField(
            label = "Task title",
            value = editorState.title,
            onValueChange = { onEditorChange(editorState.copy(title = it)) },
            placeholder = "Pick up prescription",
            error = titleError,
            inputSize = style.inputSize,
            inputIntensity = style.inputIntensity
        )
        EditorTextField(
            label = "Notes",
            value = editorState.note,
            onValueChange = { onEditorChange(editorState.copy(note = it)) },
            placeholder = "Ask for refill timing",
            minLines = 3,
            inputSize = style.inputSize,
            inputIntensity = style.inputIntensity
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            EditorTextField(
                label = "Place name",
                value = editorState.placeName,
                onValueChange = { onEditorChange(editorState.copy(placeName = it)) },
                placeholder = "City Pharmacy",
                modifier = Modifier.weight(1f),
                error = placeError,
                inputSize = style.inputSize,
                inputIntensity = style.inputIntensity
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
                error = latitudeError,
                inputSize = style.inputSize,
                inputIntensity = style.inputIntensity
            )
            EditorTextField(
                label = "Longitude",
                value = editorState.longitude,
                onValueChange = { onEditorChange(editorState.copy(longitude = it)) },
                placeholder = "77.2090",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
                error = longitudeError,
                inputSize = style.inputSize,
                inputIntensity = style.inputIntensity
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
                error = radiusError,
                inputSize = style.inputSize,
                inputIntensity = style.inputIntensity
            )
            EditorTextField(
                label = "Dwell (min)",
                value = editorState.dwellMinutes,
                onValueChange = { onEditorChange(editorState.copy(dwellMinutes = it.filter(Char::isDigit))) },
                placeholder = "2",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
                enabled = editorState.triggerMode != NearNoteViewModel.TRIGGER_ENTER,
                error = dwellError,
                inputSize = style.inputSize,
                inputIntensity = style.inputIntensity
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
                error = intervalError,
                inputSize = style.inputSize,
                inputIntensity = style.inputIntensity
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassButton(
                text = "Cancel",
                onClick = onCancel,
                style = style,
                size = style.buttonSize,
                variant = "outline",
                modifier = Modifier.weight(1f)
            )
            GlassButton(
                text = if (editorState.id == null) "Save reminder" else "Update reminder",
                onClick = onSave,
                style = style,
                size = style.buttonSize,
                variant = "solid",
                enabled = canSave,
                modifier = Modifier.weight(1f)
            )
        }

        if (editorState.id != null) {
            GlassButton(
                text = "Delete reminder",
                onClick = { showDeleteConfirm = true },
                style = style,
                size = style.buttonSize,
                variant = "outline",
                textColor = accentRed
            )
        }

        if (showDeleteConfirm) {
            GlassConfirmModal(
                style = style,
                title = "Delete this reminder?",
                description = "This action cannot be undone.",
                confirmLabel = "Delete",
                dismissLabel = "Cancel",
                onConfirm = {
                    onDelete()
                    showDeleteConfirm = false
                },
                onDismiss = { showDeleteConfirm = false }
            )
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
    error: String? = null,
    inputSize: String = "md",
    inputIntensity: String = "medium"
) {
    val minHeight = when (inputSize) {
        "sm" -> 48.dp
        "lg" -> 64.dp
        else -> 56.dp
    }
    val containerColor = when (inputIntensity) {
        "subtle" -> Color(0x141E232C)
        "strong" -> Color(0x331E232C)
        else -> Color(0x241E232C)
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth().heightIn(min = minHeight),
        enabled = enabled,
        minLines = minLines,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(error)
            }
        },
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedTextColor = textLight,
            unfocusedTextColor = textLight,
            focusedLabelColor = textLight,
            unfocusedLabelColor = textMuted,
            focusedPlaceholderColor = textMuted,
            unfocusedPlaceholderColor = textMuted,
            focusedBorderColor = accentPrimary,
            unfocusedBorderColor = Color(0x55FFFFFF),
            errorBorderColor = accentRed,
            errorLabelColor = accentRed,
            errorSupportingTextColor = accentRed,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            errorContainerColor = Color(0x1AF87171)
        ),
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
            color = textLight,
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
                    label = {
                        Text(
                            text = label,
                            color = if (selected == value) Color(0xFF101828) else textLight
                        )
                    },
                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentPrimary,
                        containerColor = Color(0x1FFFFFFF),
                        selectedLabelColor = Color(0xFF101828),
                        labelColor = textLight
                    )
                )
            }
        }
    }
}

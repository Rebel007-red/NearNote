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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.nearnote.app.data.model.ReminderTask
import kotlinx.coroutines.delay

// Glassmorphism palette aligned with the provided CSS reference.
private val charcoalBg = Color(0xFF111927)
private val charcoalSecondary = Color(0xFF111927)
private val glassPanel = Color(0xAB111928)
private val gradientStart = Color(0xFF17B585)
private val gradientEnd = Color(0xFF111927)
private val radialGlowPrimary = Color(0xFF17B585)
private val radialGlowSecondary = Color(0xFF111927)
private val accentPrimary = Color(0xFFF0B35E)
private val accentGold = Color(0xFFFFC107)
private val accentRed = Color(0xFFEF5350)
private val textLight = Color(0xFFF8FAFC)
private val textMuted = Color(0xFFCBD5E1)
private val glassStroke = Color(0x1FFFFFFF)
private val glassCardShape = RoundedCornerShape(12.dp)
private val glassModalShape = RoundedCornerShape(12.dp)
private const val GLASS_STYLE_PREFS = "glass_style_prefs"
private const val GLASS_INTENSITY_KEY = "glass_intensity"
private const val GLASS_BORDER_KEY = "glass_border"
private const val GLASS_SHADOW_KEY = "glass_shadow"
private const val GLASS_TINT_KEY = "glass_tint"
private const val GLASS_BUTTON_SIZE_KEY = "glass_button_size"
private const val GLASS_INPUT_SIZE_KEY = "glass_input_size"
private const val GLASS_INPUT_INTENSITY_KEY = "glass_input_intensity"
private const val UI_SETTINGS_PREFS = "ui_settings_prefs"
private const val THEME_MODE_KEY = "theme_mode"
private const val ACCENT_COLOR_KEY = "accent_color"
private const val NAV_HEIGHT_KEY = "nav_height"
private const val NAV_RADIUS_KEY = "nav_radius"
private const val NAV_MARGIN_KEY = "nav_margin"
private const val NAV_OPACITY_KEY = "nav_opacity"
private const val NAV_BLUR_KEY = "nav_blur"
private const val TITLE_ALIGN_KEY = "title_align"
private const val SWIPE_THRESHOLD_KEY = "swipe_threshold"
private const val SWIPE_HAPTIC_KEY = "swipe_haptic"
private const val SWIPE_RIGHT_KEY = "swipe_right"
private const val SWIPE_LEFT_KEY = "swipe_left"
private const val LONG_PRESS_EDIT_KEY = "long_press_edit"
private const val DEFAULT_RADIUS_KEY = "default_radius"
private const val DEFAULT_PRIORITY_KEY = "default_priority"
private const val DEFAULT_TRIGGER_KEY = "default_trigger"
private const val DEFAULT_RECURRENCE_KEY = "default_recurrence"

private data class UiSettings(
    val themeMode: String = "SYSTEM",
    val accentHex: String = "#17B585",
    val navHeight: Int = 56,
    val navRadius: Int = 16,
    val navMargin: Int = 20,
    val navOpacity: Float = 0.78f,
    val navBlur: Int = 13,
    val titleAlign: String = "LEFT",
    val swipeThreshold: Float = 0.5f,
    val swipeHaptic: Boolean = true,
    val swipeRightEnabled: Boolean = true,
    val swipeLeftEnabled: Boolean = true,
    val longPressEdit: Boolean = true,
    val defaultRadius: String = "250",
    val defaultPriority: String = NearNoteViewModel.PRIORITY_MEDIUM,
    val defaultTrigger: String = NearNoteViewModel.TRIGGER_ENTER,
    val defaultRecurrence: String = NearNoteViewModel.RECURRENCE_ONCE
)

private data class PopupMessage(
    val id: Long,
    val message: String,
    val isError: Boolean = false
)

private data class UndoNotice(
    val id: Long,
    val message: String,
    val actionType: String,
    val task: ReminderTask
)

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
        "subtle" -> Color(0x8A111928)
        "strong" -> Color(0xC9111928)
        else -> glassPanel
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
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val activeTasks = uiState.tasks.filter { !it.isCompleted }
    val completedTasks = uiState.tasks.filter { it.isCompleted }
    val editorState = uiState.editorState
    val context = LocalContext.current
    val glassPrefs = remember { context.getSharedPreferences(GLASS_STYLE_PREFS, Context.MODE_PRIVATE) }
    val uiPrefs = remember { context.getSharedPreferences(UI_SETTINGS_PREFS, Context.MODE_PRIVATE) }
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
    var themeMode by rememberSaveable { mutableStateOf(uiPrefs.getString(THEME_MODE_KEY, "SYSTEM") ?: "SYSTEM") }
    var customAccentHex by rememberSaveable { mutableStateOf(uiPrefs.getString(ACCENT_COLOR_KEY, "#17B585") ?: "#17B585") }
    var navHeight by rememberSaveable { mutableIntStateOf(uiPrefs.getInt(NAV_HEIGHT_KEY, 56)) }
    var navRadius by rememberSaveable { mutableIntStateOf(uiPrefs.getInt(NAV_RADIUS_KEY, 16)) }
    var navMargin by rememberSaveable { mutableIntStateOf(uiPrefs.getInt(NAV_MARGIN_KEY, 20)) }
    var navOpacity by rememberSaveable { mutableStateOf(uiPrefs.getFloat(NAV_OPACITY_KEY, 0.78f)) }
    var navBlur by rememberSaveable { mutableIntStateOf(uiPrefs.getInt(NAV_BLUR_KEY, 13)) }
    var titleAlign by rememberSaveable { mutableStateOf(uiPrefs.getString(TITLE_ALIGN_KEY, "LEFT") ?: "LEFT") }
    var swipeThreshold by rememberSaveable { mutableStateOf(uiPrefs.getFloat(SWIPE_THRESHOLD_KEY, 0.5f)) }
    var swipeHaptic by rememberSaveable { mutableStateOf(uiPrefs.getBoolean(SWIPE_HAPTIC_KEY, true)) }
    var swipeRightEnabled by rememberSaveable { mutableStateOf(uiPrefs.getBoolean(SWIPE_RIGHT_KEY, true)) }
    var swipeLeftEnabled by rememberSaveable { mutableStateOf(uiPrefs.getBoolean(SWIPE_LEFT_KEY, true)) }
    var longPressEditEnabled by rememberSaveable { mutableStateOf(uiPrefs.getBoolean(LONG_PRESS_EDIT_KEY, true)) }
    var defaultRadius by rememberSaveable { mutableStateOf(uiPrefs.getString(DEFAULT_RADIUS_KEY, "250") ?: "250") }
    var defaultPriority by rememberSaveable {
        mutableStateOf(uiPrefs.getString(DEFAULT_PRIORITY_KEY, NearNoteViewModel.PRIORITY_MEDIUM) ?: NearNoteViewModel.PRIORITY_MEDIUM)
    }
    var defaultTrigger by rememberSaveable {
        mutableStateOf(uiPrefs.getString(DEFAULT_TRIGGER_KEY, NearNoteViewModel.TRIGGER_ENTER) ?: NearNoteViewModel.TRIGGER_ENTER)
    }
    var defaultRecurrence by rememberSaveable {
        mutableStateOf(uiPrefs.getString(DEFAULT_RECURRENCE_KEY, NearNoteViewModel.RECURRENCE_ONCE) ?: NearNoteViewModel.RECURRENCE_ONCE)
    }
    val popupQueue = remember { mutableStateListOf<PopupMessage>() }
    val undoQueue = remember { mutableStateListOf<UndoNotice>() }
    var detailTaskId by rememberSaveable { mutableStateOf<Long?>(null) }
    val haptic = LocalHapticFeedback.current
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
    LaunchedEffect(hasMissingPermissions) {
        showPermissionPopup = hasMissingPermissions
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
    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage ?: return@LaunchedEffect
        val lowered = message.lowercase()
        val isError = listOf("must", "required", "valid", "error", "failed", "cannot").any { lowered.contains(it) }
        if (popupQueue.size >= 3) popupQueue.removeAt(0)
        popupQueue.add(PopupMessage(id = System.currentTimeMillis(), message = message, isError = isError))
        viewModel.clearStatusMessage()
    }

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
                        val titleAlignment = when (titleAlign) {
                            "CENTER" -> Alignment.CenterHorizontally
                            "RIGHT" -> Alignment.End
                            else -> Alignment.Start
                        }
                        val fillModifier = if (titleAlign == "LEFT") Modifier else Modifier.fillMaxWidth()
                        Column(modifier = fillModifier, horizontalAlignment = titleAlignment) {
                        Text(
                            text = if (editorState == null) "NearNote" else if (editorState.id == null) "Add reminder" else "Edit reminder",
                            style = MaterialTheme.typography.titleMedium
                        )
                        }
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
        floatingActionButton = {}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(charcoalBg)
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(radialGlowPrimary.copy(alpha = 0.42f), Color.Transparent),
                            center = Offset(460f, 320f),
                            radius = 920f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(radialGlowSecondary.copy(alpha = 0.9f), Color.Transparent),
                            center = Offset(1100f, 1280f),
                            radius = 1160f
                        )
                    )
            )

            if (editorState == null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .padding(bottom = 128.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedTab == 0) {
                        if (activeTasks.isEmpty()) {
                            item {
                                Text(
                                    text = "Tap + to add",
                                    color = textMuted.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                                )
                            }
                        }
                        items(activeTasks, key = { it.id }) { task ->
                            ReminderCompactCard(
                                style = contentGlassStyle,
                                task = task,
                                onDelete = { pendingDeleteTaskId = task.id },
                                onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                                onEdit = { viewModel.startEditReminder(task) },
                                onTap = { detailTaskId = if (detailTaskId == task.id) null else task.id },
                                swipeThreshold = swipeThreshold,
                                enableSwipeRight = swipeRightEnabled,
                                enableSwipeLeft = swipeLeftEnabled,
                                enableLongPressEdit = longPressEditEnabled,
                                onSwipeComplete = {
                                    if (swipeHaptic) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleTaskCompleted(task)
                                    if (undoQueue.size >= 3) undoQueue.removeAt(0)
                                    undoQueue.add(
                                        UndoNotice(
                                            id = System.currentTimeMillis(),
                                            message = if (task.isCompleted) "Reminder reopened" else "Reminder completed",
                                            actionType = "complete",
                                            task = task
                                        )
                                    )
                                },
                                onSwipeDelete = {
                                    if (swipeHaptic) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.deleteTask(task.id)
                                    if (undoQueue.size >= 3) undoQueue.removeAt(0)
                                    undoQueue.add(
                                        UndoNotice(
                                            id = System.currentTimeMillis(),
                                            message = "Reminder deleted",
                                            actionType = "delete",
                                            task = task
                                        )
                                    )
                                }
                            )
                        }
                    }

                    if (selectedTab == 1) {
                        items(completedTasks, key = { "completed-${it.id}" }) { task ->
                            ReminderCompactCard(
                                style = contentGlassStyle,
                                task = task,
                                onDelete = { pendingDeleteTaskId = task.id },
                                onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                                onEdit = { viewModel.startEditReminder(task) },
                                onTap = { detailTaskId = if (detailTaskId == task.id) null else task.id },
                                swipeThreshold = swipeThreshold,
                                enableSwipeRight = swipeRightEnabled,
                                enableSwipeLeft = swipeLeftEnabled,
                                enableLongPressEdit = longPressEditEnabled,
                                onSwipeComplete = {
                                    if (swipeHaptic) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleTaskCompleted(task)
                                    if (undoQueue.size >= 3) undoQueue.removeAt(0)
                                    undoQueue.add(
                                        UndoNotice(
                                            id = System.currentTimeMillis(),
                                            message = if (task.isCompleted) "Reminder reopened" else "Reminder completed",
                                            actionType = "complete",
                                            task = task
                                        )
                                    )
                                },
                                onSwipeDelete = {
                                    if (swipeHaptic) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.deleteTask(task.id)
                                    if (undoQueue.size >= 3) undoQueue.removeAt(0)
                                    undoQueue.add(
                                        UndoNotice(
                                            id = System.currentTimeMillis(),
                                            message = "Reminder deleted",
                                            actionType = "delete",
                                            task = task
                                        )
                                    )
                                }
                            )
                        }
                    }

                    if (selectedTab == 2) {
                        item {
                            SettingsTabContent(
                                glassStyle = glassStyle,
                                themeMode = themeMode,
                                onThemeModeChange = {
                                    themeMode = it
                                    uiPrefs.edit().putString(THEME_MODE_KEY, it).apply()
                                },
                                accentHex = customAccentHex,
                                onAccentHexChange = {
                                    customAccentHex = it
                                    uiPrefs.edit().putString(ACCENT_COLOR_KEY, it).apply()
                                },
                                navHeight = navHeight,
                                navRadius = navRadius,
                                navMargin = navMargin,
                                navOpacity = navOpacity,
                                navBlur = navBlur,
                                onNavHeightChange = {
                                    navHeight = it
                                    uiPrefs.edit().putInt(NAV_HEIGHT_KEY, it).apply()
                                },
                                onNavRadiusChange = {
                                    navRadius = it
                                    uiPrefs.edit().putInt(NAV_RADIUS_KEY, it).apply()
                                },
                                onNavMarginChange = {
                                    navMargin = it
                                    uiPrefs.edit().putInt(NAV_MARGIN_KEY, it).apply()
                                },
                                onNavOpacityChange = {
                                    navOpacity = it
                                    uiPrefs.edit().putFloat(NAV_OPACITY_KEY, it).apply()
                                },
                                onNavBlurChange = {
                                    navBlur = it
                                    uiPrefs.edit().putInt(NAV_BLUR_KEY, it).apply()
                                },
                                titleAlign = titleAlign,
                                onTitleAlignChange = {
                                    titleAlign = it
                                    uiPrefs.edit().putString(TITLE_ALIGN_KEY, it).apply()
                                },
                                swipeThreshold = swipeThreshold,
                                swipeHaptic = swipeHaptic,
                                swipeRightEnabled = swipeRightEnabled,
                                swipeLeftEnabled = swipeLeftEnabled,
                                longPressEditEnabled = longPressEditEnabled,
                                onSwipeThresholdChange = {
                                    swipeThreshold = it
                                    uiPrefs.edit().putFloat(SWIPE_THRESHOLD_KEY, it).apply()
                                },
                                onSwipeHapticChange = {
                                    swipeHaptic = it
                                    uiPrefs.edit().putBoolean(SWIPE_HAPTIC_KEY, it).apply()
                                },
                                onSwipeRightChange = {
                                    swipeRightEnabled = it
                                    uiPrefs.edit().putBoolean(SWIPE_RIGHT_KEY, it).apply()
                                },
                                onSwipeLeftChange = {
                                    swipeLeftEnabled = it
                                    uiPrefs.edit().putBoolean(SWIPE_LEFT_KEY, it).apply()
                                },
                                onLongPressEditChange = {
                                    longPressEditEnabled = it
                                    uiPrefs.edit().putBoolean(LONG_PRESS_EDIT_KEY, it).apply()
                                },
                                defaultRadius = defaultRadius,
                                defaultPriority = defaultPriority,
                                defaultTrigger = defaultTrigger,
                                defaultRecurrence = defaultRecurrence,
                                onDefaultRadiusChange = {
                                    defaultRadius = it
                                    uiPrefs.edit().putString(DEFAULT_RADIUS_KEY, it).apply()
                                },
                                onDefaultPriorityChange = {
                                    defaultPriority = it
                                    uiPrefs.edit().putString(DEFAULT_PRIORITY_KEY, it).apply()
                                },
                                onDefaultTriggerChange = {
                                    defaultTrigger = it
                                    uiPrefs.edit().putString(DEFAULT_TRIGGER_KEY, it).apply()
                                },
                                onDefaultRecurrenceChange = {
                                    defaultRecurrence = it
                                    uiPrefs.edit().putString(DEFAULT_RECURRENCE_KEY, it).apply()
                                },
                                onResetUiDefaults = {
                                    themeMode = "SYSTEM"
                                    titleAlign = "LEFT"
                                    navHeight = 56
                                    navRadius = 16
                                    navMargin = 20
                                    navOpacity = 0.78f
                                    navBlur = 13
                                },
                                onResetAllVisuals = {
                                    glassIntensity = DefaultGlassStyle.intensity
                                    glassBorder = DefaultGlassStyle.border
                                    glassShadow = DefaultGlassStyle.shadow
                                    glassTint = DefaultGlassStyle.tint
                                    glassButtonSize = DefaultGlassStyle.buttonSize
                                    glassInputSize = DefaultGlassStyle.inputSize
                                    glassInputIntensity = DefaultGlassStyle.inputIntensity
                                    customAccentHex = "#17B585"
                                }
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

            AnimatedVisibility(
                visible = hasMissingPermissions && showPermissionPopup,
                enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 8 },
                exit = fadeOut(tween(180)) + slideOutVertically(tween(180)) { it / 8 }
            ) {
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

            if (editorState == null && selectedTab == 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = tintedPanelColor(glassStyle).copy(alpha = 0.78f),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(end = 24.dp, bottom = 94.dp)
                        .border(
                            width = 1.dp,
                            color = strokeFor(glassStyle),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    IconButton(
                        onClick = {
                            viewModel.setEditorState(
                                ReminderEditorState(
                                    radiusMeters = defaultRadius,
                                    priority = defaultPriority,
                                    triggerMode = defaultTrigger,
                                    dwellMinutes = if (defaultTrigger == NearNoteViewModel.TRIGGER_ENTER) "0" else "2",
                                    recurrenceType = defaultRecurrence
                                )
                            )
                        },
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add task",
                            tint = textLight
                        )
                    }
                }
            }

            if (editorState == null) {
                GlassBottomNavBar(
                    selectedIndex = selectedTab,
                    onSelected = { selectedTab = it },
                    navHeight = navHeight,
                    navRadius = navRadius,
                    navOpacity = navOpacity,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = navMargin.dp, vertical = 14.dp)
                )
            }

            detailTaskId?.let { selectedId ->
                uiState.tasks.firstOrNull { it.id == selectedId }?.let { task ->
                    TaskDetailsPopup(
                        style = glassStyle,
                        task = task,
                        onClose = { detailTaskId = null },
                        onComplete = {
                            viewModel.toggleTaskCompleted(task)
                            detailTaskId = null
                        },
                        onDelete = {
                            pendingDeleteTaskId = task.id
                            detailTaskId = null
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = (navHeight + 40).dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                undoQueue.forEach { notice ->
                    LaunchedEffect(notice.id) {
                        delay(4_000)
                        undoQueue.removeAll { it.id == notice.id }
                    }
                    GlassUndoPopup(
                        style = glassStyle,
                        message = notice.message,
                        onUndo = {
                            when (notice.actionType) {
                                "delete" -> viewModel.restoreTask(notice.task)
                                "complete" -> {
                                    val toggledTask = notice.task.copy(isCompleted = !notice.task.isCompleted)
                                    viewModel.toggleTaskCompleted(toggledTask)
                                }
                            }
                            undoQueue.removeAll { it.id == notice.id }
                        },
                        onDismiss = { undoQueue.removeAll { it.id == notice.id } }
                    )
                }
                popupQueue.forEach { popup ->
                    LaunchedEffect(popup.id) {
                        if (!popup.isError) {
                            delay(4_000)
                            popupQueue.removeAll { it.id == popup.id }
                        }
                    }
                    GlassStatusPopup(
                        style = glassStyle,
                        message = popup.message,
                        isError = popup.isError,
                        onDismiss = { popupQueue.removeAll { it.id == popup.id } }
                    )
                }
            }

            pendingDeleteTaskId?.let { taskId ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 6 },
                    exit = fadeOut(tween(160)) + slideOutVertically(tween(160)) { it / 8 }
                ) {
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
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ReminderCompactCard(
    style: GlassStyle = DefaultGlassStyle,
    task: ReminderTask,
    onDelete: () -> Unit,
    onToggleCompleted: () -> Unit,
    onEdit: () -> Unit,
    onTap: () -> Unit,
    swipeThreshold: Float,
    enableSwipeRight: Boolean,
    enableSwipeLeft: Boolean,
    enableLongPressEdit: Boolean,
    onSwipeComplete: () -> Unit,
    onSwipeDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (enableSwipeRight) {
                        onSwipeComplete()
                        true
                    } else {
                        false
                    }
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    if (enableSwipeLeft) {
                        onSwipeDelete()
                        true
                    } else {
                        false
                    }
                }

                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * swipeThreshold }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(tintedPanelColor(style).copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Complete", color = accentGold)
                Text("Delete", color = accentRed)
            }
        }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = tintedPanelColor(style).copy(alpha = if (task.isCompleted) 0.6f else 0.76f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    width = 1.dp,
                    color = strokeFor(style),
                    shape = RoundedCornerShape(16.dp)
                )
                .combinedClickable(
                    onClick = onTap,
                    onLongClick = { if (enableLongPressEdit) onEdit() }
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) textMuted else textLight,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = textLight
                        )
                    }
                    IconButton(onClick = onToggleCompleted) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = if (task.isCompleted) "Reopen" else "Complete",
                            tint = if (task.isCompleted) textMuted else accentGold
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = accentRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassBottomNavBar(
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    navHeight: Int,
    navRadius: Int,
    navOpacity: Float,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("Active", Icons.Filled.Home, 0),
        Triple("Completed", Icons.Filled.CheckCircle, 1),
        Triple("Settings", Icons.Filled.Settings, 2)
    )

    Surface(
        shape = RoundedCornerShape(navRadius.dp),
        color = glassPanel.copy(alpha = navOpacity),
        shadowElevation = 10.dp,
        modifier = modifier.border(
            width = 1.dp,
            color = glassStroke,
            shape = RoundedCornerShape(navRadius.dp)
        )
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(navHeight.dp)
        ) {
            items.forEach { (label, icon, index) ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { onSelected(index) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label
                        )
                    },
                    label = { Text(label) },
                    alwaysShowLabel = true
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = glassPanel.copy(alpha = 0.75f),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, glassStroke, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, color = textLight, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun SettingToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = textLight, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsTabContent(
    glassStyle: GlassStyle,
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    accentHex: String,
    onAccentHexChange: (String) -> Unit,
    navHeight: Int,
    navRadius: Int,
    navMargin: Int,
    navOpacity: Float,
    navBlur: Int,
    onNavHeightChange: (Int) -> Unit,
    onNavRadiusChange: (Int) -> Unit,
    onNavMarginChange: (Int) -> Unit,
    onNavOpacityChange: (Float) -> Unit,
    onNavBlurChange: (Int) -> Unit,
    titleAlign: String,
    onTitleAlignChange: (String) -> Unit,
    swipeThreshold: Float,
    swipeHaptic: Boolean,
    swipeRightEnabled: Boolean,
    swipeLeftEnabled: Boolean,
    longPressEditEnabled: Boolean,
    onSwipeThresholdChange: (Float) -> Unit,
    onSwipeHapticChange: (Boolean) -> Unit,
    onSwipeRightChange: (Boolean) -> Unit,
    onSwipeLeftChange: (Boolean) -> Unit,
    onLongPressEditChange: (Boolean) -> Unit,
    defaultRadius: String,
    defaultPriority: String,
    defaultTrigger: String,
    defaultRecurrence: String,
    onDefaultRadiusChange: (String) -> Unit,
    onDefaultPriorityChange: (String) -> Unit,
    onDefaultTriggerChange: (String) -> Unit,
    onDefaultRecurrenceChange: (String) -> Unit,
    onResetUiDefaults: () -> Unit,
    onResetAllVisuals: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingsSectionCard(title = "Appearance") {
            OptionGroup(
                title = "Theme mode",
                options = listOf("SYSTEM" to "System", "DARK" to "Dark", "LIGHT" to "Light"),
                selected = themeMode,
                onSelected = onThemeModeChange
            )
            EditorTextField(
                label = "Accent hex",
                value = accentHex,
                onValueChange = { input ->
                    val normalized = input.uppercase().filter { it.isDigit() || it in "#ABCDEF" }
                    onAccentHexChange(if (normalized.startsWith("#")) normalized else "#$normalized")
                },
                placeholder = "#17B585",
                inputSize = glassStyle.inputSize,
                inputIntensity = glassStyle.inputIntensity
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("#17B585", "#334155", "#F97316", "#93C5FD").forEach { swatch ->
                    FilterChip(
                        selected = accentHex == swatch,
                        onClick = { onAccentHexChange(swatch) },
                        label = { Text(swatch) }
                    )
                }
            }
        }

        SettingsSectionCard(title = "Layout") {
            OptionGroup(
                title = "Title alignment",
                options = listOf("LEFT" to "Left", "CENTER" to "Center", "RIGHT" to "Right"),
                selected = titleAlign,
                onSelected = onTitleAlignChange
            )
            Text("Nav height: ${navHeight}dp", color = textMuted)
            Slider(value = navHeight.toFloat(), onValueChange = { onNavHeightChange(it.toInt()) }, valueRange = 48f..72f)
            Text("Nav radius: ${navRadius}dp", color = textMuted)
            Slider(value = navRadius.toFloat(), onValueChange = { onNavRadiusChange(it.toInt()) }, valueRange = 12f..28f)
            Text("Nav margin: ${navMargin}dp", color = textMuted)
            Slider(value = navMargin.toFloat(), onValueChange = { onNavMarginChange(it.toInt()) }, valueRange = 12f..32f)
            Text("Nav opacity: ${"%.2f".format(navOpacity)}", color = textMuted)
            Slider(value = navOpacity, onValueChange = onNavOpacityChange, valueRange = 0.55f..0.95f)
            Text("Nav blur: ${navBlur}", color = textMuted)
            Slider(value = navBlur.toFloat(), onValueChange = { onNavBlurChange(it.toInt()) }, valueRange = 0f..20f)
        }

        SettingsSectionCard(title = "Card behavior") {
            Text("Swipe threshold: ${"%.2f".format(swipeThreshold)}", color = textMuted)
            Slider(value = swipeThreshold, onValueChange = onSwipeThresholdChange, valueRange = 0.2f..0.8f)
            SettingToggleRow("Swipe right to complete", swipeRightEnabled, onSwipeRightChange)
            SettingToggleRow("Swipe left to delete", swipeLeftEnabled, onSwipeLeftChange)
            SettingToggleRow("Long press to edit", longPressEditEnabled, onLongPressEditChange)
            SettingToggleRow("Haptic feedback", swipeHaptic, onSwipeHapticChange)
        }

        SettingsSectionCard(title = "Default new task") {
            EditorTextField(
                label = "Default radius",
                value = defaultRadius,
                onValueChange = { onDefaultRadiusChange(it.filter(Char::isDigit)) },
                placeholder = "250",
                keyboardType = KeyboardType.Number,
                inputSize = glassStyle.inputSize,
                inputIntensity = glassStyle.inputIntensity
            )
            OptionGroup(
                title = "Priority",
                options = listOf("LOW" to "Low", "MEDIUM" to "Medium", "HIGH" to "High"),
                selected = defaultPriority,
                onSelected = onDefaultPriorityChange
            )
            OptionGroup(
                title = "Trigger",
                options = listOf("ENTER" to "Enter", "ENTER_DWELL" to "Enter + dwell", "EXIT" to "Exit"),
                selected = defaultTrigger,
                onSelected = onDefaultTriggerChange
            )
            OptionGroup(
                title = "Recurrence",
                options = listOf("ONCE" to "None", "DAILY" to "Daily", "WEEKLY" to "Weekly", "MONTHLY" to "Monthly", "CUSTOM" to "Custom"),
                selected = defaultRecurrence,
                onSelected = onDefaultRecurrenceChange
            )
        }

        SettingsSectionCard(title = "Preview") {
            Text("Icon labels are preview-only as requested.", color = textMuted)
            GlassBottomNavBar(
                selectedIndex = 0,
                onSelected = {},
                navHeight = navHeight,
                navRadius = navRadius,
                navOpacity = navOpacity
            )
        }

        SettingsSectionCard(title = "Reset") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassButton(
                    text = "Reset UI",
                    onClick = onResetUiDefaults,
                    style = glassStyle,
                    size = "sm",
                    variant = "outline",
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "Reset visuals",
                    onClick = onResetAllVisuals,
                    style = glassStyle,
                    size = "sm",
                    variant = "outline",
                    textColor = accentRed,
                    modifier = Modifier.weight(1f)
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
        shape = glassCardShape,
        color = tintedPanelColor(style),
        shadowElevation = elevationFor(style),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = strokeFor(style),
                shape = glassCardShape
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
        shape = glassCardShape,
        color = tintedPanelColor(style),
        shadowElevation = elevationFor(style),
        modifier = Modifier
            .fillMaxWidth()
            .background(heroGradient)
            .border(
                width = 1.dp,
                color = strokeFor(style),
                shape = glassCardShape
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
                text = "Geo reminders",
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
        shape = glassCardShape,
        color = tintedPanelColor(style),
        shadowElevation = elevationFor(style),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = strokeFor(style),
                shape = glassCardShape
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
                text = "Tap Add to start",
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
        shape = glassCardShape,
        color = tintedPanelColor(style),
        shadowElevation = elevationFor(style),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = strokeFor(style),
                shape = glassCardShape
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
    var cardVisible by rememberSaveable(task.id) { mutableStateOf(false) }

    LaunchedEffect(task.id) {
        cardVisible = true
    }

    val titleColor = if (task.isCompleted) textMuted else textLight
    val subtitleColor = if (task.isCompleted) Color(0xFF616161) else Color(0xFFBBBBBB)
    
    // Gradient for card background (left to right)
    val gradientBrush = Brush.horizontalGradient(
        colors = if (task.isCompleted) 
            listOf(Color(0x66666666), Color(0x4D666666))
        else
            listOf(gradientStart, gradientEnd)
    )

    AnimatedVisibility(
        visible = cardVisible,
        enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { it / 8 },
        exit = fadeOut(tween(160)) + slideOutVertically(tween(160)) { it / 10 }
    ) {
        Card(
            shape = glassCardShape,
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
                    shape = glassCardShape
                )
                .graphicsLayer {
                    if (!task.isCompleted) {
                        shadowElevation = 18.dp.toPx()
                    }
                }
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
                if (!task.isCompleted) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        gradientStart.copy(alpha = 0.18f),
                                        Color.Transparent
                                    ),
                                    center = Offset(120f, 50f),
                                    radius = 420f
                                )
                            )
                    )
                }

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
                            text = "Hold for details",
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
                        text = "${task.latitude}, ${task.longitude}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Action buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassButton(
                            text = "Edit",
                            onClick = onEdit,
                            style = style,
                            size = "md",
                            variant = "outline",
                            modifier = Modifier.weight(1f)
                        )
                        GlassButton(
                            text = if (task.isCompleted) "Reopen" else "Complete",
                            onClick = onToggleCompleted,
                            style = style,
                            size = "md",
                            variant = "outline",
                            modifier = Modifier.weight(1f)
                        )
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
        color = tintedPanelColor(style).copy(alpha = 0.67f),
        shape = glassCardShape,
        shadowElevation = 3.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = strokeFor(style),
            shape = glassCardShape
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
    val bgColor = when (variant) {
        "solid" -> accentPrimary
        "outline" -> tintedPanelColor(style).copy(alpha = 0.52f)
        else -> Color.Transparent
    }

    when (variant) {
        "solid" -> {
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier.heightIn(min = minHeight),
                shape = glassCardShape,
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
                shape = glassCardShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = bgColor,
                    contentColor = contentColor,
                    disabledContainerColor = bgColor.copy(alpha = 0.45f),
                    disabledContentColor = contentColor.copy(alpha = 0.6f)
                ),
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
                modifier = modifier.heightIn(min = minHeight),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor,
                    disabledContentColor = contentColor.copy(alpha = 0.6f)
                )
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
            shape = glassModalShape,
            color = tintedPanelColor(style),
            shadowElevation = elevationFor(style),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .border(
                    width = 1.dp,
                    color = strokeFor(style),
                    shape = glassModalShape
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
    isError: Boolean = false,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = glassCardShape,
        color = tintedPanelColor(style),
        shadowElevation = elevationFor(style),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isError) accentRed else strokeFor(style),
                shape = glassCardShape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isError) {
                Text("!", color = accentRed, modifier = Modifier.padding(end = 6.dp))
            }
            Text(
                text = message,
                color = if (isError) Color(0xFFFFC9C9) else textLight,
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
private fun GlassUndoPopup(
    style: GlassStyle,
    message: String,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        shape = glassCardShape,
        color = tintedPanelColor(style),
        shadowElevation = elevationFor(style),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, strokeFor(style), glassCardShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(message, color = textLight, modifier = Modifier.weight(1f))
            GlassButton(text = "Undo", onClick = onUndo, style = style, size = "sm", variant = "outline")
            IconButton(onClick = onDismiss) {
                Text("x", color = textMuted)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun TaskDetailsPopup(
    style: GlassStyle,
    task: ReminderTask,
    onClose: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val triggerFriendly = when (task.triggerMode) {
        NearNoteViewModel.TRIGGER_ENTER -> "Enter region"
        NearNoteViewModel.TRIGGER_ENTER_DWELL -> "Enter and dwell"
        NearNoteViewModel.TRIGGER_EXIT -> "Exit region"
        else -> task.triggerMode
    }
    val recurrenceFriendly = when (task.recurrenceType) {
        NearNoteViewModel.RECURRENCE_ONCE -> "None"
        NearNoteViewModel.RECURRENCE_DAILY -> "Daily"
        NearNoteViewModel.RECURRENCE_WEEKLY -> "Weekly"
        NearNoteViewModel.RECURRENCE_MONTHLY -> "Monthly"
        NearNoteViewModel.RECURRENCE_CUSTOM -> {
            if (task.recurrenceDays.isNotBlank()) {
                "On ${task.recurrenceDays.replace(",", ", ")}"
            } else {
                "Every ${task.recurrenceInterval ?: 1} days"
            }
        }
        else -> task.recurrenceType
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = tintedPanelColor(style),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(1.dp, strokeFor(style), RoundedCornerShape(16.dp))
                .combinedClickable(onClick = onClose, onLongClick = {})
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(task.title, color = textLight, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Place: ${task.placeName}", color = textMuted)
                Text("Radius: ${task.radiusMeters}m", color = textMuted)
                Text("Trigger: $triggerFriendly", color = textMuted)
                Text("Recurrence: $recurrenceFriendly", color = textMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    GlassButton(text = "Close", onClick = onClose, style = style, size = "sm", variant = "outline", modifier = Modifier.weight(1f))
                    GlassButton(
                        text = if (task.isCompleted) "Reopen" else "Complete",
                        onClick = onComplete,
                        style = style,
                        size = "sm",
                        variant = "outline",
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "Delete",
                        onClick = onDelete,
                        style = style,
                        size = "sm",
                        variant = "outline",
                        textColor = accentRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
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
            shape = glassModalShape,
            color = tintedPanelColor(style),
            shadowElevation = elevationFor(style),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .border(
                    width = 1.dp,
                    color = strokeFor(style),
                    shape = glassModalShape
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
        if (editorState.recurrenceCustomMode == NearNoteViewModel.CUSTOM_MODE_DAYS) {
            when {
                editorState.recurrenceInterval.isBlank() -> "Interval is required"
                intervalValue == null -> "Enter a whole number"
                intervalValue !in 1..365 -> "Must be 1 to 365"
                else -> null
            }
        } else {
            null
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
            OptionGroup(
                title = "Custom mode",
                options = listOf(
                    NearNoteViewModel.CUSTOM_MODE_DAYS to "Days",
                    NearNoteViewModel.CUSTOM_MODE_WEEKDAYS to "Weekdays"
                ),
                selected = editorState.recurrenceCustomMode,
                onSelected = {
                    onEditorChange(
                        editorState.copy(
                            recurrenceCustomMode = it,
                            recurrenceWeekdays = if (it == NearNoteViewModel.CUSTOM_MODE_WEEKDAYS) {
                                editorState.recurrenceWeekdays.ifEmpty { setOf("MON", "TUE", "WED", "THU", "FRI") }
                            } else {
                                editorState.recurrenceWeekdays
                            }
                        )
                    )
                }
            )

            if (editorState.recurrenceCustomMode == NearNoteViewModel.CUSTOM_MODE_DAYS) {
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
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = editorState.recurrenceWeekdays.containsAll(listOf("MON", "TUE", "WED", "THU", "FRI")),
                        onClick = {
                            onEditorChange(editorState.copy(recurrenceWeekdays = setOf("MON", "TUE", "WED", "THU", "FRI")))
                        },
                        label = { Text("Mon-Fri") }
                    )
                    listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
                        val selected = editorState.recurrenceWeekdays.contains(day)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val updated = editorState.recurrenceWeekdays.toMutableSet()
                                if (selected) updated.remove(day) else updated.add(day)
                                onEditorChange(
                                    editorState.copy(
                                        recurrenceWeekdays = if (updated.isEmpty()) setOf("MON", "TUE", "WED", "THU", "FRI") else updated
                                    )
                                )
                            },
                            label = { Text(day) }
                        )
                    }
                }
            }
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

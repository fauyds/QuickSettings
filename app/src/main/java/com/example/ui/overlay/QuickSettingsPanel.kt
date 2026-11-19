package com.example.ui.overlay

import com.example.R
import androidx.compose.foundation.layout.defaultMinSize
import com.example.ui.notification.MonetThemeHelper
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.runtime.DisposableEffect
import com.example.data.EditButtonPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import com.example.data.TileShape
import com.example.data.ProgressBarStyle
import com.example.data.QsPageIndicatorStyle
import com.example.data.BatteryPercentageLocation
import com.example.data.SettingsIconLocation
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.text.format.DateUtils
import com.example.data.local.TileEntity
import com.example.model.TileCategory
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawOutline
import com.example.ui.shape.insideShapeBorder
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.lerp
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import kotlin.math.abs
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.AutoAwesome
import com.example.ui.tile.TileEditorFilter
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.Dispatchers
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import android.graphics.Bitmap
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentHeight
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.RecyclerView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.data.NotificationBlacklistManager
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.collectAsState
import com.example.ui.components.WavySlider
import com.example.ui.components.QsProgressSlider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.model.MediaState
import com.example.model.NotificationActionModel
import com.example.model.NotificationItem
import com.example.model.QsGridLayout
import com.example.model.QsTileModel
import com.example.system.ExternalTileIcon
import com.example.model.TileState
import com.example.service.QuickPanelAccessibilityService
import com.example.service.QuickPanelNotificationListenerService
import com.example.ui.theme.DarkUiSurface
import com.example.ui.theme.DarkUiSurfaceVariant
import com.example.ui.theme.ExpressiveCardShapes
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.MonetOnPrimary
import com.example.ui.theme.MonetPrimary
import com.example.ui.theme.MonetPrimaryContainer
import com.example.ui.theme.MonetTertiary
import com.example.ui.theme.TileActiveBg
import com.example.ui.theme.TileActiveText
import com.example.ui.theme.TileInactiveBg
import com.example.ui.theme.TileInactiveText
import com.example.ui.theme.TileUnavailableBg
import com.example.ui.theme.TileUnavailableText
import com.example.ui.theme.LightTileActiveBg
import com.example.ui.theme.LightTileActiveText
import com.example.ui.theme.LightTileInactiveBg
import com.example.ui.theme.LightTileInactiveText
import com.example.ui.theme.LightTileUnavailableBg
import com.example.ui.theme.LightTileUnavailableText
import androidx.compose.runtime.collectAsState
import com.example.data.PanelPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class QsExpansionStage {
    COLLAPSED, // Fully closed
    QQS,       // Stage 1: Compact Quick Quick Settings (Circles only, no labels)
    FULL_QS    // Stage 2: Expanded Full Quick Settings
}

// 95% solid black background tint in light and dark modes
val TranslucentBlackBg = Color.Black.copy(alpha = 0.95f)

@Composable
fun QuickSettingsPanel(
    expansionStage: QsExpansionStage,
    onStageChange: (QsExpansionStage) -> Unit,
    tiles: List<QsTileModel>,
    onTileClick: (QsTileModel) -> Unit,
    onTileLongClick: (QsTileModel) -> Unit,
    brightness: Int,
    onBrightnessChange: (Int) -> Unit,
    mediaVolume: Float = 0f,
    ringVolume: Float = 0f,
    alarmVolume: Float = 0f,
    notificationVolume: Float = 0f,
    onVolumeChange: (streamType: Int, fraction: Float) -> Unit = { _, _ -> },
    gridLayout: QsGridLayout = QsGridLayout.DEFAULT,
    notifications: List<NotificationItem> = emptyList(),
    isNotificationPermissionGranted: Boolean = true,
    onNotificationClick: (NotificationItem) -> Unit = {},
    onNotificationDismiss: (NotificationItem) -> Unit = {},
    onNotificationActionClick: (NotificationActionModel) -> Unit = {},
    onDismissAllNotifications: () -> Unit = {},
    onRequestNotificationAccess: () -> Unit = {},
    onNavigateToCustomizer: () -> Unit = {},
    onNavigateToNotificationHub: () -> Unit = {},
    onTilesReordered: ((activeTiles: List<QsTileModel>, inactiveTiles: List<QsTileModel>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val animSpeedScale by com.example.data.PanelPreferences.animationSpeedScale.collectAsState()
    val speedFactor = animSpeedScale.coerceAtLeast(0.01f)
    val disablePanelAnimations by com.example.data.PanelPreferences.disablePanelAnimations.collectAsState()

    val density = LocalDensity.current
    val maxDragDistancePx = with(density) { 300.dp.toPx() }
    val coroutineScope = rememberCoroutineScope()

    var isEditingTiles by remember { mutableStateOf(false) }
    var isTileDragging by remember { mutableStateOf(false) }
    var isScreenRecordConfigOpen by remember { mutableStateOf(false) }
    var isNotificationManagerOpen by remember { mutableStateOf(false) }
    val editScrollState = rememberScrollState()
    val hiddenTileIds by PanelPreferences.hiddenTileIds.collectAsState()

    var editActiveTiles by remember { mutableStateOf<List<QsTileModel>?>(null) }
    var editInactiveTiles by remember { mutableStateOf<List<QsTileModel>?>(null) }

    val enterEditMode = {
        val nonBlacklisted = tiles.filter {
            it.id !in hiddenTileIds &&
            (if (it.id == "sound_profile") "sound_mode" !in hiddenTileIds && "volume" !in hiddenTileIds else true)
        }
        val active = nonBlacklisted.filter { it.isVisible }
        val inactive = nonBlacklisted.filter { !it.isVisible }
        editActiveTiles = active
        editInactiveTiles = inactive
        isEditingTiles = true
    }

    val persistReorder: (List<QsTileModel>, List<QsTileModel>) -> Unit = { activeList, inactiveList ->
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val db = com.example.data.local.QuickPanelDatabase.getInstance(context)
                val repo = com.example.data.repository.TileRepository(db.tileDao())
                val entities = mutableListOf<TileEntity>()
                var order = 0
                for (tile in activeList) {
                    val isExt = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.category == TileCategory.EXTERNAL || tile.id.contains("/")
                    val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                    val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                    val compId = if (isExt && pkg != null && cls != null) "$pkg/$cls" else tile.id

                    val entity = TileEntity(
                        tileId = compId,
                        label = tile.label,
                        subtitle = tile.subtitle,
                        categoryName = if (isExt) TileCategory.THIRD_PARTY.name else tile.category.name,
                        orderIndex = order++,
                        isVisible = true,
                        isQqsFavorite = tile.isQqsFavorite,
                        isExternal = isExt,
                        packageName = pkg,
                        serviceClassName = cls,
                        settingsAction = tile.settingsAction
                    )
                    entities.add(entity)
                }
                for (tile in inactiveList) {
                    val isExt = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.category == TileCategory.EXTERNAL || tile.id.contains("/")
                    val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                    val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                    val compId = if (isExt && pkg != null && cls != null) "$pkg/$cls" else tile.id

                    val entity = TileEntity(
                        tileId = compId,
                        label = tile.label,
                        subtitle = tile.subtitle,
                        categoryName = if (isExt) TileCategory.THIRD_PARTY.name else tile.category.name,
                        orderIndex = order++,
                        isVisible = false,
                        isQqsFavorite = tile.isQqsFavorite,
                        isExternal = isExt,
                        packageName = pkg,
                        serviceClassName = cls,
                        settingsAction = tile.settingsAction
                    )
                    entities.add(entity)
                }
                repo.insertAll(entities)
                PanelPreferences.saveActiveTileIds(context, activeList.map { it.id })
            } catch (e: Exception) {
                Log.e("QuickSettingsPanel", "Failed to persist tile reorder: ${e.message}")
            }
        }
    }

    val handleTilesUpdated: (List<QsTileModel>, List<QsTileModel>) -> Unit = { newActive, newInactive ->
        editActiveTiles = newActive
        editInactiveTiles = newInactive
        persistReorder(newActive, newInactive)
        onTilesReordered?.invoke(newActive, newInactive)
    }

    val saveAndExitEditMode: () -> Unit = {
        val finalActive = editActiveTiles
        val finalInactive = editInactiveTiles
        if (finalActive != null && finalInactive != null) {
            persistReorder(finalActive, finalInactive)
            onTilesReordered?.invoke(finalActive, finalInactive)
        }
        isEditingTiles = false
        editActiveTiles = null
        editInactiveTiles = null
    }

    LaunchedEffect(isEditingTiles) {
        if (isEditingTiles) {
            editScrollState.scrollTo(0)
        }
    }

    val dragTranslationYState = remember { mutableFloatStateOf(0f) }
    var manualDragMorphFraction by remember { mutableFloatStateOf(0f) }
    var isDraggingMorph by remember { mutableStateOf(false) }
    val morphDragDistancePx = with(density) { 150.dp.toPx() }

    val isFullQs = expansionStage == QsExpansionStage.FULL_QS

    LaunchedEffect(expansionStage) {
        if (expansionStage != QsExpansionStage.COLLAPSED) {
            com.example.system.NativeTileManager.instance?.refreshAllStates()
        }
        if (expansionStage == QsExpansionStage.COLLAPSED && isEditingTiles) {
            saveAndExitEditMode()
        }
    }

    val activeTiles = remember(tiles) { tiles.filter { it.isVisible } }
    val isDark = LocalIsDarkTheme.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val shadeDragModifier = Modifier.pointerInput(maxDragDistancePx, expansionStage, isEditingTiles, isLandscape) {
        if (isEditingTiles || isLandscape) return@pointerInput // Disable vertical swipe collapse while editing tiles or in landscape mode
        var dragAccumulatedY = 0f
        detectVerticalDragGestures(
            onDragStart = {
                dragAccumulatedY = 0f
                dragTranslationYState.floatValue = 0f
                manualDragMorphFraction = if (expansionStage == QsExpansionStage.FULL_QS) 1f else 0f
                isDraggingMorph = false
                com.example.system.NativeTileManager.instance?.isDraggingShade = true
                com.example.system.CpuPerformanceManager.getInstance(context).onInteractionStart()
            },
            onDragCancel = {
                val draggedDownPx = dragAccumulatedY.coerceAtLeast(0f)
                val dismissThresholdPx = with(density) { 36.dp.toPx() }
                if (expansionStage == QsExpansionStage.QQS && (draggedDownPx >= dismissThresholdPx || manualDragMorphFraction > 0.25f)) {
                    // Swiped down past threshold: smoothly expand to FULL_QS rather than getting stuck or snapping closed
                    isDraggingMorph = false
                    dragTranslationYState.floatValue = 0f
                    onStageChange(QsExpansionStage.FULL_QS)
                } else {
                    dragTranslationYState.floatValue = 0f
                    isDraggingMorph = false
                    manualDragMorphFraction = if (expansionStage == QsExpansionStage.FULL_QS) 1f else 0f
                }
                com.example.system.NativeTileManager.instance?.isDraggingShade = false
                com.example.system.CpuPerformanceManager.getInstance(context).onInteractionEnd()
            },
            onDragEnd = {
                com.example.system.NativeTileManager.instance?.isDraggingShade = false
                com.example.system.CpuPerformanceManager.getInstance(context).onInteractionEnd()
                val dismissThresholdPx = with(density) { 36.dp.toPx() } // Require dragging up by at least 36dp
                val draggedUpPx = (-dragAccumulatedY).coerceAtLeast(0f)
                val translationUpPx = (-dragTranslationYState.floatValue).coerceAtLeast(0f)
                val isDraggedUp = draggedUpPx >= dismissThresholdPx || translationUpPx >= dismissThresholdPx

                if (expansionStage == QsExpansionStage.FULL_QS) {
                    if (isDraggedUp) {
                        // Swiped UP in FULL_QS -> Collapse to QQS (or directly close panel for KitKat style)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        dragTranslationYState.floatValue = 0f
                        if (com.example.data.PanelPreferences.isAndroidKitKatStyle(context)) {
                            onStageChange(QsExpansionStage.COLLAPSED)
                        } else {
                            onStageChange(QsExpansionStage.QQS)
                        }
                    } else {
                        // Dragged UP less than 36dp -> Snap back open!
                        if (disablePanelAnimations) {
                            dragTranslationYState.floatValue = 0f
                        } else {
                            coroutineScope.launch {
                                val anim = androidx.compose.animation.core.Animatable(dragTranslationYState.floatValue)
                                anim.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 400f)) {
                                    dragTranslationYState.floatValue = this.value
                                }
                            }
                        }
                    }
                    isDraggingMorph = false
                    dragTranslationYState.floatValue = 0f
                } else if (expansionStage == QsExpansionStage.QQS) {
                    if (isDraggedUp) {
                        // Swiped UP in QQS -> Trigger smooth dismissal following finger!
                        isDraggingMorph = false
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val panelHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                        val startVal = dragTranslationYState.floatValue
                        if (disablePanelAnimations) {
                            dragTranslationYState.floatValue = 0f
                            onStageChange(QsExpansionStage.COLLAPSED)
                        } else {
                            coroutineScope.launch {
                                val anim = androidx.compose.animation.core.Animatable(startVal)
                                anim.animateTo(
                                    targetValue = -panelHeightPx,
                                    animationSpec = androidx.compose.animation.core.tween(
                                        durationMillis = 220,
                                        easing = androidx.compose.animation.core.FastOutLinearInEasing
                                    )
                                ) {
                                    dragTranslationYState.floatValue = this.value
                                }
                                dragTranslationYState.floatValue = 0f
                            }
                            onStageChange(QsExpansionStage.COLLAPSED)
                        }
                    } else if (dragAccumulatedY > with(density) { 36.dp.toPx() } || manualDragMorphFraction > 0.25f) {
                        // Swiped DOWN in QQS -> Morph transition expands to FULL_QS without shifting panel!
                        isDraggingMorph = false
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStageChange(QsExpansionStage.FULL_QS)
                        dragTranslationYState.floatValue = 0f
                    } else {
                        // Dragged UP less than 36dp -> Snap back open!
                        isDraggingMorph = false
                        if (disablePanelAnimations) {
                            dragTranslationYState.floatValue = 0f
                        } else {
                            coroutineScope.launch {
                                val anim = androidx.compose.animation.core.Animatable(dragTranslationYState.floatValue)
                                anim.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 400f)) {
                                    dragTranslationYState.floatValue = this.value
                                }
                            }
                        }
                    }
                }
            },
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                dragAccumulatedY += dragAmount

                if (expansionStage == QsExpansionStage.QQS) {
                    if (dragAccumulatedY > 0f) {
                        // Dragging DOWN in QQS: Do NOT translate the panel vertically! Only follow morph transition!
                        dragTranslationYState.floatValue = 0f
                        isDraggingMorph = true
                        manualDragMorphFraction = (dragAccumulatedY / morphDragDistancePx).coerceIn(0f, 1f)
                    } else {
                        // Dragging UP in QQS: Translate panel upward for dismissal glide
                        isDraggingMorph = false
                        manualDragMorphFraction = 0f
                        dragTranslationYState.floatValue = dragAccumulatedY.coerceIn(-maxDragDistancePx, 0f)
                    }
                } else if (expansionStage == QsExpansionStage.FULL_QS) {
                    if (dragAccumulatedY < 0f) {
                        // Dragging UP in FULL_QS: Follow morph transition back to QQS without translation
                        dragTranslationYState.floatValue = 0f
                        isDraggingMorph = true
                        manualDragMorphFraction = (1f - (kotlin.math.abs(dragAccumulatedY) / morphDragDistancePx)).coerceIn(0f, 1f)
                    } else {
                        dragTranslationYState.floatValue = 0f
                        isDraggingMorph = false
                        manualDragMorphFraction = 1f
                    }
                }
            }
        )
    }

    val shadeDragToCloseModifier = Modifier.pointerInput(maxDragDistancePx, expansionStage, isEditingTiles, isLandscape) {
        if (isEditingTiles || isLandscape) return@pointerInput
        var dragAccumulatedY = 0f
        detectVerticalDragGestures(
            onDragStart = {
                dragAccumulatedY = 0f
                dragTranslationYState.floatValue = 0f
                com.example.system.NativeTileManager.instance?.isDraggingShade = true
                com.example.system.CpuPerformanceManager.getInstance(context).onInteractionStart()
            },
            onDragCancel = {
                dragTranslationYState.floatValue = 0f
                com.example.system.NativeTileManager.instance?.isDraggingShade = false
                com.example.system.CpuPerformanceManager.getInstance(context).onInteractionEnd()
            },
            onDragEnd = {
                com.example.system.NativeTileManager.instance?.isDraggingShade = false
                com.example.system.CpuPerformanceManager.getInstance(context).onInteractionEnd()
                val dismissThresholdPx = with(density) { 36.dp.toPx() } // Require dragging up by at least 36dp
                val draggedUpPx = (-dragAccumulatedY).coerceAtLeast(0f)
                val translationUpPx = (-dragTranslationYState.floatValue).coerceAtLeast(0f)
                val isDraggedUp = draggedUpPx >= dismissThresholdPx || translationUpPx >= dismissThresholdPx

                if (expansionStage == QsExpansionStage.FULL_QS || expansionStage == QsExpansionStage.QQS) {
                    if (isDraggedUp) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (expansionStage == QsExpansionStage.FULL_QS) {
                            if (com.example.data.PanelPreferences.isAndroidKitKatStyle(context)) {
                                dragTranslationYState.floatValue = 0f
                                onStageChange(QsExpansionStage.COLLAPSED)
                            } else {
                                // Swiped UP in FULL_QS -> Collapse to QQS
                                dragTranslationYState.floatValue = 0f
                                onStageChange(QsExpansionStage.QQS)
                            }
                        } else {
                            // Swiped UP in QQS -> Trigger dismissal
                            val panelHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                            val startVal = dragTranslationYState.floatValue
                            if (disablePanelAnimations) {
                                dragTranslationYState.floatValue = 0f
                                onStageChange(QsExpansionStage.COLLAPSED)
                            } else {
                                coroutineScope.launch {
                                    val anim = androidx.compose.animation.core.Animatable(startVal)
                                    anim.animateTo(
                                        targetValue = -panelHeightPx,
                                        animationSpec = androidx.compose.animation.core.tween(
                                            durationMillis = 220,
                                            easing = androidx.compose.animation.core.FastOutLinearInEasing
                                        )
                                    ) {
                                        dragTranslationYState.floatValue = this.value
                                    }
                                    dragTranslationYState.floatValue = 0f
                                }
                                onStageChange(QsExpansionStage.COLLAPSED)
                            }
                        }
                    } else {
                        // Dragged less than threshold -> Snap back open!
                        if (disablePanelAnimations) {
                            dragTranslationYState.floatValue = 0f
                        } else {
                            coroutineScope.launch {
                                val anim = androidx.compose.animation.core.Animatable(dragTranslationYState.floatValue)
                                anim.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 400f)) {
                                    dragTranslationYState.floatValue = this.value
                                }
                            }
                        }
                    }
                }
            },
            onVerticalDrag = { change, dragAmount ->
                change.consume()
                dragAccumulatedY += dragAmount
                val panelHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                dragTranslationYState.floatValue = (dragTranslationYState.floatValue + dragAmount).coerceIn(-panelHeightPx, panelHeightPx)
            }
        )
    }

        Box(
            modifier = modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .graphicsLayer {
                        translationY = dragTranslationYState.floatValue
                        clip = false
                        compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                    }
        ) {
            val showCustomHeaderMonetGradient by PanelPreferences.showCustomHeaderMonetGradient.collectAsState()
            val customHeaderMonetGradientHeight by PanelPreferences.customHeaderMonetGradientHeight.collectAsState()
            val showMonetSurfaceBackground by PanelPreferences.showMonetSurfaceBackground.collectAsState()

            if (showMonetSurfaceBackground) {
                val isDark = com.example.ui.theme.LocalIsDarkTheme.current
                val monetSurfaceBg = com.example.ui.theme.getQsMonetSurfaceBackgroundColor(isDark)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(monetSurfaceBg)
                )
            }

            if (showCustomHeaderMonetGradient) {
                val gradientStartColor = com.example.ui.theme.getHeaderGradientElementColor(isDark)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(customHeaderMonetGradientHeight.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    gradientStartColor,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // Background dismissal backdrop for tapping outside panel
            if (expansionStage != QsExpansionStage.COLLAPSED) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                onStageChange(QsExpansionStage.COLLAPSED)
                            })
                        }
                )
            }

            val isAndroidKitKatStyle by PanelPreferences.isAndroidKitKatStyle.collectAsState()
            val isAndroidMarshmallowStyle by PanelPreferences.isAndroidMarshmallowStyle.collectAsState()
            val hiddenTileIds by PanelPreferences.hiddenTileIds.collectAsState()

            val kitKatActiveTiles = remember(tiles, hiddenTileIds) {
                tiles.filter {
                    it.isVisible &&
                    it.id !in hiddenTileIds &&
                    (if (it.id == "sound_profile") "sound_mode" !in hiddenTileIds && "volume" !in hiddenTileIds else true)
                }
            }

            val carrierName = remember(context) {
                try {
                    val tm = context.getSystemService(android.content.Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
                    val name = tm?.networkOperatorName?.trim()
                    val simName = tm?.simOperatorName?.trim()
                    if (!name.isNullOrBlank() && name != "Android") name else if (!simName.isNullOrBlank()) simName else ""
                } catch (_: Exception) { "" }
            }

            if (isAndroidKitKatStyle && !isEditingTiles) {
                val showMonetSurfaceBackground by PanelPreferences.showMonetSurfaceBackground.collectAsState()
                val showCustomHeaderMonetGradient by PanelPreferences.showCustomHeaderMonetGradient.collectAsState()
                val customHeaderMonetGradientHeight by PanelPreferences.customHeaderMonetGradientHeight.collectAsState()
                val qsTilesTertiary by PanelPreferences.getQsTilesColorTertiary(isDark).collectAsState()
                val slidersTertiary by PanelPreferences.getSlidersColorTertiary(isDark).collectAsState()
                val clearAllTertiary by PanelPreferences.getClearAllColorTertiary(isDark).collectAsState()
                val qsPanelBgColor by PanelPreferences.qsPanelBackgroundColor.collectAsState()
                val qsBgColor by PanelPreferences.qsBackgroundColor.collectAsState()
                val disableNotifMonet by PanelPreferences.disableMonetNotifText.collectAsState()

                val mediaSessionsList by com.example.service.MediaSessionManagerHelper.sessionsList.collectAsState()
                val forceShowMediaPreview by PanelPreferences.forceShowMediaPreview.collectAsState()
                val activeMediaSessions = remember(mediaSessionsList, forceShowMediaPreview) {
                    if (mediaSessionsList.isNotEmpty()) {
                        mediaSessionsList
                    } else if (forceShowMediaPreview) {
                        listOf(com.example.model.MediaState.Preview)
                    } else {
                        emptyList()
                    }
                }

                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        KitKatShadeHostView(ctx).apply {
                            this.onTileClick = onTileClick
                            this.onTileLongClick = onTileLongClick
                            this.onBrightnessChange = onBrightnessChange
                            this.onNotificationClick = onNotificationClick
                            this.onNotificationDismiss = onNotificationDismiss
                            this.onNotificationActionClick = onNotificationActionClick
                            this.onDismissAllNotifications = onDismissAllNotifications
                            this.onRequestNotificationAccess = onRequestNotificationAccess
                            this.onOpenNotificationManager = { isNotificationManagerOpen = true }
                            this.onClosePanel = { onStageChange(QsExpansionStage.COLLAPSED) }
                            this.onToggleEditMode = { enterEditMode() }
                        }
                    },
                    update = { view ->
                        view.onTileClick = onTileClick
                        view.onTileLongClick = onTileLongClick
                        view.onBrightnessChange = onBrightnessChange
                        view.onNotificationClick = onNotificationClick
                        view.onNotificationDismiss = onNotificationDismiss
                        view.onNotificationActionClick = onNotificationActionClick
                        view.onDismissAllNotifications = onDismissAllNotifications
                        view.onRequestNotificationAccess = onRequestNotificationAccess
                        view.onOpenNotificationManager = { isNotificationManagerOpen = true }
                        view.onClosePanel = { onStageChange(QsExpansionStage.COLLAPSED) }
                        view.onToggleEditMode = { enterEditMode() }

                        view.updateData(
                            tiles = kitKatActiveTiles,
                            brightness = brightness,
                            notifications = notifications,
                            isNotificationPermissionGranted = isNotificationPermissionGranted,
                            carrierName = carrierName,
                            isDark = isDark,
                            showMonetSurface = showMonetSurfaceBackground,
                            showHeaderGradient = showCustomHeaderMonetGradient,
                            mediaSessions = activeMediaSessions
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (isAndroidKitKatStyle && isEditingTiles) {
                val showMonetSurfaceBackground by PanelPreferences.showMonetSurfaceBackground.collectAsState()
                val qsPanelBgColor by PanelPreferences.qsPanelBackgroundColor.collectAsState()
                KitKatTileEditorScreen(
                    activeTiles = editActiveTiles ?: kitKatActiveTiles,
                    inactiveTiles = editInactiveTiles ?: tiles.filter { !it.isVisible },
                    gridLayout = gridLayout,
                    editScrollState = editScrollState,
                    onDragStateChange = { isTileDragging = it },
                    onTilesUpdated = handleTilesUpdated,
                    onDone = saveAndExitEditMode,
                    carrierName = carrierName,
                    isDark = isDark,
                    showMonetSurface = showMonetSurfaceBackground,
                    customPanelBg = qsPanelBgColor
                )
            } else if (isAndroidMarshmallowStyle && isEditingTiles) {
                val showMonetSurfaceBackground by PanelPreferences.showMonetSurfaceBackground.collectAsState()
                val qsPanelBgColor by PanelPreferences.qsPanelBackgroundColor.collectAsState()
                MarshmallowTileEditorScreen(
                    activeTiles = editActiveTiles ?: kitKatActiveTiles,
                    inactiveTiles = editInactiveTiles ?: tiles.filter { !it.isVisible },
                    gridLayout = gridLayout,
                    editScrollState = editScrollState,
                    onDragStateChange = { isTileDragging = it },
                    onTilesUpdated = handleTilesUpdated,
                    onDone = saveAndExitEditMode,
                    carrierName = carrierName,
                    isDark = isDark,
                    showMonetSurface = showMonetSurfaceBackground,
                    customPanelBg = qsPanelBgColor
                )
            } else {
            val isAndroidOreoStyle by PanelPreferences.isAndroidOreoStyle.collectAsState()
            val isFlatLegacyStyle = isAndroidOreoStyle || isAndroidMarshmallowStyle || isAndroidKitKatStyle

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(
                        horizontal = if (isAndroidOreoStyle) 0.dp else (if (isLandscape) 24.dp else 12.dp),
                        vertical = if (isFlatLegacyStyle) 0.dp else 6.dp
                    )
                    .then(
                        if (isAndroidOreoStyle) Modifier.fillMaxWidth()
                        else Modifier.widthIn(max = if (isLandscape) 920.dp else 640.dp)
                    )
                    .align(Alignment.TopCenter)
            ) {
                val showCustomHeaderImage by PanelPreferences.showCustomHeaderImage.collectAsState()
                val customHeaderImageUri by PanelPreferences.customHeaderImageUri.collectAsState()
                val customHeaderImageHeight by PanelPreferences.customHeaderImageHeight.collectAsState()
                val customHeaderImageOpacity by PanelPreferences.customHeaderImageOpacity.collectAsState()
                val showCustomHeaderMonetGradient by PanelPreferences.showCustomHeaderMonetGradient.collectAsState()
                val customHeaderMonetGradientHeight by PanelPreferences.customHeaderMonetGradientHeight.collectAsState()
                val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
                val effectiveOuterCorner = panelOuterCorner.dp

                if (!isFlatLegacyStyle) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                when {
                                    showCustomHeaderImage && customHeaderImageUri.isNotBlank() -> {
                                        Modifier
                                             .height(customHeaderImageHeight.dp)
                                             .clip(RoundedCornerShape(effectiveOuterCorner))
                                             .background(com.example.ui.theme.getQsPanelBackgroundColor(isDark))
                                    }
                                    else -> {
                                        Modifier.wrapContentHeight()
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (showCustomHeaderImage && customHeaderImageUri.isNotBlank()) {
                            coil.compose.AsyncImage(
                                model = customHeaderImageUri,
                                contentDescription = "Custom Header Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(alpha = customHeaderImageOpacity),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }

                        // Header Bar (Clock, Date, System Settings, Tile Studio, Battery)
                        Box(modifier = Modifier.fillMaxWidth().then(shadeDragModifier)) {
                            QsHeaderBar(
                                currentStage = expansionStage,
                                isEditingTiles = isEditingTiles,
                                onHeaderClick = {
                                    if (isEditingTiles) {
                                        saveAndExitEditMode()
                                    } else {
                                        when (expansionStage) {
                                            QsExpansionStage.COLLAPSED -> onStageChange(QsExpansionStage.QQS)
                                            QsExpansionStage.QQS -> onStageChange(QsExpansionStage.FULL_QS)
                                            QsExpansionStage.FULL_QS -> onStageChange(QsExpansionStage.QQS)
                                        }
                                    }
                                },
                                onTimeClick = {
                                    launchClockApp(context)
                                    onStageChange(QsExpansionStage.COLLAPSED)
                                },
                                onDateClick = {
                                    launchCalendarApp(context)
                                    com.example.service.QuickPanelAccessibilityService.hideCustomPanel()
                                    onStageChange(QsExpansionStage.COLLAPSED)
                                },
                                onSettingsClick = {
                                    try {
                                        val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                    com.example.service.QuickPanelAccessibilityService.hideCustomPanel()
                                    onStageChange(QsExpansionStage.COLLAPSED)
                                },
                                onToggleEditMode = {
                                    if (isEditingTiles) {
                                        saveAndExitEditMode()
                                    } else {
                                        enterEditMode()
                                        onStageChange(QsExpansionStage.FULL_QS)
                                    }
                                },
                                unreadNotificationCount = notifications.size
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Expansion Views
                when (expansionStage) {
                    QsExpansionStage.COLLAPSED -> {
                        // Minimal subtle drag pill without text labels
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(shadeDragModifier)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onStageChange(QsExpansionStage.QQS) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 40.dp, height = 4.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.35f))
                            )
                        }
                    }

                    QsExpansionStage.QQS, QsExpansionStage.FULL_QS -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .then(
                                    if (isEditingTiles) {
                                        Modifier.verticalScroll(
                                            state = editScrollState,
                                            enabled = isEditingTiles && !isTileDragging
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            // Quick Settings Panel (QQS / FULL QS)
                            UnifiedQsPanel(
                                expansionStage = expansionStage,
                                isEditingTiles = isEditingTiles,
                                editScrollState = editScrollState,
                                isTileDragging = isTileDragging,
                                onTileDragStateChange = { isTileDragging = it },
                                onToggleEditMode = {
                                    if (isEditingTiles) {
                                        saveAndExitEditMode()
                                    } else {
                                        enterEditMode()
                                        onStageChange(QsExpansionStage.FULL_QS)
                                    }
                                },
                                tiles = tiles,
                                editActiveTiles = editActiveTiles,
                                editInactiveTiles = editInactiveTiles,
                                gridLayout = gridLayout,
                                brightness = brightness,
                                onBrightnessChange = onBrightnessChange,
                                onTileClick = onTileClick,
                                onTileLongClick = { tile ->
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (tile.id == "screenrecord") {
                                        isScreenRecordConfigOpen = true
                                    }
                                    onTileLongClick(tile)
                                },
                                onExpandFull = { onStageChange(QsExpansionStage.FULL_QS) },
                                onCollapseToQqs = { onStageChange(QsExpansionStage.QQS) },
                                onClosePanel = { onStageChange(QsExpansionStage.COLLAPSED) },
                                notifications = notifications,
                                isNotificationPermissionGranted = isNotificationPermissionGranted,
                                onNotificationClick = { item ->
                                    onNotificationClick(item)
                                },
                                onNotificationDismiss = onNotificationDismiss,
                                onNotificationActionClick = { action ->
                                    onNotificationActionClick(action)
                                },
                                onDismissAllNotifications = onDismissAllNotifications,
                                onRequestNotificationAccess = onRequestNotificationAccess,
                                onNavigateToNotificationHub = onNavigateToNotificationHub,
                                onOpenNotificationManager = { isNotificationManagerOpen = true },
                                onTilesReordered = handleTilesUpdated,
                                dragMorphFraction = manualDragMorphFraction,
                                isDraggingMorph = isDraggingMorph,
                                shadeDragModifier = shadeDragModifier,
                                shadeDragToCloseModifier = shadeDragToCloseModifier
                            )
                        }
                    }
                }
            }
            }

            if (isScreenRecordConfigOpen) {
                com.example.ui.customizer.ScreenRecordBottomSheet(
                    onDismissRequest = { isScreenRecordConfigOpen = false }
                )
            }

            if (isNotificationManagerOpen) {
                com.example.ui.customizer.NotificationManagerBottomSheet(
                    onDismissRequest = { isNotificationManagerOpen = false }
                )
            }

            val activeMicroWindow by com.example.system.ComplicationDataHub.activeMicroWindow.collectAsState()
            activeMicroWindow?.let { microType ->
                com.example.ui.tile.ComplicationMicroWindowSheet(
                    type = microType,
                    onDismissRequest = { com.example.system.ComplicationDataHub.closeMicroWindow() }
                )
            }

            // Invisible bottom-left detection square for volume key / bottom-left touch dismiss
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(64.dp)
                    .testTag("invisible_bottom_left_volume_detector")
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                onStageChange(QsExpansionStage.COLLAPSED)
                            }
                        )
                    }
            )
        }
}

fun launchClockApp(context: Context) {
    val intents = listOf(
        Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS),
        Intent(android.provider.AlarmClock.ACTION_SET_ALARM),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage("com.google.android.deskclock"),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage("com.sec.android.app.clockpackage"),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage("com.android.deskclock"),
        Intent(android.provider.Settings.ACTION_DATE_SETTINGS)
    )
    for (intent in intents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        } catch (e: Exception) {}
    }
}

fun launchCalendarApp(context: Context) {
    val uri = android.net.Uri.parse("content://com.android.calendar/time/${System.currentTimeMillis()}")
    val intents = listOf(
        Intent(Intent.ACTION_VIEW, uri),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage("com.google.android.calendar"),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage("com.samsung.android.calendar"),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage("com.android.calendar")
    )
    for (intent in intents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        } catch (e: Exception) {}
    }
}

data class QqsBatteryInfo(val percentage: Int, val isCharging: Boolean)

@Composable
fun rememberQqsBatteryState(): QqsBatteryInfo {
    val context = LocalContext.current
    var batteryInfo by remember {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val initialPct = try {
            bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        } catch (e: Exception) {
            100
        }
        mutableStateOf(QqsBatteryInfo(percentage = initialPct.coerceIn(0, 100), isCharging = false))
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (com.example.system.NativeTileManager.instance?.isDraggingShade == true) return
                if (intent != null) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                    val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
                    batteryInfo = QqsBatteryInfo(percentage = pct.coerceIn(0, 100), isCharging = isCharging)
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        try {
            val sticky = context.registerReceiver(receiver, filter)
            if (sticky != null) {
                val level = sticky.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = sticky.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = sticky.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                val pct = if (level >= 0 && scale > 0) (level * 100 / scale) else 100
                batteryInfo = QqsBatteryInfo(percentage = pct.coerceIn(0, 100), isCharging = isCharging)
            }
        } catch (e: Exception) {}

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {}
        }
    }
    return batteryInfo
}

@Composable
fun QsPageIndicator(
    pageCount: Int,
    scrollPosition: Float,
    style: QsPageIndicatorStyle,
    activeColor: Color,
    inactiveColor: Color,
    onPageClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    activeLineThickness: Float = 4.0f,
    inactiveLineThickness: Float = 3.0f,
    activeLineWidth: Float = 22.0f,
    inactiveLineWidth: Float = 12.0f,
    activeDotRadius: Float = 3.5f,
    inactiveDotRadius: Float = 2.5f,
    activeSlidingThickness: Float = 4.0f,
    inactiveSlidingThickness: Float = 4.0f
) {
    if (pageCount <= 1) return
    val clampedScroll = scrollPosition.coerceIn(0f, (pageCount - 1).toFloat())

    when (style) {
        QsPageIndicatorStyle.DOTS -> {
            val activeDiam = (activeDotRadius * 2f).dp
            val inactiveDiam = (inactiveDotRadius * 2f).dp
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
            ) {
                repeat(pageCount) { pageIdx ->
                    val positionDiff = abs(pageIdx - clampedScroll)
                    val progress = (1f - positionDiff).coerceIn(0f, 1f)
                    val dotSize = lerp(inactiveDiam, activeDiam, progress)
                    val dotColor = androidx.compose.ui.graphics.lerp(inactiveColor, activeColor, progress)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.5.dp)
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(dotColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onPageClick(pageIdx) }
                    )
                }
            }
        }
        QsPageIndicatorStyle.HORIZONTAL_LINES -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
            ) {
                repeat(pageCount) { pageIdx ->
                    val positionDiff = abs(pageIdx - clampedScroll)
                    val progress = (1f - positionDiff).coerceIn(0f, 1f)
                    val lineWidth = lerp(inactiveLineWidth.dp, activeLineWidth.dp, progress)
                    val lineHeight = lerp(inactiveLineThickness.dp, activeLineThickness.dp, progress)
                    val lineColor = androidx.compose.ui.graphics.lerp(inactiveColor, activeColor, progress)
                    Box(
                        modifier = Modifier
                            .width(lineWidth)
                            .height(lineHeight)
                            .clip(CircleShape)
                            .background(lineColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onPageClick(pageIdx) }
                    )
                }
            }
        }
        QsPageIndicatorStyle.DOTS_WITH_ACTIVE_LINE -> {
            val inactiveDiam = (inactiveDotRadius * 2f).dp
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
            ) {
                repeat(pageCount) { pageIdx ->
                    val positionDiff = abs(pageIdx - clampedScroll)
                    val progress = (1f - positionDiff).coerceIn(0f, 1f)
                    val itemWidth = lerp(inactiveDiam, activeLineWidth.dp, progress)
                    val itemHeight = lerp(inactiveDiam, activeLineThickness.dp, progress)
                    val itemColor = androidx.compose.ui.graphics.lerp(inactiveColor, activeColor, progress)
                    Box(
                        modifier = Modifier
                            .width(itemWidth)
                            .height(itemHeight)
                            .clip(CircleShape)
                            .background(itemColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onPageClick(pageIdx) }
                    )
                }
            }
        }
        QsPageIndicatorStyle.SLIDING_TRACK -> {
            val thumbWidth = if (pageCount <= 4) 22.dp else (88.dp / pageCount).coerceAtLeast(12.dp)
            val trackWidth = thumbWidth * pageCount
            val containerHeight = maxOf(activeSlidingThickness, inactiveSlidingThickness, 14f).dp
            Box(
                modifier = modifier
                    .width(trackWidth)
                    .height(containerHeight),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .width(trackWidth)
                        .height(inactiveSlidingThickness.dp)
                        .clip(CircleShape)
                        .background(inactiveColor)
                )
                Box(
                    modifier = Modifier
                        .offset(x = thumbWidth * clampedScroll)
                        .width(thumbWidth)
                        .height(activeSlidingThickness.dp)
                        .clip(CircleShape)
                        .background(activeColor)
                )
                Row(modifier = Modifier.fillMaxSize()) {
                    repeat(pageCount) { pageIdx ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onPageClick(pageIdx) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OreoTopStatusBar(
    carrierName: String,
    batteryPercent: Int,
    isBatteryCharging: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDark) Color.LightGray else Color(0xFF555555)
    val batteryPercentageLocation by PanelPreferences.batteryPercentageLocation.collectAsState()
    val showBatteryPercent = batteryPercentageLocation != com.example.data.BatteryPercentageLocation.HIDDEN
    val batteryBgEnabled by PanelPreferences.batteryBackgroundEnabled.collectAsState()
    val batteryBgCorner by PanelPreferences.batteryBackgroundCorner.collectAsState()
    val clockBgEnabled by PanelPreferences.clockBackgroundEnabled.collectAsState()
    val clockBgCorner by PanelPreferences.clockBackgroundCorner.collectAsState()
    val carrierBgEnabled by PanelPreferences.carrierBackgroundEnabled.collectAsState()
    val carrierBgCorner by PanelPreferences.carrierBackgroundCorner.collectAsState()
    val badgeBgColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
    val badgeContentColor = if (badgeBgColor.luminance() > 0.5f) Color(0xFF1F1F1F) else Color.White

    val currentTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(carrierBgCorner.dp))
                .then(
                    if (carrierBgEnabled) {
                        Modifier
                            .background(badgeBgColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = carrierName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (carrierBgEnabled) badgeContentColor else textColor
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (showBatteryPercent) {
                val batteryTextColor = if (batteryBgEnabled) badgeContentColor else textColor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(batteryBgCorner.dp))
                        .then(
                            if (batteryBgEnabled) {
                                Modifier
                                    .background(badgeBgColor)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            } else Modifier
                        )
                ) {
                    Text(
                        text = "$batteryPercent %",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = batteryTextColor
                    )
                    Icon(
                        imageVector = if (isBatteryCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = batteryTextColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(clockBgCorner.dp))
                    .then(
                        if (clockBgEnabled) {
                            Modifier
                                .background(badgeBgColor)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentTime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (clockBgEnabled) badgeContentColor else textColor
                )
            }
        }
    }
}

/**
 * Top Header Bar (Clean Clock, Stage Switcher & System Actions)
 */
@Composable
fun QsHeaderBar(
    currentStage: QsExpansionStage,
    isEditingTiles: Boolean = false,
    onHeaderClick: () -> Unit = {},
    onTimeClick: () -> Unit = onHeaderClick,
    onDateClick: () -> Unit = onHeaderClick,
    onSettingsClick: () -> Unit = {},
    onToggleEditMode: () -> Unit = {},
    unreadNotificationCount: Int = 0
) {
    val isDark = LocalIsDarkTheme.current
    val editButtonPos by PanelPreferences.editButtonPosition.collectAsState()
    val showDateInQqs by PanelPreferences.showDateInQqsHeader.collectAsState()
    val datePosState by PanelPreferences.datePosition.collectAsState()
    val showMonetSurfaceBackground by PanelPreferences.showMonetSurfaceBackground.collectAsState()
    val showCustomHeaderImage by PanelPreferences.showCustomHeaderImage.collectAsState()
    val customHeaderImageUri by PanelPreferences.customHeaderImageUri.collectAsState()

    val isHeaderImageEnabled = showCustomHeaderImage && customHeaderImageUri.isNotBlank()
    val useBlackHeaderText = showMonetSurfaceBackground && !isDark

    val headerTextColor = if (useBlackHeaderText) Color.Black else Color.White
    val headerSecondaryTextColor = if (useBlackHeaderText) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f)
    val headerIconTint = if (useBlackHeaderText) Color.Black.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)

    val hideDateInExpanded by PanelPreferences.hideDateInExpanded.collectAsState()
    val batteryInfo = rememberQqsBatteryState()

    val dateBgEnabled by PanelPreferences.dateBackgroundEnabled.collectAsState()
    val dateBgCorner by PanelPreferences.dateBackgroundCorner.collectAsState()
    val batteryBgEnabled by PanelPreferences.batteryBackgroundEnabled.collectAsState()
    val batteryBgCorner by PanelPreferences.batteryBackgroundCorner.collectAsState()
    val clockBgEnabled by PanelPreferences.clockBackgroundEnabled.collectAsState()
    val clockBgCorner by PanelPreferences.clockBackgroundCorner.collectAsState()
    val badgeBgColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
    val badgeContentColor = if (badgeBgColor.luminance() > 0.5f) Color(0xFF1F1F1F) else Color.White

    val effectiveDatePos = remember(datePosState, showDateInQqs, hideDateInExpanded, currentStage) {
        if (hideDateInExpanded && currentStage == QsExpansionStage.FULL_QS) {
            com.example.data.DatePosition.HIDDEN
        } else if (showDateInQqs && datePosState == com.example.data.DatePosition.BELOW_TIME) {
            com.example.data.DatePosition.NEXT_TO_TIME
        } else {
            datePosState
        }
    }

    val currentTime = remember {
        val sdf = SimpleDateFormat("h:mm", Locale.getDefault())
        sdf.format(Date())
    }
    val currentDate = remember {
        val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
        sdf.format(Date())
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(if (clockBgEnabled || dateBgEnabled) 4.dp else 0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(clockBgCorner.dp))
                        .then(
                            if (clockBgEnabled) {
                                Modifier
                                    .background(badgeBgColor)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            } else Modifier
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = if (clockBgEnabled) ripple(bounded = true) else null,
                            onClick = onTimeClick
                        )
                        .testTag("qqs_header_time"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentTime,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (clockBgEnabled) badgeContentColor else headerTextColor
                    )
                }
                if (effectiveDatePos == com.example.data.DatePosition.NEXT_TO_TIME) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(dateBgCorner.dp))
                            .then(
                                if (dateBgEnabled) {
                                    Modifier
                                        .background(badgeBgColor)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                } else Modifier
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = if (dateBgEnabled) ripple(bounded = true) else null,
                                onClick = onDateClick
                            )
                            .testTag("qqs_header_date_inline"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (clockBgEnabled || dateBgEnabled) currentDate else "•  $currentDate",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (dateBgEnabled) badgeContentColor else headerTextColor
                        )
                    }
                }
            }

            if (effectiveDatePos == com.example.data.DatePosition.BELOW_TIME) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(dateBgCorner.dp))
                            .then(
                                if (dateBgEnabled) {
                                    Modifier
                                        .background(badgeBgColor)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                } else Modifier
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = if (dateBgEnabled) ripple(bounded = true) else null,
                                onClick = onDateClick
                            )
                            .testTag("qqs_header_date_below"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentDate,
                            fontSize = 12.sp,
                            color = if (dateBgEnabled) badgeContentColor else headerTextColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (effectiveDatePos == com.example.data.DatePosition.HEADER_RIGHT) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dateBgCorner.dp))
                        .then(
                            if (dateBgEnabled) {
                                Modifier
                                    .background(badgeBgColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            } else {
                                Modifier.padding(end = 4.dp)
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = if (dateBgEnabled) ripple(bounded = true) else null,
                            onClick = onDateClick
                        )
                        .testTag("qqs_header_date_right"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentDate,
                        fontSize = 13.sp,
                        color = if (dateBgEnabled) badgeContentColor else headerSecondaryTextColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            val batteryLoc by PanelPreferences.batteryPercentageLocation.collectAsState()
            val settingsLoc by PanelPreferences.settingsIconLocation.collectAsState()

            // Battery Indicator in QQS Header
            if (batteryLoc == com.example.data.BatteryPercentageLocation.HEADER) {
                val resolvedBatteryColor = if (batteryBgEnabled) badgeContentColor else headerTextColor
                val chargingTint = if (batteryBgEnabled && badgeBgColor.luminance() > 0.5f) Color(0xFF2E7D32) else Color(0xFF4CAF50)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(batteryBgCorner.dp))
                        .then(
                            if (batteryBgEnabled) {
                                Modifier
                                    .background(badgeBgColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            } else {
                                Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            }
                        )
                        .testTag("qqs_header_battery_indicator")
                ) {
                    Icon(
                        imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = "Battery ${batteryInfo.percentage}%",
                        tint = if (batteryInfo.isCharging) chargingTint else if (batteryInfo.percentage <= 15) Color(0xFFFF5252) else resolvedBatteryColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${batteryInfo.percentage}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = resolvedBatteryColor
                    )
                }
            }

            if (!isEditingTiles) {
                if (editButtonPos == EditButtonPosition.HEADER || editButtonPos == EditButtonPosition.BOTH) {
                    IconButton(
                        onClick = onToggleEditMode,
                        modifier = Modifier.size(36.dp).testTag("header_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Tiles",
                            tint = headerIconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (settingsLoc == com.example.data.SettingsIconLocation.HEADER) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(36.dp).testTag("header_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "System Settings",
                        tint = headerIconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KitKatEditTileCard(
    tile: QsTileModel,
    tileWidth: Dp? = null,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
    val showMonetSurface by PanelPreferences.showMonetSurfaceBackground.collectAsState()
    val tileAccent = com.example.ui.theme.getQsTilesElementColor(isDark)
    val cardBg = if (showMonetSurface) {
        Color(MonetThemeHelper.getCardBackground(context, isDark))
    } else {
        if (isDark) Color(0xFF1E1E1E) else Color(MonetThemeHelper.getCardBackground(context, isDark))
    }
    val textPrimary = Color(MonetThemeHelper.getTextPrimary(context, isDark))
    val textSecondary = Color(MonetThemeHelper.getTextSecondary(context, isDark))

    val enableLegacyTileShapes by PanelPreferences.enableLegacyTileShapes.collectAsState()
    val tileOutlineEnabled by PanelPreferences.tileOutlineEnabled.collectAsState()
    val tileOutlineThicknessDp by PanelPreferences.tileOutlineThicknessDp.collectAsState()
    val enabledTileShapeId by PanelPreferences.enabledTileShapeId.collectAsState()
    val disabledTileShapeId by PanelPreferences.disabledTileShapeId.collectAsState()
    val chosenShapeId = if (isActive) enabledTileShapeId else disabledTileShapeId

    val inactiveIconColor = if (isDark) Color(0xFF999999) else textSecondary
    val inactiveTextColor = if (isDark) Color(0xFFAAAAAA) else textSecondary

    val iconTint = if (enableLegacyTileShapes) {
        if (isActive) {
            if (tileOutlineEnabled) tileAccent else Color.White
        } else {
            inactiveIconColor
        }
    } else {
        if (isActive) tileAccent else inactiveIconColor
    }

    val labelCol = if (isActive) (if (isDark) Color.White else textPrimary) else inactiveTextColor
    val subCol = if (isActive) tileAccent else textSecondary

    val baseModifier = if (tileWidth != null) {
        modifier.width(tileWidth).aspectRatio(1f)
    } else {
        modifier.aspectRatio(1f)
    }

    val cardShape = RoundedCornerShape(panelOuterCorner.dp)

    Box(
        modifier = baseModifier
            .clip(cardShape)
            .background(cardBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            val isThirdParty = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.category == TileCategory.EXTERNAL || tile.id.contains("/")
            val compSpec = tile.complicationSpec
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                if (compSpec != null) {
                    com.example.ui.tile.ComplicationTileView(
                        model = compSpec.copy(
                            layoutMode = com.example.model.ComplicationLayoutMode.HEADER_ROW,
                            sublinePlacement = com.example.model.ComplicationSublinePlacement.INSIDE_RING
                        ),
                        showGridCardContainer = false,
                        isInteractive = false,
                        showTitle = false,
                        showBelowSubline = false,
                        customRingSize = 44.dp
                    )
                } else if (enableLegacyTileShapes) {
                    val tileShape = com.example.ui.shape.QsShapeManager.getComposeShape(chosenShapeId)
                    val inactiveBgColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
                    val oreoOutlineColor = if (isActive) tileAccent else inactiveBgColor
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .then(
                                if (tileOutlineEnabled) {
                                    Modifier.insideShapeBorder(tileOutlineThicknessDp.dp, oreoOutlineColor, tileShape)
                                } else Modifier
                            )
                            .clip(tileShape)
                            .background(if (tileOutlineEnabled) Color.Transparent else if (isActive) tileAccent else inactiveBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isThirdParty) {
                            val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                            val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                            ExternalTileIcon(
                                packageName = pkg,
                                serviceClassName = cls,
                                tintColor = iconTint,
                                contentDescription = tile.label,
                                fallbackDrawable = tile.externalDrawable,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            val vector = com.example.ui.tile.QsTileIconHelper.getTileImageVector(tile)
                            Icon(
                                imageVector = vector,
                                contentDescription = tile.label,
                                tint = iconTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                } else {
                    if (isThirdParty) {
                        val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                        val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                        ExternalTileIcon(
                            packageName = pkg,
                            serviceClassName = cls,
                            tintColor = iconTint,
                            contentDescription = tile.label,
                            fallbackDrawable = tile.externalDrawable,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        val vector = com.example.ui.tile.QsTileIconHelper.getTileImageVector(tile)
                        Icon(
                            imageVector = vector,
                            contentDescription = tile.label,
                            tint = iconTint,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tile.label.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = labelCol,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            if (tile.subtitle.isNotBlank() && tile.subtitle != tile.label) {
                Text(
                    text = tile.subtitle.uppercase(),
                    fontSize = 10.sp,
                    color = subCol,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(y = (-1).dp)
                )
            }
        }
    }
}

@Composable
fun KitKatTileEditorScreen(
    activeTiles: List<QsTileModel>,
    inactiveTiles: List<QsTileModel>,
    gridLayout: QsGridLayout,
    editScrollState: ScrollState,
    onDragStateChange: (Boolean) -> Unit = {},
    onTilesUpdated: (List<QsTileModel>, List<QsTileModel>) -> Unit,
    onDone: () -> Unit,
    carrierName: String,
    isDark: Boolean,
    showMonetSurface: Boolean,
    customPanelBg: Int? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var localActive by remember(activeTiles) { mutableStateOf(activeTiles) }
    var localInactive by remember(inactiveTiles) { mutableStateOf(inactiveTiles) }

    var draggedTileId by remember { mutableStateOf<String?>(null) }
    var dragSourceIndex by remember { mutableIntStateOf(-1) }
    var hoveredTargetIndex by remember { mutableIntStateOf(-1) }
    var currentDragCenter by remember { mutableStateOf(Offset.Zero) }

    val slotBoundsMap = remember { mutableStateMapOf<Int, Rect>() }
    val tileBoundsMap = remember { mutableStateMapOf<String, Rect>() }
    var activeGridContainerBounds by remember { mutableStateOf<Rect?>(null) }
    var inactiveDropZoneBounds by remember { mutableStateOf<Rect?>(null) }
    var studioCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
    val holoBlue = Color(0xFF33B5E5)
    val textPrimary = Color(MonetThemeHelper.getTextPrimary(context, isDark))
    val textSecondary = Color(MonetThemeHelper.getTextSecondary(context, isDark))
    val panelSurfaceColor = when {
        customPanelBg != null -> Color(customPanelBg)
        showMonetSurface -> Color(MonetThemeHelper.getSurfaceBackground(context, isDark))
        else -> if (isDark) Color.Black else Color(MonetThemeHelper.getSurfaceBackground(context, isDark))
    }

    val columns = gridLayout.columns.coerceAtLeast(1)

    val isOverInactiveDropZone = draggedTileId != null &&
        inactiveDropZoneBounds != null &&
        activeGridContainerBounds != null &&
        currentDragCenter.y > activeGridContainerBounds!!.bottom + 10f &&
        currentDragCenter.y >= inactiveDropZoneBounds!!.top

    val inactiveQsTiles = remember(localInactive) { localInactive.filter { !it.isComplicationTile } }
    val inactiveComplications = remember(localInactive) { localInactive.filter { it.isComplicationTile } }

    val handleAddTileToActive: (QsTileModel, Int?) -> Unit = { targetTile, slotIndex ->
        val updatedInactive = localInactive.toMutableList()
        val moved = updatedInactive.firstOrNull { it.id == targetTile.id }
        if (moved != null) {
            updatedInactive.remove(moved)
            val isExt = moved.isExternal || moved.category == TileCategory.THIRD_PARTY || moved.category == TileCategory.EXTERNAL || moved.id.contains("/")
            val pkg = moved.packageName ?: if (moved.id.contains("/")) moved.id.substringBefore('/') else null
            val cls = moved.serviceClassName ?: if (moved.id.contains("/")) moved.id.substringAfter('/') else null
            val compId = if (isExt && pkg != null && cls != null) "$pkg/$cls" else moved.id

            val activated = moved.copy(
                id = compId,
                isVisible = true,
                isExternal = isExt,
                category = if (isExt) TileCategory.THIRD_PARTY else moved.category,
                packageName = pkg,
                serviceClassName = cls
            )
            val updatedActive = localActive.toMutableList().apply {
                if (slotIndex != null) {
                    add(slotIndex.coerceIn(0, size), activated)
                } else {
                    add(activated)
                }
            }
            localActive = updatedActive
            localInactive = updatedInactive
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onTilesUpdated(localActive, localInactive)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(panelSurfaceColor)
            .onGloballyPositioned { coords ->
                studioCoords = coords
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. KitKat Header Bar (Matching shade_layout_kitkat.xml)
            val now = remember { java.util.Date() }
            val timeFormatter = remember { java.text.SimpleDateFormat("H:mm", java.util.Locale.getDefault()) }
            val dateFormatter = remember { java.text.SimpleDateFormat("EEE, MMMM d", java.util.Locale.getDefault()) }
            val clockBgEnabled by PanelPreferences.clockBackgroundEnabled.collectAsState()
            val clockBgCorner by PanelPreferences.clockBackgroundCorner.collectAsState()
            val dateBgEnabled by PanelPreferences.dateBackgroundEnabled.collectAsState()
            val dateBgCorner by PanelPreferences.dateBackgroundCorner.collectAsState()
            val carrierBgEnabled by PanelPreferences.carrierBackgroundEnabled.collectAsState()
            val carrierBgCorner by PanelPreferences.carrierBackgroundCorner.collectAsState()
            val badgeBgColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
            val badgeContentColor = if (badgeBgColor.luminance() > 0.5f) Color(0xFF1F1F1F) else Color.White

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Bold digital clock + uppercase date
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(clockBgCorner.dp))
                                .then(
                                    if (clockBgEnabled) {
                                        Modifier
                                            .background(badgeBgColor)
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = timeFormatter.format(now),
                                fontSize = 38.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                                fontWeight = FontWeight.Light,
                                color = if (clockBgEnabled) badgeContentColor else if (isDark) Color.White else textPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .clip(RoundedCornerShape(dateBgCorner.dp))
                                .then(
                                    if (dateBgEnabled) {
                                        Modifier
                                            .background(badgeBgColor)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dateFormatter.format(now).uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dateBgEnabled) badgeContentColor else if (isDark) Color(0xFFB0BEC5) else textSecondary
                            )
                        }
                    }

                    // Right: Action button - Holo "DONE"
                    Surface(
                        onClick = onDone,
                        shape = RoundedCornerShape(panelOuterCorner.dp),
                        color = holoBlue,
                        modifier = Modifier.testTag("kitkat_edit_done_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "DONE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Centered Carrier / Status text
                val resolvedCarrier = if (carrierName.isNotBlank()) carrierName.uppercase() else "EMERGENCY CALLS ONLY"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(carrierBgCorner.dp))
                            .then(
                                if (carrierBgEnabled) {
                                    Modifier
                                        .background(badgeBgColor)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = resolvedCarrier,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = if (carrierBgEnabled) badgeContentColor else if (isDark) Color(0xFF888888) else textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 2. Holo Blue Accent Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(holoBlue)
            )

            // 3. Scrollable Studio (Separated Active & Available QS Tiles vs Complications)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(panelSurfaceColor)
                    .verticalScroll(editScrollState)
                    .padding(horizontal = 1.5.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Active Section Container (Unified Active Tiles)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            activeGridContainerBounds = coords.boundsInRoot()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE TILES (${localActive.size})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = holoBlue
                        )
                        Text(
                            text = "Hold to drag • Tap '-'",
                            fontSize = 10.sp,
                            color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666)
                        )
                    }
                    if (localActive.isEmpty()) {
                        Text(
                            text = "No active tiles. Add one below.",
                            fontSize = 11.5.sp,
                            color = if (isDark) Color.Gray else Color(0xFF777777),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    } else {
                        val groupRows = localActive.chunked(columns)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            groupRows.forEach { rowTiles ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    rowTiles.forEach { tile ->
                                        val slotIndex = localActive.indexOfFirst { it.id == tile.id }
                                        val isDragging = draggedTileId == tile.id
                                        val isHoverTarget = draggedTileId != null && hoveredTargetIndex == slotIndex

                                        key(tile.id) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .onGloballyPositioned { coords ->
                                                        if (slotIndex >= 0) slotBoundsMap[slotIndex] = coords.boundsInRoot()
                                                        tileBoundsMap[tile.id] = coords.boundsInRoot()
                                                    }
                                                    .pointerInput(tile.id) {
                                                        detectDragGesturesAfterLongPress(
                                                            onDragStart = { offset ->
                                                                val idx = localActive.indexOfFirst { it.id == tile.id }
                                                                if (idx < 0) return@detectDragGesturesAfterLongPress
                                                                draggedTileId = tile.id
                                                                dragSourceIndex = idx
                                                                hoveredTargetIndex = -1
                                                                onDragStateChange(true)
                                                                val myBounds = slotBoundsMap[idx] ?: tileBoundsMap[tile.id] ?: Rect.Zero
                                                                currentDragCenter = myBounds.topLeft + offset
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            },
                                                            onDrag = { change, dragAmount ->
                                                                change.consume()
                                                                currentDragCenter += dragAmount

                                                                if (draggedTileId != null) {
                                                                    val activeContainer = activeGridContainerBounds
                                                                    val dropBounds = inactiveDropZoneBounds
                                                                    val isStrictlyAvailable = dropBounds != null &&
                                                                        activeContainer != null &&
                                                                        currentDragCenter.y > activeContainer.bottom + 10f &&
                                                                        currentDragCenter.y >= dropBounds.top

                                                                    if (isStrictlyAvailable) {
                                                                        hoveredTargetIndex = -1
                                                                    } else {
                                                                        var targetIndex = -1
                                                                        val totalCount = localActive.size
                                                                        for (i in 0 until totalCount) {
                                                                            val b = slotBoundsMap[i] ?: continue
                                                                            val expanded = Rect(b.left - 6f, b.top - 6f, b.right + 6f, b.bottom + 6f)
                                                                            if (expanded.contains(currentDragCenter)) {
                                                                                targetIndex = i
                                                                                break
                                                                            }
                                                                        }
                                                                        if (targetIndex != -1 && targetIndex != dragSourceIndex) {
                                                                            if (hoveredTargetIndex != targetIndex) {
                                                                                hoveredTargetIndex = targetIndex
                                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                            }
                                                                        } else {
                                                                            hoveredTargetIndex = -1
                                                                        }
                                                                    }
                                                                }
                                                            },
                                                            onDragEnd = {
                                                                onDragStateChange(false)
                                                                val activeId = draggedTileId
                                                                val dropBounds = inactiveDropZoneBounds
                                                                val activeContainer = activeGridContainerBounds
                                                                val sourceIdx = dragSourceIndex
                                                                val targetIdx = hoveredTargetIndex

                                                                val isDropToAvailable = activeId != null && dropBounds != null &&
                                                                    ((activeContainer != null && currentDragCenter.y > activeContainer.bottom + 10f && currentDragCenter.y >= dropBounds.top) ||
                                                                     (activeContainer == null && currentDragCenter.y >= dropBounds.top))

                                                                if (isDropToAvailable) {
                                                                    val updatedActive = localActive.toMutableList()
                                                                    val moved = updatedActive.firstOrNull { it.id == activeId }
                                                                    if (moved != null) {
                                                                        updatedActive.remove(moved)
                                                                        val updatedInactive = localInactive.toMutableList().apply {
                                                                            removeAll { it.id == moved.id }
                                                                            add(0, moved.copy(isVisible = false))
                                                                        }
                                                                        localActive = updatedActive
                                                                        localInactive = updatedInactive
                                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                    }
                                                                } else if (sourceIdx != -1 && targetIdx != -1 && sourceIdx != targetIdx &&
                                                                    sourceIdx in 0 until localActive.size && targetIdx in 0 until localActive.size
                                                                ) {
                                                                    val updated = localActive.toMutableList()
                                                                    java.util.Collections.swap(updated, sourceIdx, targetIdx)
                                                                    localActive = updated
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                }
                                                                draggedTileId = null
                                                                dragSourceIndex = -1
                                                                hoveredTargetIndex = -1
                                                                onTilesUpdated(localActive, localInactive)
                                                            },
                                                            onDragCancel = {
                                                                onDragStateChange(false)
                                                                draggedTileId = null
                                                                dragSourceIndex = -1
                                                                hoveredTargetIndex = -1
                                                                onTilesUpdated(localActive, localInactive)
                                                            }
                                                        )
                                                    }
                                            ) {
                                                if (isDragging) {
                                                    Spacer(modifier = Modifier.fillMaxSize())
                                                } else {
                                                    Box(modifier = Modifier.fillMaxSize()) {
                                                        if (isHoverTarget) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .clip(RoundedCornerShape(panelOuterCorner.dp))
                                                                    .background(Color(0x3333B5E5))
                                                                    .border(1.5.dp, holoBlue, RoundedCornerShape(panelOuterCorner.dp))
                                                            )
                                                        }
                                                        KitKatEditTileCard(
                                                            tile = tile,
                                                            isActive = true,
                                                            modifier = Modifier.fillMaxSize()
                                                        )

                                                        if (draggedTileId == null) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .align(Alignment.TopEnd)
                                                                    .padding(top = 2.dp, end = 2.dp)
                                                                    .size(20.dp)
                                                                    .clip(RoundedCornerShape(3.dp))
                                                                    .background(Color(0xFFCC0000))
                                                                    .clickable(
                                                                        interactionSource = remember { MutableInteractionSource() },
                                                                        indication = null
                                                                    ) {
                                                                        val updatedActive = localActive.toMutableList()
                                                                        val moved = updatedActive.firstOrNull { it.id == tile.id }
                                                                        if (moved != null) {
                                                                            updatedActive.remove(moved)
                                                                            val updatedInactive = localInactive.toMutableList().apply {
                                                                                removeAll { it.id == moved.id }
                                                                                add(0, moved.copy(isVisible = false))
                                                                            }
                                                                            localActive = updatedActive
                                                                            localInactive = updatedInactive
                                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                            onTilesUpdated(localActive, localInactive)
                                                                        }
                                                                    },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Remove,
                                                                    contentDescription = "Remove tile",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(13.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    repeat(columns - rowTiles.size) {
                                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Available QS Tiles & Complications
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            inactiveDropZoneBounds = coords.boundsInRoot()
                        }
                        .clip(RoundedCornerShape(panelOuterCorner.dp))
                        .background(
                            if (isOverInactiveDropZone) Color(0x3333B5E5)
                            else if (isDark) Color(0xFF141414)
                            else Color(0xFFECEFF1)
                        )
                        .border(
                            width = if (isOverInactiveDropZone) 2.dp else 1.dp,
                            color = if (isOverInactiveDropZone) holoBlue else (if (isDark) Color(0xFF262626) else Color(0xFFCFD8DC)),
                            shape = RoundedCornerShape(panelOuterCorner.dp)
                        )
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val renderAvailableGroup: @Composable (String, List<QsTileModel>, String) -> Unit = { titleText, availTiles, emptyMsg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isOverInactiveDropZone) "DROP TO REMOVE FROM PANEL" else titleText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = holoBlue
                            )
                            Text(
                                text = if (isOverInactiveDropZone) "Release to drop" else "Tap '+' or drag up",
                                fontSize = 10.sp,
                                color = if (isDark) Color.Gray else Color(0xFF666666)
                            )
                        }

                        if (availTiles.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emptyMsg,
                                    fontSize = 12.sp,
                                    color = if (isDark) Color.Gray else Color(0xFF888888)
                                )
                            }
                        } else {
                            val inactiveRows = availTiles.chunked(columns)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                inactiveRows.forEach { rowTiles ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        rowTiles.forEach { tile ->
                                            val isThisDragging = draggedTileId == tile.id
                                            key(tile.id) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
                                                        .onGloballyPositioned { coords ->
                                                            if (draggedTileId != tile.id) {
                                                                tileBoundsMap[tile.id] = coords.boundsInRoot()
                                                            }
                                                        }
                                                        .pointerInput(tile.id) {
                                                            detectDragGesturesAfterLongPress(
                                                                onDragStart = { offset ->
                                                                    draggedTileId = tile.id
                                                                    onDragStateChange(true)
                                                                    val myBounds = tileBoundsMap[tile.id] ?: Rect.Zero
                                                                    currentDragCenter = myBounds.topLeft + offset
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                },
                                                                onDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    currentDragCenter += dragAmount
                                                                },
                                                                onDragEnd = {
                                                                    onDragStateChange(false)
                                                                    val dropBounds = inactiveDropZoneBounds
                                                                    val activeContainer = activeGridContainerBounds

                                                                    val isDraggedToActive = (activeContainer != null && currentDragCenter.y <= activeContainer.bottom + 10f) ||
                                                                        (dropBounds != null && currentDragCenter.y < dropBounds.top)

                                                                    if (isDraggedToActive) {
                                                                        var targetSlot: Int? = null
                                                                        for (i in 0 until localActive.size) {
                                                                            val b = slotBoundsMap[i] ?: continue
                                                                            if (b.contains(currentDragCenter)) {
                                                                                targetSlot = i
                                                                                break
                                                                            }
                                                                        }
                                                                        handleAddTileToActive(tile, targetSlot)
                                                                    }
                                                                    draggedTileId = null
                                                                },
                                                                onDragCancel = {
                                                                    onDragStateChange(false)
                                                                    draggedTileId = null
                                                                }
                                                            )
                                                        }
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = null
                                                        ) {
                                                            handleAddTileToActive(tile, null)
                                                        }
                                                ) {
                                                    if (isThisDragging) {
                                                        Spacer(modifier = Modifier.fillMaxSize())
                                                    } else {
                                                        Box(modifier = Modifier.fillMaxSize()) {
                                                            KitKatEditTileCard(
                                                                tile = tile,
                                                                isActive = false,
                                                                modifier = Modifier.fillMaxSize()
                                                            )

                                                            Box(
                                                                modifier = Modifier
                                                                    .align(Alignment.TopEnd)
                                                                    .padding(top = 2.dp, end = 2.dp)
                                                                    .size(20.dp)
                                                                    .clip(RoundedCornerShape(3.dp))
                                                                    .background(holoBlue)
                                                                    .clickable(
                                                                        interactionSource = remember { MutableInteractionSource() },
                                                                        indication = null
                                                                    ) { handleAddTileToActive(tile, null) },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Add,
                                                                    contentDescription = "Add tile",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(13.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        repeat(columns - rowTiles.size) {
                                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    renderAvailableGroup(
                        "AVAILABLE QS TILES (${inactiveQsTiles.size})",
                        inactiveQsTiles,
                        "All QS tiles are currently in your active panel"
                    )
                    renderAvailableGroup(
                        "COMPLICATIONS (${inactiveComplications.size})",
                        inactiveComplications,
                        "All complications are currently in your active panel"
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        // Floating Drag Overlay for Dragged Tile
        if (draggedTileId != null) {
            val draggedTile = localActive.firstOrNull { it.id == draggedTileId }
                ?: localInactive.firstOrNull { it.id == draggedTileId }
            val currentStudio = studioCoords
            if (draggedTile != null && currentStudio != null && currentStudio.isAttached) {
                val studioOrigin = currentStudio.boundsInRoot().topLeft
                val localX = currentDragCenter.x - studioOrigin.x
                val localY = currentDragCenter.y - studioOrigin.y

                val studioWidthPx = currentStudio.boundsInRoot().width
                val gapPx = with(density) { 3.dp.toPx() }
                val tileWidthPx = (studioWidthPx - (gapPx * (columns - 1))) / columns
                val tileWidthDp = with(density) { tileWidthPx.toDp() }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (localX - tileWidthPx / 2f).roundToInt(),
                                (localY - tileWidthPx / 2f).roundToInt()
                            )
                        }
                        .size(tileWidthDp)
                        .zIndex(9999f)
                        .graphicsLayer {
                            scaleX = 1.05f
                            scaleY = 1.05f
                            alpha = 0.95f
                            shadowElevation = 12f
                        }
                ) {
                    KitKatEditTileCard(
                        tile = draggedTile,
                        isActive = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun MarshmallowTileEditorScreen(
    activeTiles: List<QsTileModel>,
    inactiveTiles: List<QsTileModel>,
    gridLayout: QsGridLayout,
    editScrollState: ScrollState,
    onDragStateChange: (Boolean) -> Unit = {},
    onTilesUpdated: (List<QsTileModel>, List<QsTileModel>) -> Unit,
    onDone: () -> Unit,
    carrierName: String,
    isDark: Boolean,
    showMonetSurface: Boolean,
    customPanelBg: Int? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var localActive by remember(activeTiles) { mutableStateOf(activeTiles) }
    var localInactive by remember(inactiveTiles) { mutableStateOf(inactiveTiles) }

    var draggedTileId by remember { mutableStateOf<String?>(null) }
    var dragSourceIndex by remember { mutableIntStateOf(-1) }
    var hoveredTargetIndex by remember { mutableIntStateOf(-1) }
    var currentDragCenter by remember { mutableStateOf(Offset.Zero) }
    var autoScrollDirection by remember { mutableIntStateOf(0) }

    val slotBoundsMap = remember { mutableStateMapOf<Int, Rect>() }
    val tileBoundsMap = remember { mutableStateMapOf<String, Rect>() }
    var activeGridContainerBounds by remember { mutableStateOf<Rect?>(null) }
    var inactiveDropZoneBounds by remember { mutableStateOf<Rect?>(null) }
    var studioViewportBounds by remember { mutableStateOf<Rect?>(null) }
    var studioCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
    val effectiveCorner = panelOuterCorner.dp

    val monetAccent = com.example.ui.theme.getQsTilesElementColor(isDark)
    val baseSurfaceBg = MonetThemeHelper.getSurfaceBackground(context, isDark)

    val isPanelContainerCustomized = customPanelBg != null ||
        PanelPreferences.getPanelContainerUseCustom(isDark).collectAsState().value ||
        PanelPreferences.getPanelContainerColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.DEFAULT ||
        PanelPreferences.getPanelContainerHue(isDark).collectAsState().value != 0f ||
        PanelPreferences.getPanelContainerSat(isDark).collectAsState().value != 1f ||
        PanelPreferences.getPanelContainerVal(isDark).collectAsState().value != 1f ||
        PanelPreferences.showMonetSurfaceBackground.collectAsState().value

    val isHeaderGradientCustomized = PanelPreferences.getHeaderGradientUseCustom(isDark).collectAsState().value ||
        PanelPreferences.getHeaderGradientColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.PRIMARY ||
        PanelPreferences.getHeaderGradientHue(isDark).collectAsState().value != 0f ||
        PanelPreferences.getHeaderGradientSat(isDark).collectAsState().value != 1f ||
        PanelPreferences.getHeaderGradientVal(isDark).collectAsState().value != 1f

    val isMiscCustomized = PanelPreferences.getMiscElementsUseCustom(isDark).collectAsState().value ||
        PanelPreferences.getMiscElementsColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.PRIMARY ||
        PanelPreferences.getMiscElementsHue(isDark).collectAsState().value != 0f ||
        PanelPreferences.getMiscElementsSat(isDark).collectAsState().value != 1f ||
        PanelPreferences.getMiscElementsVal(isDark).collectAsState().value != 1f

    val panelElementColor = com.example.ui.theme.getQsPanelBackgroundColor(isDark)
    val headerGradientElementColor = com.example.ui.theme.getHeaderGradientElementColor(isDark)
    val miscElementColor = com.example.ui.theme.getMiscElementColor(isDark)

    val bodyBg = when {
        customPanelBg != null -> Color(customPanelBg)
        isPanelContainerCustomized -> panelElementColor
        isDark -> Color(MonetThemeHelper.getCardBackground(context, isDark))
        else -> {
            val r = (android.graphics.Color.red(baseSurfaceBg) * 0.98f).toInt().coerceIn(0, 255)
            val g = (android.graphics.Color.green(baseSurfaceBg) * 0.98f).toInt().coerceIn(0, 255)
            val b = (android.graphics.Color.blue(baseSurfaceBg) * 0.98f).toInt().coerceIn(0, 255)
            Color(android.graphics.Color.argb(255, r, g, b))
        }
    }

    val darkHeaderBg = when {
        customPanelBg != null -> Color(customPanelBg)
        isHeaderGradientCustomized -> headerGradientElementColor
        isPanelContainerCustomized -> {
            val factor = 0.88f
            Color(
                red = (bodyBg.red * factor).coerceIn(0f, 1f),
                green = (bodyBg.green * factor).coerceIn(0f, 1f),
                blue = (bodyBg.blue * factor).coerceIn(0f, 1f),
                alpha = 1.0f
            )
        }
        isDark -> Color(MonetThemeHelper.brightenColor(baseSurfaceBg, true))
        else -> {
            val r = (android.graphics.Color.red(baseSurfaceBg) * 0.90f).toInt().coerceIn(0, 255)
            val g = (android.graphics.Color.green(baseSurfaceBg) * 0.90f).toInt().coerceIn(0, 255)
            val b = (android.graphics.Color.blue(baseSurfaceBg) * 0.90f).toInt().coerceIn(0, 255)
            Color(android.graphics.Color.argb(255, r, g, b))
        }
    }

    val headerTextColor = Color(MonetThemeHelper.getTextPrimary(context, isDark))
    val textLightSecondary = Color(MonetThemeHelper.getTextSecondary(context, isDark))
    val headerActionTint = if (isMiscCustomized) miscElementColor else headerTextColor

    val columns = gridLayout.columns.coerceAtLeast(1)

    // Lock/unlock parent scroll while dragging
    LaunchedEffect(draggedTileId) {
        onDragStateChange(draggedTileId != null)
    }

    // Edge auto-scroll
    LaunchedEffect(draggedTileId, autoScrollDirection) {
        if (draggedTileId != null && autoScrollDirection != 0) {
            while (isActive && draggedTileId != null && autoScrollDirection != 0) {
                val canScrollUp = autoScrollDirection < 0 && editScrollState.value > 0
                val canScrollDown = autoScrollDirection > 0 && editScrollState.value < editScrollState.maxValue
                if (!canScrollUp && !canScrollDown) {
                    autoScrollDirection = 0
                    break
                }
                val scrollDelta = with(density) {
                    if (autoScrollDirection < 0) -9.dp.toPx() else 9.dp.toPx()
                }
                editScrollState.scrollBy(scrollDelta)
                delay(16)
            }
        }
    }

    val checkAutoScroll: (Offset) -> Unit = { center ->
        val vBounds = studioViewportBounds
        if (vBounds != null && vBounds.height > 0) {
            val edgeZoneHeight = vBounds.height * 0.12f
            val topThreshold = vBounds.top + edgeZoneHeight
            val bottomThreshold = vBounds.bottom - edgeZoneHeight
            if (center.y <= topThreshold && editScrollState.value > 0) {
                autoScrollDirection = -1
            } else if (center.y >= bottomThreshold && editScrollState.value < editScrollState.maxValue) {
                autoScrollDirection = 1
            } else {
                autoScrollDirection = 0
            }
        } else {
            autoScrollDirection = 0
        }
    }

    val isOverInactiveDropZone = draggedTileId != null &&
        inactiveDropZoneBounds != null &&
        activeGridContainerBounds != null &&
        currentDragCenter.y > activeGridContainerBounds!!.bottom + 10f &&
        currentDragCenter.y >= inactiveDropZoneBounds!!.top

    val inactiveQsTiles = remember(localInactive) { localInactive.filter { !it.isComplicationTile } }
    val inactiveComplications = remember(localInactive) { localInactive.filter { it.isComplicationTile } }

    val handleAddTileToActive: (QsTileModel, Int?) -> Unit = { targetTile, slotIndex ->
        val updatedInactive = localInactive.toMutableList()
        val moved = updatedInactive.firstOrNull { it.id == targetTile.id }
        if (moved != null) {
            updatedInactive.remove(moved)
            val isExt = moved.isExternal || moved.category == TileCategory.THIRD_PARTY || moved.category == TileCategory.EXTERNAL || moved.id.contains("/")
            val pkg = moved.packageName ?: if (moved.id.contains("/")) moved.id.substringBefore('/') else null
            val cls = moved.serviceClassName ?: if (moved.id.contains("/")) moved.id.substringAfter('/') else null
            val compId = if (isExt && pkg != null && cls != null) "$pkg/$cls" else moved.id

            val activated = moved.copy(
                id = compId,
                isVisible = true,
                isExternal = isExt,
                category = if (isExt) TileCategory.THIRD_PARTY else moved.category,
                packageName = pkg,
                serviceClassName = cls
            )
            val updatedActive = localActive.toMutableList().apply {
                if (slotIndex != null) {
                    add(slotIndex.coerceIn(0, size), activated)
                } else {
                    add(activated)
                }
            }
            localActive = updatedActive
            localInactive = updatedInactive
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onTilesUpdated(localActive, localInactive)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                studioCoords = coords
                studioViewportBounds = coords.boundsInRoot()
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Marshmallow Header Bar (Dark Slate #263238)
            val now = remember { java.util.Date() }
            val timeFormatter = remember { java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()) }
            val dateFormatter = remember { java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.getDefault()) }
            val batteryInfo = rememberQqsBatteryState()
            val dateBgEnabled by PanelPreferences.dateBackgroundEnabled.collectAsState()
            val dateBgCorner by PanelPreferences.dateBackgroundCorner.collectAsState()
            val batteryBgEnabled by PanelPreferences.batteryBackgroundEnabled.collectAsState()
            val batteryBgCorner by PanelPreferences.batteryBackgroundCorner.collectAsState()
            val clockBgEnabled by PanelPreferences.clockBackgroundEnabled.collectAsState()
            val clockBgCorner by PanelPreferences.clockBackgroundCorner.collectAsState()
            val carrierBgEnabled by PanelPreferences.carrierBackgroundEnabled.collectAsState()
            val carrierBgCorner by PanelPreferences.carrierBackgroundCorner.collectAsState()
            val badgeBgColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
            val badgeContentColor = if (badgeBgColor.luminance() > 0.5f) Color(0xFF1F1F1F) else Color.White

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(darkHeaderBg)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
            ) {
                // Top status bar row: Carrier on Left, Battery + Settings + Profile on Right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Carrier name
                    val resolvedCarrierColor = if (carrierBgEnabled) badgeContentColor else textLightSecondary
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(carrierBgCorner.dp))
                            .then(
                                if (carrierBgEnabled) {
                                    Modifier
                                        .background(badgeBgColor)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                } else Modifier
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = null,
                            tint = resolvedCarrierColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = carrierName.ifBlank { "Emergency calls only" },
                            fontSize = 13.sp,
                            color = resolvedCarrierColor
                        )
                    }

                    // Right: Battery % + icon + Settings + User profile
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val resolvedBatteryColor = if (batteryBgEnabled) badgeContentColor else headerTextColor
                        val batteryChargingTint = if (batteryBgEnabled && badgeBgColor.luminance() > 0.5f) Color(0xFF2E7D32) else Color(0xFF4CAF50)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(batteryBgCorner.dp))
                                .then(
                                    if (batteryBgEnabled) {
                                        Modifier
                                            .background(badgeBgColor)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    } else Modifier
                                )
                        ) {
                            Text(
                                text = "${batteryInfo.percentage}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = resolvedBatteryColor
                            )
                            Icon(
                                imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                contentDescription = "Battery",
                                tint = if (batteryInfo.isCharging) batteryChargingTint else resolvedBatteryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = headerActionTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_qs_user),
                            contentDescription = "User Avatar",
                            tint = headerTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Time & Date
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(clockBgCorner.dp))
                        .then(
                            if (clockBgEnabled) {
                                Modifier
                                    .background(badgeBgColor)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            } else Modifier
                        )
                        .clickable { launchClockApp(context) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timeFormatter.format(now),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (clockBgEnabled) badgeContentColor else headerTextColor
                    )
                }
                Spacer(modifier = Modifier.height(if (clockBgEnabled || dateBgEnabled) 4.dp else 2.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dateBgCorner.dp))
                        .then(
                            if (dateBgEnabled) {
                                Modifier
                                    .background(badgeBgColor)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            } else Modifier
                        )
                        .clickable { launchCalendarApp(context) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dateFormatter.format(now),
                        fontSize = 13.sp,
                        color = if (dateBgEnabled) badgeContentColor else textLightSecondary
                    )
                }

                // Subheader & Done Button Row
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = monetAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "EDIT QUICK TILES",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = monetAccent
                            )
                            Text(
                                text = "Drag tiles to reorder. Tap '-' to remove.",
                                fontSize = 10.5.sp,
                                color = textLightSecondary
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            onTilesUpdated(localActive, localInactive)
                            onDone()
                        },
                        shape = RoundedCornerShape(effectiveCorner),
                        color = monetAccent,
                        modifier = Modifier.testTag("marshmallow_edit_done_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "DONE",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 2. Main QS Body (Solid Slate #37474F)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = effectiveCorner,
                            bottomEnd = effectiveCorner
                        )
                    )
                    .background(bodyBg)
                    .verticalScroll(editScrollState)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Active Section Container (Unified Active Tiles)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            activeGridContainerBounds = coords.boundsInRoot()
                        }
                ) {
                    val renderActiveGroup: @Composable (String, List<QsTileModel>, String) -> Unit = { headerTitle, groupTiles, emptyMsg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = headerTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = monetAccent
                            )
                            Text(
                                text = "Hold to drag • Tap '-'",
                                fontSize = 10.sp,
                                color = textLightSecondary
                            )
                        }
                        if (groupTiles.isEmpty()) {
                            Text(
                                text = emptyMsg,
                                fontSize = 11.5.sp,
                                color = textLightSecondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            val groupRows = groupTiles.chunked(columns)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                groupRows.forEach { rowTiles ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowTiles.forEach { tile ->
                                            val slotIndex = localActive.indexOfFirst { it.id == tile.id }
                                            val isDragging = draggedTileId == tile.id
                                            val isHoverTarget = draggedTileId != null && hoveredTargetIndex == slotIndex

                                            key(tile.id) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(86.dp)
                                                        .onGloballyPositioned { coords ->
                                                            if (slotIndex >= 0) slotBoundsMap[slotIndex] = coords.boundsInRoot()
                                                            tileBoundsMap[tile.id] = coords.boundsInRoot()
                                                        }
                                                        .pointerInput(tile.id) {
                                                            detectDragGesturesAfterLongPress(
                                                                onDragStart = { offset ->
                                                                    val idx = localActive.indexOfFirst { it.id == tile.id }
                                                                    if (idx < 0) return@detectDragGesturesAfterLongPress
                                                                    draggedTileId = tile.id
                                                                    dragSourceIndex = idx
                                                                    hoveredTargetIndex = -1
                                                                    onDragStateChange(true)
                                                                    val myBounds = slotBoundsMap[idx] ?: tileBoundsMap[tile.id] ?: Rect.Zero
                                                                    currentDragCenter = myBounds.topLeft + offset
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                },
                                                                onDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    currentDragCenter += dragAmount
                                                                    checkAutoScroll(currentDragCenter)

                                                                    if (draggedTileId != null) {
                                                                        val activeContainer = activeGridContainerBounds
                                                                        val dropBounds = inactiveDropZoneBounds
                                                                        val isStrictlyAvailable = dropBounds != null &&
                                                                            activeContainer != null &&
                                                                            currentDragCenter.y > activeContainer.bottom + 10f &&
                                                                            currentDragCenter.y >= dropBounds.top

                                                                        if (isStrictlyAvailable) {
                                                                            hoveredTargetIndex = -1
                                                                        } else {
                                                                            var targetIndex = -1
                                                                            val totalCount = localActive.size
                                                                            for (i in 0 until totalCount) {
                                                                                val b = slotBoundsMap[i] ?: continue
                                                                                val expanded = Rect(b.left - 6f, b.top - 6f, b.right + 6f, b.bottom + 6f)
                                                                                if (expanded.contains(currentDragCenter)) {
                                                                                    targetIndex = i
                                                                                    break
                                                                                }
                                                                            }
                                                                            if (targetIndex != -1 && targetIndex != dragSourceIndex) {
                                                                                if (hoveredTargetIndex != targetIndex) {
                                                                                    hoveredTargetIndex = targetIndex
                                                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                                }
                                                                            } else {
                                                                                hoveredTargetIndex = -1
                                                                            }
                                                                        }
                                                                    }
                                                                },
                                                                onDragEnd = {
                                                                    autoScrollDirection = 0
                                                                    onDragStateChange(false)
                                                                    val activeId = draggedTileId
                                                                    val dropBounds = inactiveDropZoneBounds
                                                                    val activeContainer = activeGridContainerBounds
                                                                    val sourceIdx = dragSourceIndex
                                                                    val targetIdx = hoveredTargetIndex

                                                                    val isDropToAvailable = activeId != null && dropBounds != null &&
                                                                        ((activeContainer != null && currentDragCenter.y > activeContainer.bottom + 10f && currentDragCenter.y >= dropBounds.top) ||
                                                                         (activeContainer == null && currentDragCenter.y >= dropBounds.top))

                                                                    if (isDropToAvailable) {
                                                                        val updatedActive = localActive.toMutableList()
                                                                        val moved = updatedActive.firstOrNull { it.id == activeId }
                                                                        if (moved != null) {
                                                                            updatedActive.remove(moved)
                                                                            val updatedInactive = localInactive.toMutableList().apply {
                                                                                removeAll { it.id == moved.id }
                                                                                add(0, moved.copy(isVisible = false))
                                                                            }
                                                                            localActive = updatedActive
                                                                            localInactive = updatedInactive
                                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                        }
                                                                    } else if (sourceIdx != -1 && targetIdx != -1 && sourceIdx != targetIdx &&
                                                                        sourceIdx in 0 until localActive.size && targetIdx in 0 until localActive.size
                                                                    ) {
                                                                        val updated = localActive.toMutableList()
                                                                        java.util.Collections.swap(updated, sourceIdx, targetIdx)
                                                                        localActive = updated
                                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                    }
                                                                    draggedTileId = null
                                                                    dragSourceIndex = -1
                                                                    hoveredTargetIndex = -1
                                                                    onTilesUpdated(localActive, localInactive)
                                                                },
                                                                onDragCancel = {
                                                                    autoScrollDirection = 0
                                                                    onDragStateChange(false)
                                                                    draggedTileId = null
                                                                    dragSourceIndex = -1
                                                                    hoveredTargetIndex = -1
                                                                    onTilesUpdated(localActive, localInactive)
                                                                }
                                                            )
                                                        }
                                                ) {
                                                    if (isDragging) {
                                                        Spacer(modifier = Modifier.fillMaxSize())
                                                    } else {
                                                        Box(modifier = Modifier.fillMaxSize()) {
                                                            if (isHoverTarget) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxSize()
                                                                        .clip(RoundedCornerShape(effectiveCorner))
                                                                        .background(monetAccent.copy(alpha = 0.2f))
                                                                        .border(1.5.dp, monetAccent, RoundedCornerShape(effectiveCorner))
                                                                )
                                                            }
                                                            MarshmallowEditTileCard(
                                                                tile = tile,
                                                                tileWidth = Dp.Unspecified,
                                                                isActive = true,
                                                                modifier = Modifier.fillMaxSize()
                                                            )

                                                            // Top-right subtle minus remove button
                                                            if (draggedTileId == null) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .align(Alignment.TopEnd)
                                                                        .padding(top = 3.dp, end = 3.dp)
                                                                        .size(20.dp)
                                                                        .clip(CircleShape)
                                                                        .background(Color(0xFFE53935))
                                                                        .clickable(
                                                                            interactionSource = remember { MutableInteractionSource() },
                                                                            indication = null
                                                                        ) {
                                                                            val updatedActive = localActive.toMutableList()
                                                                            val moved = updatedActive.firstOrNull { it.id == tile.id }
                                                                            if (moved != null) {
                                                                                updatedActive.remove(moved)
                                                                                val updatedInactive = localInactive.toMutableList().apply {
                                                                                    removeAll { it.id == moved.id }
                                                                                    add(0, moved.copy(isVisible = false))
                                                                                }
                                                                                localActive = updatedActive
                                                                                localInactive = updatedInactive
                                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                                onTilesUpdated(localActive, localInactive)
                                                                            }
                                                                        },
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Remove,
                                                                        contentDescription = "Remove tile",
                                                                        tint = Color.White,
                                                                        modifier = Modifier.size(13.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        repeat(columns - rowTiles.size) {
                                            Spacer(modifier = Modifier.weight(1f).height(86.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    renderActiveGroup(
                        "ACTIVE TILES (${localActive.size})",
                        localActive,
                        "No active tiles. Add one below."
                    )
                }

                // Available Tiles & Complications Section
                val availableBg = if (isOverInactiveDropZone) monetAccent.copy(alpha = 0.2f)
                else Color(MonetThemeHelper.getInactiveTilesColor(context, isDark))
                val availableBorder = if (isOverInactiveDropZone) monetAccent
                else Color(MonetThemeHelper.getCardOutline(context, isDark)).copy(alpha = 0.5f)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            inactiveDropZoneBounds = coords.boundsInRoot()
                        }
                        .clip(RoundedCornerShape(effectiveCorner))
                        .background(availableBg)
                        .border(
                            width = if (isOverInactiveDropZone) 2.dp else 1.dp,
                            color = availableBorder,
                            shape = RoundedCornerShape(effectiveCorner)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val renderAvailableGroup: @Composable (String, List<QsTileModel>, String, Boolean) -> Unit = { titleText, availTiles, emptyMsg, isCompGroup ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isCompGroup) Icons.Filled.AutoAwesome else Icons.Default.Tune,
                                contentDescription = null,
                                tint = monetAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isOverInactiveDropZone) {
                                    "DROP TO REMOVE FROM ACTIVE PANEL"
                                } else {
                                    titleText
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = monetAccent
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = if (isOverInactiveDropZone) "Release to drop" else "Tap '+' or drag up",
                                fontSize = 10.sp,
                                color = textLightSecondary
                            )
                        }

                        if (availTiles.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emptyMsg,
                                    fontSize = 12.sp,
                                    color = textLightSecondary
                                )
                            }
                        } else {
                            val inactiveRows = availTiles.chunked(columns)

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                inactiveRows.forEach { rowTiles ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowTiles.forEach { tile ->
                                            val isThisDragging = draggedTileId == tile.id
                                            key(tile.id) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(86.dp)
                                                        .onGloballyPositioned { coords ->
                                                            if (draggedTileId != tile.id) {
                                                                tileBoundsMap[tile.id] = coords.boundsInRoot()
                                                            }
                                                        }
                                                        .pointerInput(tile.id) {
                                                            detectDragGesturesAfterLongPress(
                                                                onDragStart = { offset ->
                                                                    draggedTileId = tile.id
                                                                    onDragStateChange(true)
                                                                    val myBounds = tileBoundsMap[tile.id] ?: Rect.Zero
                                                                    currentDragCenter = myBounds.topLeft + offset
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                },
                                                                onDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    currentDragCenter += dragAmount
                                                                },
                                                                onDragEnd = {
                                                                    onDragStateChange(false)
                                                                    val dropBounds = inactiveDropZoneBounds
                                                                    val activeContainer = activeGridContainerBounds

                                                                    val isDraggedToActive = (activeContainer != null && currentDragCenter.y <= activeContainer.bottom + 10f) ||
                                                                        (dropBounds != null && currentDragCenter.y < dropBounds.top)

                                                                    if (isDraggedToActive) {
                                                                        var targetSlot: Int? = null
                                                                        for (i in 0 until localActive.size) {
                                                                            val b = slotBoundsMap[i] ?: continue
                                                                            if (b.contains(currentDragCenter)) {
                                                                                targetSlot = i
                                                                                break
                                                                            }
                                                                        }
                                                                        handleAddTileToActive(tile, targetSlot)
                                                                    }
                                                                    draggedTileId = null
                                                                },
                                                                onDragCancel = {
                                                                    onDragStateChange(false)
                                                                    draggedTileId = null
                                                                }
                                                            )
                                                        }
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = null
                                                        ) {
                                                            handleAddTileToActive(tile, null)
                                                        }
                                                ) {
                                                    if (isThisDragging) {
                                                        Spacer(modifier = Modifier.fillMaxSize())
                                                    } else {
                                                        Box(modifier = Modifier.fillMaxSize()) {
                                                            MarshmallowEditTileCard(
                                                                tile = tile,
                                                                tileWidth = Dp.Unspecified,
                                                                isActive = false,
                                                                modifier = Modifier.fillMaxSize()
                                                            )

                                                            // Plus add button
                                                            Box(
                                                                modifier = Modifier
                                                                    .align(Alignment.TopEnd)
                                                                    .padding(top = 3.dp, end = 3.dp)
                                                                    .size(20.dp)
                                                                    .clip(CircleShape)
                                                                    .background(monetAccent)
                                                                    .clickable(
                                                                        interactionSource = remember { MutableInteractionSource() },
                                                                        indication = null
                                                                    ) { handleAddTileToActive(tile, null) },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Add,
                                                                    contentDescription = "Add tile",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(13.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        repeat(columns - rowTiles.size) {
                                            Spacer(modifier = Modifier.weight(1f).height(86.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    renderAvailableGroup(
                        "AVAILABLE QS TILES (${inactiveQsTiles.size})",
                        inactiveQsTiles,
                        "All QS tiles are currently in your active panel",
                        false
                    )
                    renderAvailableGroup(
                        "COMPLICATIONS (${inactiveComplications.size})",
                        inactiveComplications,
                        "All complications are currently in your active panel",
                        true
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        // Floating Drag Overlay for Dragged Tile
        if (draggedTileId != null) {
            val draggedTile = localActive.firstOrNull { it.id == draggedTileId }
                ?: localInactive.firstOrNull { it.id == draggedTileId }
            val currentStudio = studioCoords
            if (draggedTile != null && currentStudio != null && currentStudio.isAttached) {
                val studioOrigin = currentStudio.boundsInRoot().topLeft
                val localX = currentDragCenter.x - studioOrigin.x
                val localY = currentDragCenter.y - studioOrigin.y

                val studioWidthPx = currentStudio.boundsInRoot().width
                val gapPx = with(density) { 8.dp.toPx() }
                val padPx = with(density) { 28.dp.toPx() }
                val tileWidthPx = (studioWidthPx - padPx - (gapPx * (columns - 1))) / columns
                val tileWidthDp = with(density) { tileWidthPx.toDp() }

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (localX - tileWidthPx / 2f).roundToInt(),
                                (localY - 43.dp.toPx()).roundToInt()
                            )
                        }
                        .size(width = tileWidthDp, height = 86.dp)
                        .zIndex(9999f)
                        .graphicsLayer {
                            scaleX = 1.05f
                            scaleY = 1.05f
                            alpha = 0.95f
                            shadowElevation = 12f
                        }
                ) {
                    MarshmallowEditTileCard(
                        tile = draggedTile,
                        tileWidth = tileWidthDp,
                        isActive = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun MarshmallowEditTileCard(
    tile: QsTileModel,
    tileWidth: Dp = Dp.Unspecified,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
    val enableLegacyTileShapes by PanelPreferences.enableLegacyTileShapes.collectAsState()
    val monetAccent = com.example.ui.theme.getQsTilesElementColor(isDark)
    val cardBg = if (isActive) {
        Color(MonetThemeHelper.getCardBackground(context, isDark))
    } else {
        Color(MonetThemeHelper.getInactiveTilesColor(context, isDark))
    }
    val labelCol = Color(MonetThemeHelper.getTextPrimary(context, isDark))
    val corner = panelOuterCorner.dp

    Box(
        modifier = modifier
            .then(if (tileWidth != Dp.Unspecified) Modifier.width(tileWidth) else Modifier)
            .height(86.dp)
            .clip(RoundedCornerShape(corner))
            .background(cardBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            val isThirdParty = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.category == TileCategory.EXTERNAL || tile.id.contains("/")
            val compSpec = tile.complicationSpec
            val isInactiveTilesCustomized = PanelPreferences.getInactiveTilesUseCustom(isDark).collectAsState().value ||
                PanelPreferences.getInactiveTilesColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.DEFAULT ||
                PanelPreferences.getInactiveTilesHue(isDark).collectAsState().value != 0f ||
                PanelPreferences.getInactiveTilesSat(isDark).collectAsState().value != 1f ||
                PanelPreferences.getInactiveTilesVal(isDark).collectAsState().value != 1f
            val inactiveOnTileColor = com.example.ui.theme.getInactiveTilesOnElementColor(isDark)
            val iconTint = when {
                isActive -> monetAccent
                isInactiveTilesCustomized -> inactiveOnTileColor
                else -> Color(MonetThemeHelper.getTextSecondary(context, isDark))
            }
            if (compSpec != null) {
                com.example.ui.tile.ComplicationTileView(
                    model = compSpec.copy(
                        layoutMode = com.example.model.ComplicationLayoutMode.HEADER_ROW,
                        sublinePlacement = com.example.model.ComplicationSublinePlacement.INSIDE_RING
                    ),
                    showGridCardContainer = false,
                    isInteractive = false,
                    showTitle = false,
                    showBelowSubline = false,
                    showRingBackground = enableLegacyTileShapes,
                    showInsideSubline = true,
                    customRingSize = if (enableLegacyTileShapes) 42.dp else 28.dp,
                    inactiveIconTintOverride = if (!enableLegacyTileShapes && !isActive) inactiveOnTileColor else null
                )
            } else if (isThirdParty) {
                val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                ExternalTileIcon(
                    packageName = pkg,
                    serviceClassName = cls,
                    tintColor = iconTint,
                    contentDescription = tile.label,
                    fallbackDrawable = tile.externalDrawable,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                val vector = com.example.ui.tile.QsTileIconHelper.getTileImageVector(tile)
                Icon(
                    imageVector = vector,
                    contentDescription = tile.label,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tile.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = labelCol,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EditStudioTileView(
    tile: QsTileModel,
    tileWidth: Dp,
    isKitKat: Boolean,
    isMarshmallow: Boolean,
    isActive: Boolean = true
) {
    if (isKitKat) {
        KitKatEditTileCard(
            tile = tile,
            tileWidth = tileWidth,
            isActive = isActive
        )
    } else if (isMarshmallow) {
        MarshmallowEditTileCard(
            tile = tile,
            tileWidth = tileWidth,
            isActive = isActive
        )
    } else {
        CircularQsTile(
            tile = tile,
            morphProgress = 1f,
            tileWidth = tileWidth,
            isInteractive = false,
            onClick = {},
            onLongClick = {}
        )
    }
}

/**
 * Edit Mode Tile Studio: Native drag-and-drop tile reordering, edge-zone auto-scroll, and active/available pools
 */
@Composable
fun EditModeTileStudio(
    activeTiles: List<QsTileModel>,
    inactiveTiles: List<QsTileModel>,
    gridLayout: QsGridLayout,
    editScrollState: ScrollState,
    onDragStateChange: (Boolean) -> Unit = {},
    onTilesUpdated: (List<QsTileModel>, List<QsTileModel>) -> Unit,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val density = LocalDensity.current
    val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
    val enableQqsComplicationsRow by PanelPreferences.enableQqsComplicationsRow.collectAsState()
    val qqs2x1ComplicationIds by PanelPreferences.qqs2x1ComplicationIds.collectAsState()
    val qqs2x1ActiveCornerDp by PanelPreferences.qqs2x1ActiveCornerDp.collectAsState()
    val qqs2x1InactiveCornerDp by PanelPreferences.qqs2x1InactiveCornerDp.collectAsState()
    val qqs2x1TileHeightDp by PanelPreferences.qqs2x1TileHeightDp.collectAsState()

    val isKitKat = PanelPreferences.isAndroidKitKatStyle(context)
    val isMarshmallow = PanelPreferences.isAndroidMarshmallowStyle(context)
    val isLegacyFlat = isKitKat || isMarshmallow
    val show2x1ComplicationEditor = enableQqsComplicationsRow && !isLegacyFlat

    var localActive by remember(activeTiles) { mutableStateOf(activeTiles) }
    var localInactive by remember(inactiveTiles) { mutableStateOf(inactiveTiles) }

    val allEditTileMap = remember(localActive, localInactive) {
        (localActive + localInactive).associateBy { it.id }
    }
    val qqs2x1EditTiles = remember(allEditTileMap, qqs2x1ComplicationIds, show2x1ComplicationEditor) {
        if (!show2x1ComplicationEditor) emptyList()
        else qqs2x1ComplicationIds.mapNotNull { id ->
            allEditTileMap[id]?.takeIf { it.isComplicationTile || it.complicationSpec != null }
        }
    }

    val inactiveQsTiles = remember(localInactive) { localInactive.filter { !it.isComplicationTile } }
    val inactiveComplications = remember(localInactive) { localInactive.filter { it.isComplicationTile } }

    var draggedTileId by remember { mutableStateOf<String?>(null) }
    var dragSourceKind by remember { mutableStateOf<String?>(null) } // "active", "inactive", "comp2x1"
    var dragSourceIndex by remember { mutableIntStateOf(-1) }
    var hoveredTargetIndex by remember { mutableIntStateOf(-1) }
    var hoveredCompSlotIndex by remember { mutableIntStateOf(-1) }
    var currentDragCenter by remember { mutableStateOf(Offset.Zero) }
    var autoScrollDirection by remember { mutableIntStateOf(0) } // -1 = up, 1 = down, 0 = idle

    val slotBoundsMap = remember { mutableStateMapOf<Int, Rect>() }
    val compSlotBoundsMap = remember { mutableStateMapOf<Int, Rect>() }
    val tileBoundsMap = remember { mutableStateMapOf<String, Rect>() }
    var compRowContainerBounds by remember { mutableStateOf<Rect?>(null) }
    var activeGridContainerBounds by remember { mutableStateOf<Rect?>(null) }
    var inactiveDropZoneBounds by remember { mutableStateOf<Rect?>(null) }
    var studioViewportBounds by remember { mutableStateOf<Rect?>(null) }
    var studioCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var compCardWidthDp by remember { mutableStateOf(140.dp) }

    // Lock/unlock parent vertical scroll while dragging to prevent gesture cancellation
    LaunchedEffect(draggedTileId) {
        onDragStateChange(draggedTileId != null)
    }

    // Edge-Zone Auto-Scroll Engine (top 12% / bottom 12% of visible viewport)
    LaunchedEffect(draggedTileId, autoScrollDirection) {
        if (draggedTileId != null && autoScrollDirection != 0) {
            while (isActive && draggedTileId != null && autoScrollDirection != 0) {
                val canScrollUp = autoScrollDirection < 0 && editScrollState.value > 0
                val canScrollDown = autoScrollDirection > 0 && editScrollState.value < editScrollState.maxValue
                if (!canScrollUp && !canScrollDown) {
                    autoScrollDirection = 0
                    break
                }

                val scrollDelta = with(density) {
                    if (autoScrollDirection < 0) -9.dp.toPx() else 9.dp.toPx()
                }
                editScrollState.scrollBy(scrollDelta)
                delay(16)
            }
        }
    }

    val checkAutoScroll: (Offset) -> Unit = { center ->
        val vBounds = studioViewportBounds
        if (vBounds != null && vBounds.height > 0) {
            val edgeZoneHeight = vBounds.height * 0.12f
            val topThreshold = vBounds.top + edgeZoneHeight
            val bottomThreshold = vBounds.bottom - edgeZoneHeight
            if (center.y <= topThreshold && editScrollState.value > 0) {
                autoScrollDirection = -1
            } else if (center.y >= bottomThreshold && editScrollState.value < editScrollState.maxValue) {
                autoScrollDirection = 1
            } else {
                autoScrollDirection = 0
            }
        } else {
            autoScrollDirection = 0
        }
    }

    val targetTileWidth = when (gridLayout.columns) {
        2 -> 130.dp
        3 -> 106.dp
        else -> 74.dp
    }

    val draggedTileObj = remember(draggedTileId, localActive, localInactive) {
        draggedTileId?.let { id ->
            localActive.firstOrNull { it.id == id } ?: localInactive.firstOrNull { it.id == id }
        }
    }
    val canDropInto2x1Row = show2x1ComplicationEditor &&
        draggedTileObj != null &&
        (draggedTileObj.isComplicationTile || draggedTileObj.complicationSpec != null)
    val isOverComp2x1DropZone = canDropInto2x1Row &&
        compRowContainerBounds != null &&
        compRowContainerBounds!!.let { b ->
            Rect(b.left - 12f, b.top - 12f, b.right + 12f, b.bottom + 12f).contains(currentDragCenter)
        }

    val isOverInactiveDropZone = draggedTileId != null &&
        inactiveDropZoneBounds != null &&
        currentDragCenter.y >= (inactiveDropZoneBounds?.top ?: Float.MAX_VALUE)

    val legacyAccent = if (isKitKat) Color(0xFF33B5E5) else if (isMarshmallow) com.example.ui.theme.getQsTilesElementColor(isDark) else androidx.compose.material3.MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                studioViewportBounds = coords.boundsInRoot()
                studioCoords = coords
            }
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Edit Mode Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isKitKat) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "QUICK SETTINGS TILES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Color(0xFF33B5E5)
                        )
                        Text(
                            text = "Touch & hold to reorder. Tap '-' to remove.",
                            fontSize = 10.5.sp,
                            color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF555555)
                        )
                    }
                } else if (isMarshmallow) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = legacyAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(
                                text = "EDIT QUICK TILES",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = legacyAccent
                            )
                            Text(
                                text = "Drag tiles to reorder. Tap '-' to remove.",
                                fontSize = 10.5.sp,
                                color = if (isDark) Color(0xFFB0BEC5) else Color(0xFF546E7A)
                            )
                        }
                    }
                } else {
                    Column {
                        Text(
                            text = "EDIT QUICK SETTINGS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = legacyAccent
                        )
                        Text(
                            text = "Hold & drag to reorder tiles. Tap '-' to remove.",
                            fontSize = 11.sp,
                            color = if (isDark) Color.LightGray else Color(0xFF666666)
                        )
                    }
                }

                Surface(
                    onClick = {
                        onTilesUpdated(localActive, localInactive)
                        onDone()
                    },
                    shape = RoundedCornerShape(if (isKitKat || isMarshmallow) panelOuterCorner.dp else 16.dp),
                    color = if (isKitKat) Color(0xFF33B5E5) else if (isMarshmallow) legacyAccent else legacyAccent,
                    modifier = Modifier.testTag("inline_done_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isKitKat || isMarshmallow) "DONE" else "Done",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            if (isKitKat) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color(0xFF33B5E5))
                )
            }

            // QQS 2x1 Complications Row Section (when enabled in Modern / Oreo styles)
            if (show2x1ComplicationEditor) {
                val compHighlightBorder = if (isOverComp2x1DropZone) legacyAccent else Color.Transparent
                val compContainerBg = if (isOverComp2x1DropZone) {
                    legacyAccent.copy(alpha = 0.12f)
                } else if (isDark) {
                    Color.White.copy(alpha = 0.05f)
                } else {
                    Color.Black.copy(alpha = 0.04f)
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            compRowContainerBounds = coords.boundsInRoot()
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(compContainerBg)
                        .border(
                            width = if (isOverComp2x1DropZone) 1.5.dp else 0.dp,
                            color = compHighlightBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(10.dp)
                        .testTag("edit_studio_qqs_2x1_row")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOverComp2x1DropZone) "DROP TO PLACE IN 2x1 ROW" else "QQS 2x1 COMPLICATIONS (${qqs2x1EditTiles.size})",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = legacyAccent
                        )
                        Text(
                            text = "Hold & drag to reorder/move",
                            fontSize = 10.sp,
                            color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666)
                        )
                    }

                    if (qqs2x1EditTiles.isEmpty()) {
                        Text(
                            text = "Drag any complication tile here for the 2x1 QQS row.",
                            fontSize = 11.5.sp,
                            color = if (isDark) Color.Gray else Color(0xFF777777),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val spacing = 8.dp
                            val computedWidth = if (qqs2x1EditTiles.size == 1) {
                                maxWidth
                            } else {
                                ((maxWidth - spacing) / 2f).coerceAtLeast(124.dp)
                            }
                            compCardWidthDp = computedWidth
                            val compRows = qqs2x1EditTiles.chunked(2)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                compRows.forEach { rowComps ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(spacing)
                                    ) {
                                        rowComps.forEach { tile ->
                                            val compIdx = qqs2x1EditTiles.indexOfFirst { it.id == tile.id }
                                            val isDraggingThis = draggedTileId == tile.id && dragSourceKind == "comp2x1"
                                            val isHoverCompSlot = draggedTileId != null && hoveredCompSlotIndex == compIdx
                                            key("edit_2x1_${tile.id}") {
                                                Box(
                                                    modifier = Modifier
                                                        .width(computedWidth)
                                                        .height(qqs2x1TileHeightDp.dp)
                                                        .onGloballyPositioned { coords ->
                                                            if (compIdx >= 0) compSlotBoundsMap[compIdx] = coords.boundsInRoot()
                                                            tileBoundsMap[tile.id] = coords.boundsInRoot()
                                                        }
                                                        .pointerInput(tile.id, qqs2x1EditTiles.size) {
                                                            detectDragGesturesAfterLongPress(
                                                                onDragStart = { offset ->
                                                                    val idx = qqs2x1EditTiles.indexOfFirst { it.id == tile.id }
                                                                    if (idx < 0) return@detectDragGesturesAfterLongPress
                                                                    draggedTileId = tile.id
                                                                    dragSourceKind = "comp2x1"
                                                                    dragSourceIndex = idx
                                                                    hoveredTargetIndex = -1
                                                                    hoveredCompSlotIndex = -1
                                                                    onDragStateChange(true)
                                                                    val myBounds = compSlotBoundsMap[idx] ?: tileBoundsMap[tile.id] ?: Rect.Zero
                                                                    currentDragCenter = myBounds.topLeft + offset
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                },
                                                                onDrag = { change, dragAmount ->
                                                                    change.consume()
                                                                    currentDragCenter += dragAmount
                                                                    checkAutoScroll(currentDragCenter)

                                                                    if (draggedTileId != null) {
                                                                        val compBounds = compRowContainerBounds
                                                                        val activeContainer = activeGridContainerBounds
                                                                        val dropBounds = inactiveDropZoneBounds

                                                                        val insideCompRow = compBounds != null && Rect(
                                                                            compBounds.left - 12f,
                                                                            compBounds.top - 12f,
                                                                            compBounds.right + 12f,
                                                                            compBounds.bottom + 12f
                                                                        ).contains(currentDragCenter)

                                                                        if (insideCompRow) {
                                                                            hoveredTargetIndex = -1
                                                                            var foundCompIdx = -1
                                                                            for (i in 0 until qqs2x1EditTiles.size) {
                                                                                val b = compSlotBoundsMap[i] ?: continue
                                                                                if (Rect(b.left - 8f, b.top - 8f, b.right + 8f, b.bottom + 8f).contains(currentDragCenter)) {
                                                                                    foundCompIdx = i
                                                                                    break
                                                                                }
                                                                            }
                                                                            if (foundCompIdx != -1 && foundCompIdx != dragSourceIndex) {
                                                                                if (hoveredCompSlotIndex != foundCompIdx) {
                                                                                    hoveredCompSlotIndex = foundCompIdx
                                                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                                }
                                                                            } else {
                                                                                hoveredCompSlotIndex = -1
                                                                            }
                                                                        } else {
                                                                            hoveredCompSlotIndex = -1
                                                                            val isStrictlyAvailable = dropBounds != null &&
                                                                                activeContainer != null &&
                                                                                currentDragCenter.y > activeContainer.bottom + 10f &&
                                                                                currentDragCenter.y >= dropBounds.top
                                                                            if (isStrictlyAvailable) {
                                                                                hoveredTargetIndex = -1
                                                                            } else {
                                                                                var foundGridIdx = -1
                                                                                for (i in 0 until localActive.size) {
                                                                                    val b = slotBoundsMap[i] ?: continue
                                                                                    if (Rect(b.left - 10f, b.top - 10f, b.right + 10f, b.bottom + 10f).contains(currentDragCenter)) {
                                                                                        foundGridIdx = i
                                                                                        break
                                                                                    }
                                                                                }
                                                                                if (foundGridIdx != hoveredTargetIndex) {
                                                                                    hoveredTargetIndex = foundGridIdx
                                                                                    if (foundGridIdx != -1) {
                                                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                },
                                                                onDragEnd = {
                                                                    autoScrollDirection = 0
                                                                    onDragStateChange(false)
                                                                    val activeId = draggedTileId
                                                                    val compBounds = compRowContainerBounds
                                                                    val activeContainer = activeGridContainerBounds
                                                                    val dropBounds = inactiveDropZoneBounds
                                                                    val sourceIdx = dragSourceIndex
                                                                    val targetCompIdx = hoveredCompSlotIndex
                                                                    val targetGridIdx = hoveredTargetIndex

                                                                    val droppedInCompRow = compBounds != null && Rect(
                                                                        compBounds.left - 12f,
                                                                        compBounds.top - 12f,
                                                                        compBounds.right + 12f,
                                                                        compBounds.bottom + 12f
                                                                    ).contains(currentDragCenter)
                                                                    val droppedInAvailable = dropBounds != null &&
                                                                        ((activeContainer != null && currentDragCenter.y > activeContainer.bottom + 10f && currentDragCenter.y >= dropBounds.top) ||
                                                                         (activeContainer == null && currentDragCenter.y >= dropBounds.top))
                                                                    val droppedInActiveGrid = !droppedInCompRow && !droppedInAvailable &&
                                                                        activeContainer != null &&
                                                                        currentDragCenter.y >= activeContainer.top - 12f &&
                                                                        currentDragCenter.y <= activeContainer.bottom + 16f

                                                                    if (activeId != null) {
                                                                        if (droppedInCompRow) {
                                                                            if (sourceIdx != -1 && targetCompIdx != -1 && sourceIdx != targetCompIdx) {
                                                                                PanelPreferences.moveQqs2x1ComplicationId(context, sourceIdx, targetCompIdx)
                                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                            }
                                                                        } else if (droppedInAvailable) {
                                                                            if (qqs2x1EditTiles.size > 1) {
                                                                                PanelPreferences.removeQqs2x1ComplicationId(context, activeId)
                                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                            }
                                                                        } else if (droppedInActiveGrid) {
                                                                            val alreadyActiveIdx = localActive.indexOfFirst { it.id == activeId }
                                                                            if (alreadyActiveIdx == -1) {
                                                                                val tileObj = localInactive.firstOrNull { it.id == activeId }
                                                                                if (tileObj != null) {
                                                                                    val updatedInactive = localInactive.toMutableList().apply { remove(tileObj) }
                                                                                    val updatedActive = localActive.toMutableList().apply {
                                                                                        val insertAt = if (targetGridIdx != -1) targetGridIdx.coerceIn(0, size) else size
                                                                                        add(insertAt, tileObj.copy(isVisible = true))
                                                                                    }
                                                                                    localActive = updatedActive
                                                                                    localInactive = updatedInactive
                                                                                    onTilesUpdated(localActive, localInactive)
                                                                                }
                                                                            } else if (targetGridIdx != -1 && targetGridIdx != alreadyActiveIdx && targetGridIdx in localActive.indices) {
                                                                                val updatedActive = localActive.toMutableList()
                                                                                java.util.Collections.swap(updatedActive, alreadyActiveIdx, targetGridIdx)
                                                                                localActive = updatedActive
                                                                                onTilesUpdated(localActive, localInactive)
                                                                            }
                                                                            if (qqs2x1EditTiles.size > 1) {
                                                                                PanelPreferences.removeQqs2x1ComplicationId(context, activeId)
                                                                            }
                                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                        }
                                                                    }
                                                                    draggedTileId = null
                                                                    dragSourceKind = null
                                                                    dragSourceIndex = -1
                                                                    hoveredTargetIndex = -1
                                                                    hoveredCompSlotIndex = -1
                                                                },
                                                                onDragCancel = {
                                                                    autoScrollDirection = 0
                                                                    onDragStateChange(false)
                                                                    draggedTileId = null
                                                                    dragSourceKind = null
                                                                    dragSourceIndex = -1
                                                                    hoveredTargetIndex = -1
                                                                    hoveredCompSlotIndex = -1
                                                                }
                                                            )
                                                        }
                                                ) {
                                                    val compPlaceholderCorner = if (tile.state != TileState.ACTIVE) {
                                                        qqs2x1InactiveCornerDp.dp
                                                    } else {
                                                        qqs2x1ActiveCornerDp.dp
                                                    }
                                                    if (isDraggingThis) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .clip(RoundedCornerShape(compPlaceholderCorner))
                                                                .background(legacyAccent.copy(alpha = 0.08f))
                                                                .border(1.dp, legacyAccent.copy(alpha = 0.35f), RoundedCornerShape(compPlaceholderCorner))
                                                        )
                                                    } else {
                                                        Box(modifier = Modifier.fillMaxSize()) {
                                                            Qqs2x1ComplicationCard(
                                                                tile = tile,
                                                                isDark = isDark,
                                                                onClick = {},
                                                                onLongClick = {},
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .then(
                                                                        if (isHoverCompSlot) {
                                                                            Modifier.border(2.dp, legacyAccent, RoundedCornerShape(compPlaceholderCorner))
                                                                        } else Modifier
                                                                    )
                                                            )
                                                            if (draggedTileId == null && qqs2x1EditTiles.size > 1) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .align(Alignment.TopEnd)
                                                                        .padding(top = 2.dp, end = 2.dp)
                                                                        .size(20.dp)
                                                                        .clip(CircleShape)
                                                                        .background(Color(0xFFE53935))
                                                                        .clickable(
                                                                            interactionSource = remember { MutableInteractionSource() },
                                                                            indication = null
                                                                        ) {
                                                                            PanelPreferences.removeQqs2x1ComplicationId(context, tile.id)
                                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                        }
                                                                        .testTag("edit_studio_remove_2x1_${tile.id}"),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Remove,
                                                                        contentDescription = "Remove from 2x1 Row",
                                                                        tint = Color.White,
                                                                        modifier = Modifier.size(13.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (rowComps.size == 1 && qqs2x1EditTiles.size > 1) {
                                            Spacer(modifier = Modifier.width(computedWidth))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Active Tiles Grid Section
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        activeGridContainerBounds = coords.boundsInRoot()
                    }
            ) {
                val renderActiveGroup: @Composable (String, List<QsTileModel>, String) -> Unit = { headerTitle, groupTiles, emptyMsg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = headerTitle,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = legacyAccent
                        )
                        Text(
                            text = "Hold to drag • Tap '-'",
                            fontSize = 10.sp,
                            color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF666666)
                        )
                    }
                    if (groupTiles.isEmpty()) {
                        Text(
                            text = emptyMsg,
                            fontSize = 11.5.sp,
                            color = if (isDark) Color.Gray else Color(0xFF777777),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        val activeRows = groupTiles.chunked(gridLayout.columns)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            activeRows.forEach { rowTiles ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    rowTiles.forEach { tile ->
                                        val slotIndex = localActive.indexOfFirst { it.id == tile.id }
                                        val isDragging = draggedTileId == tile.id && dragSourceKind == "active"
                                        key(tile.id) {
                                            Box(
                                                modifier = Modifier
                                                    .width(targetTileWidth)
                                                    .onGloballyPositioned { coords ->
                                                        if (slotIndex >= 0) slotBoundsMap[slotIndex] = coords.boundsInRoot()
                                                        tileBoundsMap[tile.id] = coords.boundsInRoot()
                                                    }
                                                    .pointerInput(tile.id) {
                                                        detectDragGesturesAfterLongPress(
                                                            onDragStart = { offset ->
                                                                val idx = localActive.indexOfFirst { it.id == tile.id }
                                                                if (idx < 0) return@detectDragGesturesAfterLongPress
                                                                draggedTileId = tile.id
                                                                dragSourceKind = "active"
                                                                dragSourceIndex = idx
                                                                hoveredTargetIndex = -1
                                                                hoveredCompSlotIndex = -1
                                                                onDragStateChange(true)
                                                                val myBounds = slotBoundsMap[idx] ?: tileBoundsMap[tile.id] ?: Rect.Zero
                                                                currentDragCenter = myBounds.topLeft + offset
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            },
                                                            onDrag = { change, dragAmount ->
                                                                change.consume()
                                                                currentDragCenter += dragAmount
                                                                checkAutoScroll(currentDragCenter)

                                                                if (draggedTileId != null) {
                                                                    val compBounds = compRowContainerBounds
                                                                    val activeContainer = activeGridContainerBounds
                                                                    val dropBounds = inactiveDropZoneBounds
                                                                    val isCompTile = tile.isComplicationTile || tile.complicationSpec != null
                                                                    val overCompRow = show2x1ComplicationEditor && isCompTile && compBounds != null &&
                                                                        Rect(compBounds.left - 12f, compBounds.top - 12f, compBounds.right + 12f, compBounds.bottom + 12f).contains(currentDragCenter)
                                                                    val isStrictlyAvailable = dropBounds != null &&
                                                                        activeContainer != null &&
                                                                        currentDragCenter.y > activeContainer.bottom + 10f &&
                                                                        currentDragCenter.y >= dropBounds.top

                                                                    if (overCompRow) {
                                                                        hoveredTargetIndex = -1
                                                                        var targetCompIdx = -1
                                                                        for (i in 0 until qqs2x1EditTiles.size) {
                                                                            val b = compSlotBoundsMap[i] ?: continue
                                                                            if (Rect(b.left - 8f, b.top - 8f, b.right + 8f, b.bottom + 8f).contains(currentDragCenter)) {
                                                                                targetCompIdx = i
                                                                                break
                                                                            }
                                                                        }
                                                                        if (targetCompIdx != hoveredCompSlotIndex) {
                                                                            hoveredCompSlotIndex = targetCompIdx
                                                                            if (targetCompIdx != -1) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                        }
                                                                    } else if (isStrictlyAvailable) {
                                                                        hoveredTargetIndex = -1
                                                                        hoveredCompSlotIndex = -1
                                                                    } else {
                                                                        hoveredCompSlotIndex = -1
                                                                        var targetIndex = -1
                                                                        val totalCount = localActive.size
                                                                        for (i in 0 until totalCount) {
                                                                            val b = slotBoundsMap[i] ?: continue
                                                                            val expanded = Rect(b.left - 10f, b.top - 10f, b.right + 10f, b.bottom + 10f)
                                                                            if (expanded.contains(currentDragCenter)) {
                                                                                targetIndex = i
                                                                                break
                                                                            }
                                                                        }
                                                                        if (targetIndex != -1 && targetIndex != dragSourceIndex) {
                                                                            if (hoveredTargetIndex != targetIndex) {
                                                                                hoveredTargetIndex = targetIndex
                                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                            }
                                                                        } else {
                                                                            hoveredTargetIndex = -1
                                                                        }
                                                                    }
                                                                }
                                                            },
                                                            onDragEnd = {
                                                                autoScrollDirection = 0
                                                                onDragStateChange(false)
                                                                val activeId = draggedTileId
                                                                val compBounds = compRowContainerBounds
                                                                val dropBounds = inactiveDropZoneBounds
                                                                val activeContainer = activeGridContainerBounds
                                                                val sourceIdx = dragSourceIndex
                                                                val targetIdx = hoveredTargetIndex
                                                                val targetCompIdx = hoveredCompSlotIndex
                                                                val isCompTile = tile.isComplicationTile || tile.complicationSpec != null

                                                                val isDropToCompRow = activeId != null && show2x1ComplicationEditor && isCompTile &&
                                                                    compBounds != null &&
                                                                    Rect(compBounds.left - 12f, compBounds.top - 12f, compBounds.right + 12f, compBounds.bottom + 12f).contains(currentDragCenter)
                                                                val isDropToAvailable = activeId != null && dropBounds != null &&
                                                                    ((activeContainer != null && currentDragCenter.y > activeContainer.bottom + 10f && currentDragCenter.y >= dropBounds.top) ||
                                                                     (activeContainer == null && currentDragCenter.y >= dropBounds.top))

                                                                if (isDropToCompRow) {
                                                                    PanelPreferences.insertQqs2x1ComplicationId(
                                                                        context,
                                                                        activeId!!,
                                                                        targetCompIdx.takeIf { it != -1 }
                                                                    )
                                                                    val updatedActive = localActive.toMutableList()
                                                                    val moved = updatedActive.firstOrNull { it.id == activeId }
                                                                    if (moved != null) {
                                                                        updatedActive.remove(moved)
                                                                        val updatedInactive = localInactive.toMutableList().apply {
                                                                            removeAll { it.id == moved.id }
                                                                            add(0, moved.copy(isVisible = false))
                                                                        }
                                                                        localActive = updatedActive
                                                                        localInactive = updatedInactive
                                                                    }
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                } else if (isDropToAvailable) {
                                                                    val updatedActive = localActive.toMutableList()
                                                                    val moved = updatedActive.firstOrNull { it.id == activeId }
                                                                    if (moved != null) {
                                                                        updatedActive.remove(moved)
                                                                        val updatedInactive = localInactive.toMutableList().apply {
                                                                            removeAll { it.id == moved.id }
                                                                            add(0, moved.copy(isVisible = false))
                                                                        }
                                                                        localActive = updatedActive
                                                                        localInactive = updatedInactive
                                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                    }
                                                                } else if (sourceIdx != -1 && targetIdx != -1 && sourceIdx != targetIdx &&
                                                                    sourceIdx in 0 until localActive.size && targetIdx in 0 until localActive.size
                                                                ) {
                                                                    val updated = localActive.toMutableList()
                                                                    java.util.Collections.swap(updated, sourceIdx, targetIdx)
                                                                    localActive = updated
                                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                }
                                                                draggedTileId = null
                                                                dragSourceKind = null
                                                                dragSourceIndex = -1
                                                                hoveredTargetIndex = -1
                                                                hoveredCompSlotIndex = -1
                                                                onTilesUpdated(localActive, localInactive)
                                                            },
                                                            onDragCancel = {
                                                                autoScrollDirection = 0
                                                                onDragStateChange(false)
                                                                draggedTileId = null
                                                                dragSourceKind = null
                                                                dragSourceIndex = -1
                                                                hoveredTargetIndex = -1
                                                                hoveredCompSlotIndex = -1
                                                                onTilesUpdated(localActive, localInactive)
                                                            }
                                                        )
                                                    }
                                            ) {
                                                val isHoverTarget = draggedTileId != null && hoveredTargetIndex == slotIndex
                                                val cardContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                                                val availableTilesSolidBg = if (isDark) {
                                                    Color(
                                                        red = (cardContainerColor.red * 0.55f).coerceIn(0f, 1f),
                                                        green = (cardContainerColor.green * 0.55f).coerceIn(0f, 1f),
                                                        blue = (cardContainerColor.blue * 0.55f).coerceIn(0f, 1f),
                                                        alpha = 1f
                                                    )
                                                } else {
                                                    Color(
                                                        red = (cardContainerColor.red * 0.80f).coerceIn(0f, 1f),
                                                        green = (cardContainerColor.green * 0.80f).coerceIn(0f, 1f),
                                                        blue = (cardContainerColor.blue * 0.80f).coerceIn(0f, 1f),
                                                        alpha = 1f
                                                    )
                                                }
                                                if (isDragging) {
                                                    // Hide original drag box
                                                    Spacer(modifier = Modifier.width(targetTileWidth).height(86.dp))
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(targetTileWidth)
                                                            .height(86.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isHoverTarget) {
                                                            if (isKitKat) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .requiredSize(width = targetTileWidth + 6.dp, height = 92.dp)
                                                                        .clip(RoundedCornerShape(4.dp))
                                                                        .background(Color(0x3333B5E5))
                                                                        .border(1.5.dp, Color(0xFF33B5E5), RoundedCornerShape(4.dp))
                                                                )
                                                            } else if (isMarshmallow) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .requiredSize(width = targetTileWidth + 6.dp, height = 92.dp)
                                                                        .clip(RoundedCornerShape(panelOuterCorner.dp))
                                                                        .background(legacyAccent.copy(alpha = 0.2f))
                                                                        .border(1.5.dp, legacyAccent, RoundedCornerShape(panelOuterCorner.dp))
                                                                )
                                                            } else {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .requiredSize(width = targetTileWidth + 16.dp, height = 102.dp)
                                                                        .clip(RoundedCornerShape(20.dp))
                                                                        .background(availableTilesSolidBg)
                                                                )
                                                            }
                                                        }
                                                        EditStudioTileView(
                                                            tile = tile,
                                                            tileWidth = targetTileWidth,
                                                            isKitKat = isKitKat,
                                                            isMarshmallow = isMarshmallow,
                                                            isActive = true
                                                        )

                                                        // Minus remove button
                                                        if (draggedTileId == null) {
                                                            val badgeShape = if (isKitKat) RoundedCornerShape(3.dp) else CircleShape
                                                            val badgeColor = if (isKitKat) Color(0xFFCC0000) else Color(0xFFE53935)
                                                            Box(
                                                                modifier = Modifier
                                                                    .align(Alignment.TopEnd)
                                                                    .padding(top = 2.dp, end = 2.dp)
                                                                    .size(20.dp)
                                                                    .clip(badgeShape)
                                                                    .background(badgeColor)
                                                                    .clickable(
                                                                        interactionSource = remember { MutableInteractionSource() },
                                                                        indication = null
                                                                    ) {
                                                                        val updatedActive = localActive.toMutableList()
                                                                        val moved = updatedActive.firstOrNull { it.id == tile.id }
                                                                        if (moved != null) {
                                                                            updatedActive.remove(moved)
                                                                            val updatedInactive = localInactive.toMutableList().apply {
                                                                                removeAll { it.id == moved.id }
                                                                                add(0, moved.copy(isVisible = false))
                                                                            }
                                                                            localActive = updatedActive
                                                                            localInactive = updatedInactive
                                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                            onTilesUpdated(localActive, localInactive)
                                                                        }
                                                                    },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Remove,
                                                                    contentDescription = "Remove tile",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(13.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    repeat(gridLayout.columns - rowTiles.size) {
                                        Spacer(modifier = Modifier.width(targetTileWidth))
                                    }
                                }
                            }
                        }
                    }
                }

                renderActiveGroup(
                    "ACTIVE TILES (${localActive.size})",
                    localActive,
                    "No active tiles. Add one below."
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val cardContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
            val activeAccent = legacyAccent
            val availableTilesBg = if (isOverInactiveDropZone) {
                if (isKitKat) Color(0x3333B5E5) else if (isMarshmallow) legacyAccent.copy(alpha = 0.2f) else activeAccent.copy(alpha = 0.12f)
            } else if (isKitKat) {
                if (isDark) Color(0xFF161616) else Color(0xFFE8E8E8)
            } else if (isMarshmallow) {
                Color(MonetThemeHelper.getInactiveTilesColor(context, isDark))
            } else if (isDark) {
                Color(
                    red = (cardContainerColor.red * 0.55f).coerceIn(0f, 1f),
                    green = (cardContainerColor.green * 0.55f).coerceIn(0f, 1f),
                    blue = (cardContainerColor.blue * 0.55f).coerceIn(0f, 1f),
                    alpha = cardContainerColor.alpha
                )
            } else {
                Color(
                    red = (cardContainerColor.red * 0.80f).coerceIn(0f, 1f),
                    green = (cardContainerColor.green * 0.80f).coerceIn(0f, 1f),
                    blue = (cardContainerColor.blue * 0.80f).coerceIn(0f, 1f),
                    alpha = cardContainerColor.alpha
                )
            }
            val availableCornerRadius = if (isKitKat || isMarshmallow) panelOuterCorner.dp else 16.dp
            val availableBorderColor = if (isOverInactiveDropZone) {
                activeAccent
            } else if (isKitKat) {
                if (isDark) Color(0xFF2C2C2C) else Color(0xFFCCCCCC)
            } else if (isMarshmallow) {
                Color(MonetThemeHelper.getCardOutline(context, isDark)).copy(alpha = 0.5f)
            } else {
                Color.Transparent
            }

            // Inactive / Available Tiles & Complications Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        inactiveDropZoneBounds = coords.boundsInRoot()
                    }
                    .clip(RoundedCornerShape(availableCornerRadius))
                    .background(availableTilesBg)
                    .border(
                        width = if (isOverInactiveDropZone) 2.dp else (if (isKitKat || isMarshmallow) 1.dp else 0.dp),
                        color = availableBorderColor,
                        shape = RoundedCornerShape(availableCornerRadius)
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val handleAddTileToActive: (QsTileModel, Int?) -> Unit = { targetTile, slotIndex ->
                    val updatedInactive = localInactive.toMutableList()
                    val moved = updatedInactive.firstOrNull { it.id == targetTile.id }
                    if (moved != null) {
                        updatedInactive.remove(moved)
                        val isExt = moved.isExternal || moved.category == TileCategory.THIRD_PARTY || moved.category == TileCategory.EXTERNAL || moved.id.contains("/")
                        val pkg = moved.packageName ?: if (moved.id.contains("/")) moved.id.substringBefore('/') else null
                        val cls = moved.serviceClassName ?: if (moved.id.contains("/")) moved.id.substringAfter('/') else null
                        val compId = if (isExt && pkg != null && cls != null) "$pkg/$cls" else moved.id

                        val activated = moved.copy(
                            id = compId,
                            isVisible = true,
                            isExternal = isExt,
                            category = if (isExt) TileCategory.THIRD_PARTY else moved.category,
                            packageName = pkg,
                            serviceClassName = cls
                        )
                        val updatedActive = localActive.toMutableList().apply {
                            if (slotIndex != null) {
                                add(slotIndex.coerceIn(0, size), activated)
                            } else {
                                add(activated)
                            }
                        }
                        localActive = updatedActive
                        localInactive = updatedInactive
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTilesUpdated(localActive, localInactive)
                    }
                }

                val renderInactiveGrid: @Composable (List<QsTileModel>) -> Unit = { list ->
                    val rows = list.chunked(gridLayout.columns)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rows.forEach { rowTiles ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowTiles.forEach { tile ->
                                    Box {
                                        InactiveTileItem(
                                            tile = tile,
                                            targetTileWidth = targetTileWidth,
                                            draggedTileId = if (dragSourceKind == "inactive") draggedTileId else null,
                                            tileBoundsMap = tileBoundsMap,
                                            isKitKat = isKitKat,
                                            isMarshmallow = isMarshmallow,
                                            onDragStart = { offset ->
                                                draggedTileId = tile.id
                                                dragSourceKind = "inactive"
                                                hoveredCompSlotIndex = -1
                                                hoveredTargetIndex = -1
                                                onDragStateChange(true)
                                                val myBounds = tileBoundsMap[tile.id] ?: Rect.Zero
                                                currentDragCenter = myBounds.topLeft + offset
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            onDrag = { dragAmount ->
                                                currentDragCenter += dragAmount
                                                checkAutoScroll(currentDragCenter)
                                                val compBounds = compRowContainerBounds
                                                val isCompTile = tile.isComplicationTile || tile.complicationSpec != null
                                                if (show2x1ComplicationEditor && isCompTile && compBounds != null &&
                                                    Rect(compBounds.left - 12f, compBounds.top - 12f, compBounds.right + 12f, compBounds.bottom + 12f).contains(currentDragCenter)
                                                ) {
                                                    var foundCompIdx = -1
                                                    for (i in 0 until qqs2x1EditTiles.size) {
                                                        val b = compSlotBoundsMap[i] ?: continue
                                                        if (Rect(b.left - 8f, b.top - 8f, b.right + 8f, b.bottom + 8f).contains(currentDragCenter)) {
                                                            foundCompIdx = i
                                                            break
                                                        }
                                                    }
                                                    hoveredCompSlotIndex = foundCompIdx
                                                } else {
                                                    hoveredCompSlotIndex = -1
                                                }
                                            },
                                            onDragEnd = {
                                                autoScrollDirection = 0
                                                onDragStateChange(false)
                                                val compBounds = compRowContainerBounds
                                                val dropBounds = inactiveDropZoneBounds
                                                val activeContainer = activeGridContainerBounds
                                                val isCompTile = tile.isComplicationTile || tile.complicationSpec != null

                                                val isDraggedToCompRow = show2x1ComplicationEditor && isCompTile &&
                                                    compBounds != null &&
                                                    Rect(compBounds.left - 12f, compBounds.top - 12f, compBounds.right + 12f, compBounds.bottom + 12f).contains(currentDragCenter)

                                                val isDraggedToActive = !isDraggedToCompRow &&
                                                    ((activeContainer != null && currentDragCenter.y <= activeContainer.bottom + 10f) ||
                                                     (dropBounds != null && currentDragCenter.y < dropBounds.top))

                                                if (isDraggedToCompRow) {
                                                    PanelPreferences.insertQqs2x1ComplicationId(
                                                        context,
                                                        tile.id,
                                                        hoveredCompSlotIndex.takeIf { it != -1 }
                                                    )
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                } else if (isDraggedToActive) {
                                                    var targetSlot: Int? = null
                                                    for (i in 0 until localActive.size) {
                                                        val b = slotBoundsMap[i] ?: continue
                                                        if (b.contains(currentDragCenter)) {
                                                            targetSlot = i
                                                            break
                                                        }
                                                    }
                                                    handleAddTileToActive(tile, targetSlot)
                                                }
                                                draggedTileId = null
                                                dragSourceKind = null
                                                hoveredCompSlotIndex = -1
                                                hoveredTargetIndex = -1
                                            },
                                            onDragCancel = {
                                                autoScrollDirection = 0
                                                onDragStateChange(false)
                                                draggedTileId = null
                                                dragSourceKind = null
                                                hoveredCompSlotIndex = -1
                                                hoveredTargetIndex = -1
                                            },
                                            onClick = {
                                                handleAddTileToActive(tile, null)
                                            }
                                        )
                                        if (show2x1ComplicationEditor && tile.isComplicationTile && draggedTileId != tile.id) {
                                            val in2x1 = qqs2x1ComplicationIds.contains(tile.id)
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(top = 2.dp, start = 2.dp)
                                                    .clip(RoundedCornerShape(percent = 50))
                                                    .background(
                                                        if (in2x1) legacyAccent else MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        PanelPreferences.toggleQqs2x1ComplicationId(context, tile.id)
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                                    .testTag("edit_studio_toggle_2x1_${tile.id}")
                                            ) {
                                                Text(
                                                    text = if (in2x1) "2x1 ✓" else "+2x1",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (in2x1) Color.White else (if (isDark) Color(0xFFCCCCCC) else Color(0xFF333333))
                                                )
                                            }
                                        }
                                    }
                                }
                                repeat(gridLayout.columns - rowTiles.size) {
                                    Spacer(modifier = Modifier.width(targetTileWidth))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isOverInactiveDropZone) {
                            "DROP TO REMOVE FROM ACTIVE PANEL"
                        } else {
                            "AVAILABLE QS TILES (${inactiveQsTiles.size})"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = activeAccent
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (isOverInactiveDropZone) "Release to drop" else "Tap '+' or drag to add",
                        fontSize = 10.sp,
                        color = if (isDark) Color.Gray else Color(0xFF666666)
                    )
                }

                if (inactiveQsTiles.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All QS tiles are currently in your active panel",
                            fontSize = 12.sp,
                            color = if (isDark) Color.Gray else Color(0xFF888888)
                        )
                    }
                } else {
                    val systemInactive = inactiveQsTiles.filter { !it.isExternal }
                    val externalInactive = inactiveQsTiles.filter { it.isExternal }

                    if (systemInactive.isNotEmpty()) {
                        Text(
                            text = "SYSTEM TILES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                        renderInactiveGrid(systemInactive)
                    }

                    if (externalInactive.isNotEmpty()) {
                        Text(
                            text = "THIRD-PARTY APP TILES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                        renderInactiveGrid(externalInactive)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = activeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isOverInactiveDropZone) {
                            "DROP TO REMOVE FROM ACTIVE PANEL"
                        } else {
                            "COMPLICATIONS (${inactiveComplications.size})"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = activeAccent
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (isOverInactiveDropZone) "Release to drop" else "Tap '+' or drag to add",
                        fontSize = 10.sp,
                        color = if (isDark) Color.Gray else Color(0xFF666666)
                    )
                }

                if (inactiveComplications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All complications are currently in your active panel",
                            fontSize = 12.sp,
                            color = if (isDark) Color.Gray else Color(0xFF888888)
                        )
                    }
                } else {
                    renderInactiveGrid(inactiveComplications)
                }
            }



            // Bottom breathing space so buttons and tiles are never squished by system navigation bars
            Spacer(modifier = Modifier.height(48.dp))
        }

        // Isolated Floating Overlay for Dragged Tile (never resets or shifts position on list recomposition)
        if (draggedTileId != null) {
            val draggedTile = localActive.firstOrNull { it.id == draggedTileId }
                ?: localInactive.firstOrNull { it.id == draggedTileId }
            val currentStudio = studioCoords
            if (draggedTile != null && currentStudio != null && currentStudio.isAttached) {
                val studioOrigin = currentStudio.boundsInRoot().topLeft
                val localX = currentDragCenter.x - studioOrigin.x
                val localY = currentDragCenter.y - studioOrigin.y
                if (dragSourceKind == "comp2x1") {
                    val halfW = with(density) { (compCardWidthDp / 2f).toPx() }
                    val halfH = with(density) { (qqs2x1TileHeightDp / 2f).dp.toPx() }
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (localX - halfW).roundToInt(),
                                    (localY - halfH).roundToInt()
                                )
                            }
                            .width(compCardWidthDp)
                            .height(qqs2x1TileHeightDp.dp)
                            .zIndex(9999f)
                            .graphicsLayer {
                                scaleX = 1.05f
                                scaleY = 1.05f
                                alpha = 0.95f
                                shadowElevation = 24f
                            }
                    ) {
                        Qqs2x1ComplicationCard(
                            tile = draggedTile,
                            isDark = isDark,
                            onClick = {},
                            onLongClick = {},
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    val tileHalfW = with(density) { (targetTileWidth / 2f).toPx() }
                    val tileHalfH = with(density) { 43.dp.toPx() }

                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (localX - tileHalfW).roundToInt(),
                                    (localY - tileHalfH).roundToInt()
                                )
                            }
                            .width(targetTileWidth)
                            .zIndex(9999f)
                            .graphicsLayer {
                                scaleX = 1.08f
                                scaleY = 1.08f
                                alpha = 0.95f
                                shadowElevation = if (isKitKat) 8f else if (isMarshmallow) 16f else 24f
                            }
                    ) {
                        EditStudioTileView(
                            tile = draggedTile,
                            tileWidth = targetTileWidth,
                            isKitKat = isKitKat,
                            isMarshmallow = isMarshmallow,
                            isActive = true
                        )
                    }
                }
            }
        }
    }
}

/**
 * Unified Quick Settings Panel (Morphing QQS -> FULL QS with Horizontal Pager)
 */
@Composable
fun UnifiedQsPanel(
    expansionStage: QsExpansionStage,
    isEditingTiles: Boolean = false,
    editScrollState: ScrollState? = null,
    isTileDragging: Boolean = false,
    onTileDragStateChange: (Boolean) -> Unit = {},
    onToggleEditMode: () -> Unit = {},
    tiles: List<QsTileModel>,
    editActiveTiles: List<QsTileModel>? = null,
    editInactiveTiles: List<QsTileModel>? = null,
    gridLayout: QsGridLayout,
    brightness: Int,
    onBrightnessChange: (Int) -> Unit,
    onTileClick: (QsTileModel) -> Unit,
    onTileLongClick: (QsTileModel) -> Unit,
    onExpandFull: () -> Unit,
    onCollapseToQqs: () -> Unit,
    onClosePanel: () -> Unit,
    notifications: List<NotificationItem>,
    isNotificationPermissionGranted: Boolean,
    onNotificationClick: (NotificationItem) -> Unit,
    onNotificationDismiss: (NotificationItem) -> Unit,
    onNotificationActionClick: (NotificationActionModel) -> Unit,
    onDismissAllNotifications: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onNavigateToCustomizer: () -> Unit = onToggleEditMode,
    onNavigateToNotificationHub: () -> Unit = {},
    onOpenNotificationManager: () -> Unit = onNavigateToNotificationHub,
    onTilesReordered: (List<QsTileModel>, List<QsTileModel>) -> Unit = { _, _ -> },
    dragMorphFraction: Float = 0f,
    isDraggingMorph: Boolean = false,
    shadeDragModifier: Modifier = Modifier,
    shadeDragToCloseModifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val qqsTileSizeValue by PanelPreferences.qqsTileSize.collectAsState()
    val hideQqsDragHandle by PanelPreferences.hideQqsDragHandle.collectAsState()
    val dragHandleLength by PanelPreferences.dragHandleLength.collectAsState()
    val showCarrier by PanelPreferences.showCarrier.collectAsState()
    val showCarrierDivider by PanelPreferences.showCarrierDivider.collectAsState()
    val carrierDividerAsLine by PanelPreferences.carrierDividerAsLine.collectAsState()
    val hideCarrierInExpanded by PanelPreferences.hideCarrierInExpanded.collectAsState()
    val moveCollapseToQqsFooter by PanelPreferences.moveCollapseToQqsFooter.collectAsState()
    val brightnessAtBottom by PanelPreferences.brightnessAtBottom.collectAsState()
    val brightnessInQqs by PanelPreferences.brightnessInQqs.collectAsState()
    val datePosition by PanelPreferences.datePosition.collectAsState()
    val dateBgEnabled by PanelPreferences.dateBackgroundEnabled.collectAsState()
    val dateBgCorner by PanelPreferences.dateBackgroundCorner.collectAsState()
    val batteryBgEnabled by PanelPreferences.batteryBackgroundEnabled.collectAsState()
    val batteryBgCorner by PanelPreferences.batteryBackgroundCorner.collectAsState()
    val clockBgEnabled by PanelPreferences.clockBackgroundEnabled.collectAsState()
    val clockBgCorner by PanelPreferences.clockBackgroundCorner.collectAsState()
    val carrierBgEnabled by PanelPreferences.carrierBackgroundEnabled.collectAsState()
    val carrierBgCorner by PanelPreferences.carrierBackgroundCorner.collectAsState()
    val statusBadgeBgColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
    val statusBadgeContentColor = if (statusBadgeBgColor.luminance() > 0.5f) Color(0xFF1F1F1F) else Color.White
    val context = LocalContext.current
    val batteryInfo = rememberQqsBatteryState()
    val onSettingsClick = remember {
        {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
            onClosePanel()
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val disablePanelAnimations by PanelPreferences.disablePanelAnimations.collectAsState()
    val carrierName = remember(context) {
        try {
            val tm = context.getSystemService(android.content.Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
            val simState = tm?.simState ?: android.telephony.TelephonyManager.SIM_STATE_UNKNOWN
            val isSimAvailable = simState != android.telephony.TelephonyManager.SIM_STATE_ABSENT &&
                    simState != android.telephony.TelephonyManager.SIM_STATE_UNKNOWN

            val name = tm?.networkOperatorName?.trim()
            val simOperatorName = tm?.simOperatorName?.trim()
            val resolvedName = if (!name.isNullOrBlank() && name != "Android" && !name.equals("Google Fi", ignoreCase = true)) {
                name
            } else if (!simOperatorName.isNullOrBlank() && simOperatorName != "Android" && !simOperatorName.equals("Google Fi", ignoreCase = true)) {
                simOperatorName
            } else if (name.equals("Google Fi", ignoreCase = true) || simOperatorName.equals("Google Fi", ignoreCase = true)) {
                if (isSimAvailable) "Google Fi" else "No SIM"
            } else {
                null
            }

            if (!resolvedName.isNullOrBlank()) {
                resolvedName
            } else {
                if (isSimAvailable) "Emergency calls only" else "No SIM"
            }
        } catch (e: Exception) {
            "No SIM"
        }
    }
    val hiddenTileIds by PanelPreferences.hiddenTileIds.collectAsState()
    val availableRawTiles = remember(tiles, hiddenTileIds) {
        tiles.filter {
            it.id !in hiddenTileIds &&
            (if (it.id == "sound_profile") "sound_mode" !in hiddenTileIds && "volume" !in hiddenTileIds else true)
        }
    }
    val activeTiles = remember(availableRawTiles, isEditingTiles, editActiveTiles) {
        if (isEditingTiles && editActiveTiles != null) {
            editActiveTiles
        } else {
            availableRawTiles.filter { it.isVisible }
        }
    }
    val inactiveTiles = remember(availableRawTiles, isEditingTiles, editInactiveTiles) {
        if (isEditingTiles && editInactiveTiles != null) {
            editInactiveTiles
        } else {
            availableRawTiles.filter { !it.isVisible }
        }
    }
    val enableQqsHorizontalScroll by PanelPreferences.enableQqsHorizontalScroll.collectAsState()
    val enableQqsComplicationsRow by PanelPreferences.enableQqsComplicationsRow.collectAsState()
    val qqs2x1ComplicationIds by PanelPreferences.qqs2x1ComplicationIds.collectAsState()
    val qqsTileCount by PanelPreferences.qqsTileCount.collectAsState()
    val qqsTiles = remember(activeTiles, qqsTileCount, enableQqsHorizontalScroll) {
        if (enableQqsHorizontalScroll) {
            activeTiles
        } else {
            activeTiles.take(qqsTileCount.coerceIn(2, 6))
        }
    }
    val qqs2x1Tiles = remember(tiles, qqs2x1ComplicationIds, enableQqsComplicationsRow) {
        if (!enableQqsComplicationsRow) {
            emptyList()
        } else {
            val tileMap = tiles.associateBy { it.id }
            qqs2x1ComplicationIds.mapNotNull { id ->
                tileMap[id]?.takeIf { it.complicationSpec != null }
            }
        }
    }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val isAndroidOreoStyle by PanelPreferences.isAndroidOreoStyle.collectAsState()
    val isAndroidMarshmallowStyle by PanelPreferences.isAndroidMarshmallowStyle.collectAsState()
    val effectiveRows = if (isLandscape) 1 else gridLayout.rows.coerceAtLeast(1)
    val effectiveColumns = gridLayout.columns.coerceAtLeast(1)
    val tilesPerPage = remember(activeTiles, effectiveRows, effectiveColumns) { (effectiveColumns * effectiveRows).coerceAtLeast(1) }
    val tilePages = remember(activeTiles, tilesPerPage) { activeTiles.chunked(tilesPerPage) }
    var viewPagerCurrentPage by remember { mutableIntStateOf(0) }
    var viewPagerScrollPosition by remember { mutableFloatStateOf(0f) }
    var viewPagerRef by remember { mutableStateOf<ViewPager2?>(null) }

    val density = LocalDensity.current

    var activeMediaState by remember { mutableStateOf(MediaState.Preview) }

    val isFullQs = expansionStage == QsExpansionStage.FULL_QS || isLandscape
    val targetMorph = when {
        isDraggingMorph -> dragMorphFraction
        isFullQs -> 1f
        else -> 0f
    }
    val morphProgress by animateFloatAsState(
        targetValue = targetMorph,
        animationSpec = if (isDraggingMorph || disablePanelAnimations) {
            androidx.compose.animation.core.snap()
        } else {
            spring(
                dampingRatio = 0.80f,
                stiffness = 380f
            )
        },
        label = "qs_morph_progress"
    )

    val isExpandedQsState = expansionStage == QsExpansionStage.FULL_QS || morphProgress > 0.5f
    val isCarrierVisible = showCarrier && !(hideCarrierInExpanded && isExpandedQsState)
    val isSplitCarrierConfigured = isCarrierVisible && showCarrierDivider && !carrierDividerAsLine && !isLandscape && !isEditingTiles
    val isSplitCarrier = isSplitCarrierConfigured && morphProgress < 0.5f
    val isInlineLineCarrier = isCarrierVisible && showCarrierDivider && carrierDividerAsLine && !isLandscape && !isEditingTiles && morphProgress < 0.5f
    val showSplitCarrierCard = !isAndroidOreoStyle && !isAndroidMarshmallowStyle && isSplitCarrierConfigured && (expansionStage == QsExpansionStage.QQS || isDraggingMorph || morphProgress < 0.99f)

    val placeholderTile = remember {
        QsTileModel(
            id = "placeholder_slot",
            label = " ",
            subtitle = " ",
            state = TileState.INACTIVE,
            category = TileCategory.SYSTEM,
            isVisible = false
        )
    }

    val liveMediaState by com.example.service.MediaSessionManagerHelper.mediaState.collectAsState()
    val mediaSessionsList by com.example.service.MediaSessionManagerHelper.sessionsList.collectAsState()
    val selectedSessionIndex by com.example.service.MediaSessionManagerHelper.selectedSessionIndex.collectAsState()

    LaunchedEffect(Unit) {
        com.example.service.MediaSessionManagerHelper.refreshState()
    }

    val showMediaInNotifContainer by PanelPreferences.showMediaInNotificationContainer.collectAsState()
    val forceShowMediaPreview by PanelPreferences.forceShowMediaPreview.collectAsState()
    val shouldShowPlayerInNotification = showMediaInNotifContainer &&
            (liveMediaState != null || forceShowMediaPreview)

    val mediaCardContent: @Composable () -> Unit = {
        val media = liveMediaState ?: if (forceShowMediaPreview) MediaState.Preview else null
        if (!isEditingTiles && media != null && !shouldShowPlayerInNotification) {
            val isMediaExpanded = isFullQs || isLandscape
            val mediaTopGap = if (isAndroidOreoStyle) 0.dp else 10.dp
            if (mediaTopGap > 0.dp) {
                Spacer(modifier = Modifier.height(mediaTopGap))
            }
            val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()

            val mediaAccentColor = com.example.ui.theme.getMiscElementColor(isDark)
            val effectiveCornerRadius = if (isAndroidOreoStyle) 0.dp else panelOuterCorner.dp
            val isPanelCustomizedForMedia = PanelPreferences.qsPanelBackgroundColor.collectAsState().value != null ||
                PanelPreferences.getPanelContainerUseCustom(isDark).collectAsState().value ||
                PanelPreferences.getPanelContainerColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.DEFAULT ||
                PanelPreferences.getPanelContainerHue(isDark).collectAsState().value != 0f ||
                PanelPreferences.getPanelContainerSat(isDark).collectAsState().value != 1f ||
                PanelPreferences.getPanelContainerVal(isDark).collectAsState().value != 1f ||
                PanelPreferences.showMonetSurfaceBackground.collectAsState().value

            val effectiveBgColor = if (isAndroidOreoStyle) {
                if (isPanelCustomizedForMedia) com.example.ui.theme.getQsPanelBackgroundColor(isDark)
                else Color(MonetThemeHelper.getSurfaceBackground(context, isDark))
            } else {
                if (isDark) Color(0xFF353A3B) else Color(0xFFE2E7EA)
            }
            AdaptiveMediaPlayerCard(
                mediaState = media,
                isExpanded = isMediaExpanded,
                sessions = mediaSessionsList,
                selectedIndex = selectedSessionIndex,
                onSelectSession = { index ->
                    com.example.service.MediaSessionManagerHelper.selectSession(index)
                },
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = effectiveCornerRadius,
                backgroundColor = effectiveBgColor,
                accentColor = mediaAccentColor,
                contentColor = if (isDark) Color.White else Color(0xFF222222),
                onPlayPauseClick = {
                    com.example.service.MediaSessionManagerHelper.playPause()
                },
                onPreviousClick = {
                    com.example.service.MediaSessionManagerHelper.previous()
                },
                onNextClick = {
                    com.example.service.MediaSessionManagerHelper.next()
                },
                onFavoriteClick = {
                    com.example.service.MediaSessionManagerHelper.toggleFavorite()
                },
                onRepeatClick = {
                    com.example.service.MediaSessionManagerHelper.toggleRepeat()
                },
                onShuffleClick = {
                    com.example.service.MediaSessionManagerHelper.toggleShuffle()
                },
                onSeek = { newProgress ->
                    com.example.service.MediaSessionManagerHelper.seekTo(newProgress)
                },
                onOutputDeviceClick = {
                    com.example.service.MediaSessionManagerHelper.openAudioOutputPicker(context)
                },
                onCloseClick = {
                    com.example.service.MediaSessionManagerHelper.stopAndDismiss()
                },
                onCardClick = {
                    com.example.service.MediaSessionManagerHelper.openActivePlayerApp(context)
                    onClosePanel()
                }
            )
        }
    }

    val qsCardContent: @Composable () -> Unit = {
        if (isAndroidMarshmallowStyle) {
            val baseSurfaceBg = MonetThemeHelper.getSurfaceBackground(context, isDark)
            val customPanelBg = PanelPreferences.qsPanelBackgroundColor.collectAsState().value
                ?: PanelPreferences.qsBackgroundColor.collectAsState().value

            val isPanelContainerCustomized = customPanelBg != null ||
                PanelPreferences.getPanelContainerUseCustom(isDark).collectAsState().value ||
                PanelPreferences.getPanelContainerColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.DEFAULT ||
                PanelPreferences.getPanelContainerHue(isDark).collectAsState().value != 0f ||
                PanelPreferences.getPanelContainerSat(isDark).collectAsState().value != 1f ||
                PanelPreferences.getPanelContainerVal(isDark).collectAsState().value != 1f ||
                PanelPreferences.showMonetSurfaceBackground.collectAsState().value

            val isHeaderGradientCustomized = PanelPreferences.getHeaderGradientUseCustom(isDark).collectAsState().value ||
                PanelPreferences.getHeaderGradientColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.PRIMARY ||
                PanelPreferences.getHeaderGradientHue(isDark).collectAsState().value != 0f ||
                PanelPreferences.getHeaderGradientSat(isDark).collectAsState().value != 1f ||
                PanelPreferences.getHeaderGradientVal(isDark).collectAsState().value != 1f

            val isMiscCustomized = PanelPreferences.getMiscElementsUseCustom(isDark).collectAsState().value ||
                PanelPreferences.getMiscElementsColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.PRIMARY ||
                PanelPreferences.getMiscElementsHue(isDark).collectAsState().value != 0f ||
                PanelPreferences.getMiscElementsSat(isDark).collectAsState().value != 1f ||
                PanelPreferences.getMiscElementsVal(isDark).collectAsState().value != 1f

            val panelElementColor = com.example.ui.theme.getQsPanelBackgroundColor(isDark)
            val headerGradientElementColor = com.example.ui.theme.getHeaderGradientElementColor(isDark)
            val miscElementColor = com.example.ui.theme.getMiscElementColor(isDark)

            val bodyBg = when {
                customPanelBg != null -> Color(customPanelBg)
                isPanelContainerCustomized -> panelElementColor
                isDark -> Color(MonetThemeHelper.getCardBackground(context, isDark))
                else -> {
                    val r = (android.graphics.Color.red(baseSurfaceBg) * 0.98f).toInt().coerceIn(0, 255)
                    val g = (android.graphics.Color.green(baseSurfaceBg) * 0.98f).toInt().coerceIn(0, 255)
                    val b = (android.graphics.Color.blue(baseSurfaceBg) * 0.98f).toInt().coerceIn(0, 255)
                    Color(android.graphics.Color.argb(255, r, g, b))
                }
            }

            val darkHeaderBg = when {
                customPanelBg != null -> Color(customPanelBg)
                isHeaderGradientCustomized -> headerGradientElementColor
                isPanelContainerCustomized -> {
                    val factor = 0.88f
                    Color(
                        red = (bodyBg.red * factor).coerceIn(0f, 1f),
                        green = (bodyBg.green * factor).coerceIn(0f, 1f),
                        blue = (bodyBg.blue * factor).coerceIn(0f, 1f),
                        alpha = 1.0f
                    )
                }
                isDark -> Color(MonetThemeHelper.brightenColor(baseSurfaceBg, true))
                else -> {
                    val r = (android.graphics.Color.red(baseSurfaceBg) * 0.90f).toInt().coerceIn(0, 255)
                    val g = (android.graphics.Color.green(baseSurfaceBg) * 0.90f).toInt().coerceIn(0, 255)
                    val b = (android.graphics.Color.blue(baseSurfaceBg) * 0.90f).toInt().coerceIn(0, 255)
                    Color(android.graphics.Color.argb(255, r, g, b))
                }
            }
            val textLightSecondary = Color(MonetThemeHelper.getTextSecondary(context, isDark))
            val headerTextColor = Color(MonetThemeHelper.getTextPrimary(context, isDark))
            val headerActionTint = if (isMiscCustomized) miscElementColor else headerTextColor
            val primaryColor = com.example.ui.theme.getQsTilesElementColor(isDark)
            val now = remember { java.util.Date() }
            val timeFormatter = remember { java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()) }
            val dateFormatter = remember { java.text.SimpleDateFormat("EEEE, MMMM d", java.util.Locale.getDefault()) }
            val batteryInfo = rememberQqsBatteryState()

            val wifiTile = activeTiles.find { it.id == "wifi" }
            val isWifiTileEnabled = wifiTile != null
            val isWifiOn = wifiTile?.state == TileState.ACTIVE
            val cellularTile = activeTiles.find { it.id == "cellular" }
            val isCellularTileEnabled = cellularTile != null
            val isCellularOn = cellularTile?.state == TileState.ACTIVE
            val btTile = activeTiles.find { it.id == "bluetooth" }
            val isBtTileEnabled = btTile != null
            val isBtOn = btTile?.state == TileState.ACTIVE

            val panelGlobalCornerEnabled by PanelPreferences.panelGlobalCornerEnabled.collectAsState()
            val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
            val panelInnerCorner by PanelPreferences.panelInnerCorner.collectAsState()
            val effectiveOuterCorner = panelOuterCorner.dp
            val effectiveInnerCorner = (if (panelGlobalCornerEnabled) panelOuterCorner else panelInnerCorner).dp

            val brightnessAtBottom by PanelPreferences.brightnessAtBottom.collectAsState()
            val brightnessInQqs by PanelPreferences.brightnessInQqs.collectAsState()

            val showCarrier by PanelPreferences.showCarrier.collectAsState()
            val showCarrierDivider by PanelPreferences.showCarrierDivider.collectAsState()
            val hideCarrierInExpanded by PanelPreferences.hideCarrierInExpanded.collectAsState()
            val batteryPercentageLocation by PanelPreferences.batteryPercentageLocation.collectAsState()
            val settingsIconLocation by PanelPreferences.settingsIconLocation.collectAsState()
            val editButtonPosition by PanelPreferences.editButtonPosition.collectAsState()

            val showCustomHeaderImage by PanelPreferences.showCustomHeaderImage.collectAsState()
            val customHeaderImageUri by PanelPreferences.customHeaderImageUri.collectAsState()
            val customHeaderImageHeight by PanelPreferences.customHeaderImageHeight.collectAsState()
            val customHeaderImageOpacity by PanelPreferences.customHeaderImageOpacity.collectAsState()

            val sliderSyncAll by PanelPreferences.sliderSyncAll.collectAsState()
            val globalProgressStyle by PanelPreferences.progressBarStyle.collectAsState()
            val brightnessSliderStylePref by PanelPreferences.brightnessSliderStyle.collectAsState()
            val resolvedBrightnessStyle = if (sliderSyncAll) globalProgressStyle else brightnessSliderStylePref

            val qqsBottomSliderOffset = when {
                brightnessInQqs && (resolvedBrightnessStyle == ProgressBarStyle.ANDROID_12 || resolvedBrightnessStyle == ProgressBarStyle.DEFAULT) -> (-6).dp
                !brightnessInQqs -> 12.dp
                else -> 0.dp
            }

            if (isEditingTiles) {
                EditModeTileStudio(
                    activeTiles = activeTiles,
                    inactiveTiles = inactiveTiles,
                    gridLayout = gridLayout,
                    editScrollState = editScrollState ?: rememberScrollState(),
                    onDragStateChange = onTileDragStateChange,
                    onTilesUpdated = onTilesReordered,
                    onDone = onToggleEditMode
                )
            } else {
                ContainerCard(
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = effectiveOuterCorner,
                        bottomEnd = effectiveOuterCorner
                    ),
                    containerColor = bodyBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    val qsScrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        // 1. Top Header (shade_header - Solid Slate #263238)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 0.dp,
                                        topEnd = 0.dp,
                                        bottomStart = if (morphProgress < 0.5f) effectiveOuterCorner else 0.dp,
                                        bottomEnd = if (morphProgress < 0.5f) effectiveOuterCorner else 0.dp
                                    )
                                )
                                .background(darkHeaderBg)
                                .then(shadeDragModifier)
                        ) {
                            if (showCustomHeaderImage) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(customHeaderImageHeight.dp)
                                ) {
                                    if (customHeaderImageUri.isNotBlank()) {
                                        coil.compose.AsyncImage(
                                            model = customHeaderImageUri,
                                            contentDescription = "Custom Header Image",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(alpha = customHeaderImageOpacity),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.horizontalGradient(
                                                        listOf(
                                                            com.example.ui.theme.getMiscElementColor(isDark).copy(alpha = 0.85f),
                                                            androidx.compose.material3.MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f),
                                                            androidx.compose.material3.MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                }
                            }

                            if (morphProgress < 0.5f && !isLandscape) {
                                // --- COLLAPSED STATUS HEADER ---
                                if (brightnessInQqs && !brightnessAtBottom) {
                                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                                        BrightnessSliderRow(
                                            brightness = brightness,
                                            onBrightnessChange = onBrightnessChange,
                                            sliderAlpha = 1f,
                                            sliderHeight = 36.dp,
                                            isAtBottom = false,
                                            verticalOffset = 12.dp,
                                            isQqs = true
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(if (clockBgEnabled || dateBgEnabled) 4.dp else 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(clockBgCorner.dp))
                                                .then(
                                                    if (clockBgEnabled) {
                                                        Modifier
                                                            .background(statusBadgeBgColor)
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    } else Modifier
                                                )
                                                .clickable { launchClockApp(context); onClosePanel() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = timeFormatter.format(now),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (clockBgEnabled) statusBadgeContentColor else headerTextColor
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(dateBgCorner.dp))
                                                .then(
                                                    if (dateBgEnabled) {
                                                        Modifier
                                                            .background(statusBadgeBgColor)
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    } else Modifier
                                                )
                                                .clickable { launchCalendarApp(context); onClosePanel() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dateFormatter.format(now),
                                                fontSize = 12.sp,
                                                color = if (dateBgEnabled) statusBadgeContentColor else textLightSecondary
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isWifiTileEnabled && isWifiOn) {
                                            Icon(
                                                imageVector = Icons.Default.Wifi,
                                                contentDescription = "Wi-Fi",
                                                tint = headerTextColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        if (isCellularTileEnabled && isCellularOn) {
                                            Icon(
                                                imageVector = Icons.Default.CellTower,
                                                contentDescription = "Signal",
                                                tint = headerTextColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        if (isBtTileEnabled && isBtOn) {
                                            Icon(
                                                imageVector = Icons.Default.Bluetooth,
                                                contentDescription = "Bluetooth",
                                                tint = headerTextColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        val resolvedMmBatteryColor = if (batteryBgEnabled) statusBadgeContentColor else headerTextColor
                                        val mmChargingTint = if (batteryBgEnabled && statusBadgeBgColor.luminance() > 0.5f) Color(0xFF2E7D32) else Color(0xFF4CAF50)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(batteryBgCorner.dp))
                                                .then(
                                                    if (batteryBgEnabled) {
                                                        Modifier
                                                            .background(statusBadgeBgColor)
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    } else Modifier
                                                )
                                        ) {
                                            if (batteryPercentageLocation == BatteryPercentageLocation.HEADER || batteryPercentageLocation == BatteryPercentageLocation.QQS_RIGHT) {
                                                Text(
                                                    text = "${batteryInfo.percentage}%",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = resolvedMmBatteryColor
                                                )
                                            }
                                            Icon(
                                                imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                                contentDescription = "Battery",
                                                tint = if (batteryInfo.isCharging) mmChargingTint else resolvedMmBatteryColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        if (settingsIconLocation == SettingsIconLocation.HEADER || settingsIconLocation == SettingsIconLocation.CARRIER_RIGHT) {
                                            IconButton(
                                                onClick = onSettingsClick,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = "Settings",
                                                    tint = headerActionTint,
                                                    modifier = Modifier.size(17.dp)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = onExpandFull,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Expand Quick Settings",
                                                tint = headerActionTint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                if (brightnessInQqs && brightnessAtBottom) {
                                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                                        BrightnessSliderRow(
                                            brightness = brightness,
                                            onBrightnessChange = onBrightnessChange,
                                            sliderAlpha = 1f,
                                            sliderHeight = 36.dp,
                                            isAtBottom = true,
                                            verticalOffset = qqsBottomSliderOffset,
                                            isQqs = true
                                        )
                                    }
                                }
                            } else {
                                // --- EXPANDED STATUS HEADER ---
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (showCarrier && !hideCarrierInExpanded) {
                                            val resolvedMmCarrierColor = if (carrierBgEnabled) statusBadgeContentColor else textLightSecondary
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(carrierBgCorner.dp))
                                                    .then(
                                                        if (carrierBgEnabled) {
                                                            Modifier
                                                                .background(statusBadgeBgColor)
                                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                                        } else Modifier
                                                    )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CellTower,
                                                    contentDescription = null,
                                                    tint = resolvedMmCarrierColor,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = carrierName.ifBlank { "Emergency calls only" },
                                                    fontSize = 13.sp,
                                                    color = resolvedMmCarrierColor
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.width(1.dp))
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (isWifiTileEnabled && isWifiOn) {
                                                Icon(
                                                    imageVector = Icons.Default.Wifi,
                                                    contentDescription = "Wi-Fi",
                                                    tint = headerTextColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            if (isCellularTileEnabled && isCellularOn) {
                                                Icon(
                                                    imageVector = Icons.Default.CellTower,
                                                    contentDescription = "Signal",
                                                    tint = headerTextColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            if (isBtTileEnabled && isBtOn) {
                                                Icon(
                                                    imageVector = Icons.Default.Bluetooth,
                                                    contentDescription = "Bluetooth",
                                                    tint = headerTextColor,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            val resolvedMmExpBatteryColor = if (batteryBgEnabled) statusBadgeContentColor else headerTextColor
                                            val mmExpChargingTint = if (batteryBgEnabled && statusBadgeBgColor.luminance() > 0.5f) Color(0xFF2E7D32) else Color(0xFF4CAF50)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(batteryBgCorner.dp))
                                                    .then(
                                                        if (batteryBgEnabled) {
                                                            Modifier
                                                                .background(statusBadgeBgColor)
                                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                                        } else Modifier
                                                    )
                                            ) {
                                                if (batteryPercentageLocation == BatteryPercentageLocation.HEADER) {
                                                    Text(
                                                        text = "${batteryInfo.percentage}%",
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = resolvedMmExpBatteryColor
                                                    )
                                                }
                                                Icon(
                                                    imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                                    contentDescription = "Battery",
                                                    tint = if (batteryInfo.isCharging) mmExpChargingTint else resolvedMmExpBatteryColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = onToggleEditMode,
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Tiles",
                                                    tint = headerActionTint,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            if (settingsIconLocation == SettingsIconLocation.HEADER || settingsIconLocation == SettingsIconLocation.EXPANDED_QS) {
                                                IconButton(
                                                    onClick = onSettingsClick,
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Settings,
                                                        contentDescription = "Settings",
                                                        tint = headerActionTint,
                                                        modifier = Modifier.size(19.dp)
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = { onCollapseToQqs() },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowUp,
                                                    contentDescription = "Collapse to QQS",
                                                    tint = headerActionTint,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(clockBgCorner.dp))
                                            .then(
                                                if (clockBgEnabled) {
                                                    Modifier
                                                        .background(statusBadgeBgColor)
                                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                                } else Modifier
                                            )
                                            .clickable { launchClockApp(context); onClosePanel() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = timeFormatter.format(now),
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (clockBgEnabled) statusBadgeContentColor else headerTextColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(if (clockBgEnabled || dateBgEnabled) 4.dp else 2.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(dateBgCorner.dp))
                                            .then(
                                                if (dateBgEnabled) {
                                                    Modifier
                                                        .background(statusBadgeBgColor)
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                } else Modifier
                                            )
                                            .clickable { launchCalendarApp(context); onClosePanel() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dateFormatter.format(now),
                                            fontSize = 13.sp,
                                            color = if (dateBgEnabled) statusBadgeContentColor else textLightSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Existing Tile Container (qs_tile_container / ll_marshmallow_qs_body)
                        // Positioned directly beneath the top header, peeking out by exactly 8dp in collapsed state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clipToBounds()
                                .background(bodyBg)
                                .then(if (morphProgress < 0.5f) shadeDragModifier else Modifier)
                                .clickable(enabled = morphProgress < 0.2f && !isLandscape) { onExpandFull() }
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints.copy(minHeight = 0))
                                    val fullHeight = placeable.height
                                    val peekHeightPx = 8.dp.roundToPx()
                                    val currentHeight = if (isLandscape) {
                                        fullHeight
                                    } else {
                                        (peekHeightPx + (fullHeight - peekHeightPx) * morphProgress)
                                            .roundToInt()
                                            .coerceIn(peekHeightPx, fullHeight)
                                    }
                                    layout(placeable.width, currentHeight) {
                                        placeable.placeRelative(0, 0)
                                    }
                                }
                                .then(
                                    if (!isLandscape && morphProgress >= 0.99f) {
                                        Modifier
                                            .pointerInput(onCollapseToQqs) {
                                                val thresholdPx = 24.dp.toPx()
                                                val flingThresholdPx = 16.dp.toPx()

                                                awaitEachGesture {
                                                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                                    val downY = down.position.y
                                                    val downX = down.position.x
                                                    val startedAtBottom = (qsScrollState.maxValue == 0 || qsScrollState.value >= qsScrollState.maxValue - 8)

                                                    var hasTriggered = false

                                                    while (true) {
                                                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                                        val pointer = event.changes.firstOrNull { it.id == down.id } ?: break

                                                        if (!pointer.pressed) {
                                                            // Pointer released (KitKat ACTION_UP style)
                                                            val deltaY = pointer.position.y - downY
                                                            val deltaX = pointer.position.x - downX
                                                            if (!hasTriggered && startedAtBottom && (qsScrollState.maxValue == 0 || qsScrollState.value >= qsScrollState.maxValue - 8)) {
                                                                if (deltaY < -flingThresholdPx && kotlin.math.abs(deltaY) > kotlin.math.abs(deltaX)) {
                                                                    hasTriggered = true
                                                                    pointer.consume()
                                                                    onCollapseToQqs()
                                                                }
                                                            }
                                                            break
                                                        }

                                                        // Pointer moving (KitKat ACTION_MOVE style)
                                                        val deltaY = pointer.position.y - downY
                                                        val deltaX = pointer.position.x - downX

                                                        if (!hasTriggered && startedAtBottom && (qsScrollState.maxValue == 0 || qsScrollState.value >= qsScrollState.maxValue - 8)) {
                                                            if (deltaY < -thresholdPx && kotlin.math.abs(deltaY) > kotlin.math.abs(deltaX)) {
                                                                hasTriggered = true
                                                                pointer.consume()
                                                                onCollapseToQqs()
                                                                break
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            .verticalScroll(qsScrollState)
                                    } else Modifier
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            val contentAlpha = if (isLandscape) 1f else ((morphProgress - 0.05f) / 0.25f).coerceIn(0f, 1f)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer(alpha = contentAlpha)
                            ) {
                                if (!brightnessAtBottom) {
                                    BrightnessSliderRow(
                                        brightness = brightness,
                                        onBrightnessChange = onBrightnessChange,
                                        sliderAlpha = 1f,
                                        sliderHeight = 36.dp,
                                        isAtBottom = false,
                                        isQqs = false
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))
                                }

                                MarshmallowTilesGrid(
                                    activeTiles = activeTiles,
                                    primaryAccent = primaryColor,
                                    onTileClick = onTileClick,
                                    onTileLongClick = onTileLongClick
                                )

                                if (brightnessAtBottom) {
                                    Spacer(modifier = Modifier.height(14.dp))

                                    BrightnessSliderRow(
                                        brightness = brightness,
                                        onBrightnessChange = onBrightnessChange,
                                        sliderAlpha = 1f,
                                        sliderHeight = 36.dp,
                                        isAtBottom = true,
                                        isQqs = false
                                    )
                                }

                                mediaCardContent()
                            }
                        }
                    }
                }
            }
        } else if (isAndroidOreoStyle) {
            val isPanelCustomizedOreo = PanelPreferences.qsPanelBackgroundColor.collectAsState().value != null ||
                PanelPreferences.getPanelContainerUseCustom(isDark).collectAsState().value ||
                PanelPreferences.getPanelContainerColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.DEFAULT ||
                PanelPreferences.getPanelContainerHue(isDark).collectAsState().value != 0f ||
                PanelPreferences.getPanelContainerSat(isDark).collectAsState().value != 1f ||
                PanelPreferences.getPanelContainerVal(isDark).collectAsState().value != 1f ||
                PanelPreferences.showMonetSurfaceBackground.collectAsState().value

            val isMiscCustomizedOreo = PanelPreferences.getMiscElementsUseCustom(isDark).collectAsState().value ||
                PanelPreferences.getMiscElementsColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.PRIMARY ||
                PanelPreferences.getMiscElementsHue(isDark).collectAsState().value != 0f ||
                PanelPreferences.getMiscElementsSat(isDark).collectAsState().value != 1f ||
                PanelPreferences.getMiscElementsVal(isDark).collectAsState().value != 1f

            val solidOreoBg = if (isPanelCustomizedOreo) {
                com.example.ui.theme.getQsPanelBackgroundColor(isDark)
            } else {
                Color(MonetThemeHelper.getSurfaceBackground(context, isDark))
            }
            val showCustomHeaderImage by PanelPreferences.showCustomHeaderImage.collectAsState()
            val customHeaderImageUri by PanelPreferences.customHeaderImageUri.collectAsState()
            val customHeaderImageHeight by PanelPreferences.customHeaderImageHeight.collectAsState()
            val customHeaderImageOpacity by PanelPreferences.customHeaderImageOpacity.collectAsState()

            ContainerCard(
                shape = RoundedCornerShape(0.dp),
                containerColor = solidOreoBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .then(shadeDragModifier)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    if (showCustomHeaderImage) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(customHeaderImageHeight.dp)
                        ) {
                            if (customHeaderImageUri.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = customHeaderImageUri,
                                    contentDescription = "Custom Header Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(alpha = customHeaderImageOpacity),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    com.example.ui.theme.getMiscElementColor(isDark).copy(alpha = 0.85f),
                                                    androidx.compose.material3.MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f),
                                                    androidx.compose.material3.MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(
                                start = 14.dp,
                                end = 14.dp,
                                top = if (showCustomHeaderImage) 8.dp else 10.dp,
                                bottom = 0.dp
                            )
                    ) {
                        val oreoBatteryInfo = rememberQqsBatteryState()
                        OreoTopStatusBar(
                            carrierName = carrierName.ifBlank { "simyo" },
                            batteryPercent = oreoBatteryInfo.percentage,
                            isBatteryCharging = oreoBatteryInfo.isCharging,
                            isDark = isDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val brightnessAtBottom by PanelPreferences.brightnessAtBottom.collectAsState()
                        val brightnessInQqs by PanelPreferences.brightnessInQqs.collectAsState()

                    if (isEditingTiles) {
                        EditModeTileStudio(
                            activeTiles = activeTiles,
                            inactiveTiles = inactiveTiles,
                            gridLayout = gridLayout,
                            editScrollState = editScrollState ?: rememberScrollState(),
                            onDragStateChange = onTileDragStateChange,
                            onTilesUpdated = onTilesReordered,
                            onDone = onToggleEditMode
                        )
                    } else if (morphProgress < 0.5f && !isLandscape) {
                        // Collapsed QQS View (Oreo Style)
                        if (brightnessInQqs && !brightnessAtBottom) {
                            BrightnessSliderRow(
                                brightness = brightness,
                                onBrightnessChange = onBrightnessChange,
                                sliderAlpha = 1f,
                                sliderHeight = 36.dp,
                                isAtBottom = false,
                                isQqs = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        val oQqsScrollState = rememberScrollState()
                        val qqsFadeBgColor = solidOreoBg

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (enableQqsHorizontalScroll) {
                                            Modifier.horizontalScroll(oQqsScrollState)
                                        } else Modifier
                                    )
                                    .offset(y = (-4).dp)
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = if (enableQqsHorizontalScroll) Arrangement.spacedBy(16.dp) else Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                qqsTiles.forEach { tile ->
                                    key(tile.id) {
                                        CircularQsTile(
                                            tile = tile,
                                            morphProgress = 0f,
                                            tileWidth = qqsTileSizeValue.dp,
                                            onClick = { onTileClick(tile) },
                                            onLongClick = { onTileLongClick(tile) }
                                        )
                                    }
                                }
                            }

                            if (enableQqsHorizontalScroll) {
                                if (oQqsScrollState.canScrollBackward) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .width(28.dp)
                                            .fillMaxHeight()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(qqsFadeBgColor, Color.Transparent)
                                                )
                                            )
                                    )
                                }
                                if (oQqsScrollState.canScrollForward) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .width(28.dp)
                                            .fillMaxHeight()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color.Transparent, qqsFadeBgColor)
                                                )
                                            )
                                    )
                                }
                            }
                        }

                        if (enableQqsComplicationsRow && qqs2x1Tiles.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Qqs2x1ComplicationsRow(
                                complications = qqs2x1Tiles,
                                isDark = isDark,
                                fadeBgColor = qqsFadeBgColor,
                                onTileClick = onTileClick,
                                onTileLongClick = onTileLongClick
                            )
                        }

                        if (brightnessInQqs && brightnessAtBottom) {
                            Spacer(modifier = Modifier.height(4.dp))
                            BrightnessSliderRow(
                                brightness = brightness,
                                onBrightnessChange = onBrightnessChange,
                                sliderAlpha = 1f,
                                sliderHeight = 36.dp,
                                isAtBottom = true,
                                isQqs = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 2.dp)
                                .layout { measurable, constraints ->
                                    val horizontalBleed = 14.dp.roundToPx()
                                    val targetWidth = constraints.maxWidth + horizontalBleed * 2
                                    val placeable = measurable.measure(
                                        constraints.copy(
                                            minWidth = targetWidth,
                                            maxWidth = targetWidth
                                        )
                                    )
                                    layout(constraints.maxWidth, placeable.height) {
                                        placeable.placeRelative(-horizontalBleed, 0)
                                    }
                                },
                            thickness = 1.dp,
                            color = if (isDark) Color(0x33FFFFFF) else Color(0x33000000)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val subheaderTextColor = if (isDark) Color.LightGray else Color(0xFF555555)
                        val subheaderIconTint = if (isMiscCustomizedOreo) com.example.ui.theme.getMiscElementColor(isDark) else subheaderTextColor
                        val datePosition by PanelPreferences.datePosition.collectAsState()
                        val showDate = datePosition != com.example.data.DatePosition.HIDDEN
                        val settingsIconLocation by PanelPreferences.settingsIconLocation.collectAsState()
                        val showSettingsIcon = settingsIconLocation != com.example.data.SettingsIconLocation.HIDDEN
                        val editButtonPosition by PanelPreferences.editButtonPosition.collectAsState()
                        val showEditPencil = editButtonPosition != com.example.data.EditButtonPosition.NONE

                        val currentDate = remember {
                            SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (showDate) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .offset(y = (-3).dp)
                                        .clip(RoundedCornerShape(dateBgCorner.dp))
                                        .then(
                                            if (dateBgEnabled) {
                                                Modifier
                                                    .background(statusBadgeBgColor)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            } else Modifier
                                        )
                                        .clickable {
                                            launchCalendarApp(context)
                                            onClosePanel()
                                        }
                                ) {
                                    Text(
                                        text = currentDate,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dateBgEnabled) statusBadgeContentColor else subheaderTextColor
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.offset(y = (-3).dp)
                            ) {
                                if (showSettingsIcon) {
                                    IconButton(
                                        onClick = onSettingsClick,
                                        modifier = Modifier.size(30.dp).testTag("oreo_qqs_settings_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = subheaderIconTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                if (showEditPencil) {
                                    IconButton(
                                        onClick = onToggleEditMode,
                                        modifier = Modifier.size(30.dp).testTag("oreo_qqs_edit_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Tiles",
                                            tint = subheaderIconTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = onExpandFull,
                                    modifier = Modifier.size(30.dp).testTag("oreo_qqs_expand_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Expand",
                                        tint = subheaderIconTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Expanded FULL QS View (Oreo Style)
                        if (!brightnessAtBottom) {
                            BrightnessSliderRow(
                                brightness = brightness,
                                onBrightnessChange = onBrightnessChange,
                                sliderAlpha = 1f,
                                sliderHeight = 38.dp,
                                isAtBottom = false,
                                isQqs = false
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        val maxRows = effectiveRows
                        val columns = effectiveColumns
                        val compositionContext = rememberCompositionContext()
                        val tileRowHeight = 96.dp
                        val viewPagerHeightDp = (tileRowHeight * maxRows) + (10.dp * (maxRows - 1).coerceAtLeast(0))

                        AndroidView(
                            factory = { ctx ->
                                ViewPager2(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    isSaveEnabled = false
                                    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                        override fun onPageScrolled(pos: Int, offset: Float, px: Int) {
                                            viewPagerScrollPosition = pos + offset
                                        }
                                        override fun onPageSelected(pos: Int) {
                                            viewPagerCurrentPage = pos
                                        }
                                    })
                                    adapter = TilePagerAdapter(
                                        compositionContext = compositionContext,
                                        tilePages = tilePages,
                                        columns = columns,
                                        maxRows = maxRows,
                                        placeholderTile = placeholderTile,
                                        onTileClick = onTileClick,
                                        onTileLongClick = onTileLongClick
                                    )
                                    viewPagerRef = this
                                }
                            },
                            update = { vp ->
                                viewPagerRef = vp
                                val adapter = vp.adapter as? TilePagerAdapter
                                adapter?.update(
                                    newTilePages = tilePages,
                                    newColumns = columns,
                                    newMaxRows = maxRows,
                                    newPlaceholderTile = placeholderTile,
                                    newOnTileClick = onTileClick,
                                    newOnTileLongClick = onTileLongClick
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(viewPagerHeightDp)
                        )

                        if (brightnessAtBottom) {
                            Spacer(modifier = Modifier.height(8.dp))
                            BrightnessSliderRow(
                                brightness = brightness,
                                onBrightnessChange = onBrightnessChange,
                                sliderAlpha = 1f,
                                sliderHeight = 38.dp,
                                isAtBottom = true,
                                isQqs = false
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        val footerTint = if (isDark) Color(0xFFCCCCCC) else Color(0xFF444444)
                        val footerIconTint = if (isMiscCustomizedOreo) com.example.ui.theme.getMiscElementColor(isDark) else footerTint
                        val shortDateStr = remember {
                            SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(Date())
                        }
                        val datePositionFull by PanelPreferences.datePosition.collectAsState()
                        val showDateFull = datePositionFull != com.example.data.DatePosition.HIDDEN
                        val settingsIconLocationFull by PanelPreferences.settingsIconLocation.collectAsState()
                        val showSettingsIconFull = settingsIconLocationFull != com.example.data.SettingsIconLocation.HIDDEN
                        val editButtonPositionFull by PanelPreferences.editButtonPosition.collectAsState()
                        val showEditPencilFull = editButtonPositionFull != com.example.data.EditButtonPosition.NONE

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (showDateFull) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .clip(RoundedCornerShape(dateBgCorner.dp))
                                        .then(
                                            if (dateBgEnabled) {
                                                Modifier
                                                    .background(statusBadgeBgColor)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            } else Modifier
                                        )
                                        .clickable {
                                            launchCalendarApp(context)
                                            onClosePanel()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = shortDateStr,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dateBgEnabled) statusBadgeContentColor else footerTint
                                    )
                                }
                            }

                            if (tilePages.size > 1) {
                                val pageIndicatorStyle by PanelPreferences.qsPageIndicatorStyle.collectAsState()
                                val piActiveLineThickness by PanelPreferences.piActiveLineThickness.collectAsState()
                                val piInactiveLineThickness by PanelPreferences.piInactiveLineThickness.collectAsState()
                                val piActiveLineWidth by PanelPreferences.piActiveLineWidth.collectAsState()
                                val piInactiveLineWidth by PanelPreferences.piInactiveLineWidth.collectAsState()
                                val piActiveDotRadius by PanelPreferences.piActiveDotRadius.collectAsState()
                                val piInactiveDotRadius by PanelPreferences.piInactiveDotRadius.collectAsState()
                                val piActiveSlidingThickness by PanelPreferences.piActiveSlidingThickness.collectAsState()
                                val piInactiveSlidingThickness by PanelPreferences.piInactiveSlidingThickness.collectAsState()
                                QsPageIndicator(
                                    pageCount = tilePages.size,
                                    scrollPosition = viewPagerScrollPosition,
                                    style = pageIndicatorStyle,
                                    activeColor = com.example.ui.theme.getMiscElementColor(isDark),
                                    inactiveColor = footerTint.copy(alpha = 0.35f),
                                    onPageClick = { pageIdx -> viewPagerRef?.setCurrentItem(pageIdx, true) },
                                    modifier = Modifier.align(Alignment.Center),
                                    activeLineThickness = piActiveLineThickness,
                                    inactiveLineThickness = piInactiveLineThickness,
                                    activeLineWidth = piActiveLineWidth,
                                    inactiveLineWidth = piInactiveLineWidth,
                                    activeDotRadius = piActiveDotRadius,
                                    inactiveDotRadius = piInactiveDotRadius,
                                    activeSlidingThickness = piActiveSlidingThickness,
                                    inactiveSlidingThickness = piInactiveSlidingThickness
                                )
                            }

                            val footerIconBgEnabled by PanelPreferences.footerIconBackground.collectAsState()
                            val footerOuterSpacingEnabled by PanelPreferences.footerOuterSpacing.collectAsState()
                            val footerOuterCorner by PanelPreferences.footerOuterCorner.collectAsState()
                            val footerInnerCorner by PanelPreferences.footerInnerCorner.collectAsState()
                            val footerSpacing by PanelPreferences.footerSpacing.collectAsState()
                            val footerBgColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
                            val inactiveOnTileColor = com.example.ui.theme.getInactiveTilesOnElementColor(isDark)
                            val resolvedOreoFooterIconTint = resolveFooterButtonIconTint(
                                footerIconBgEnabled = footerIconBgEnabled,
                                inactiveOnTileColor = inactiveOnTileColor,
                                defaultIconTint = footerIconTint
                            )
                            val oreoSpacingVal = if (footerIconBgEnabled) {
                                footerSpacing.dp
                            } else {
                                4.dp
                            }

                            val visibleOreoButtons = mutableListOf<String>()
                            if (showEditPencilFull) visibleOreoButtons.add("edit")
                            if (showSettingsIconFull) visibleOreoButtons.add("settings")
                            visibleOreoButtons.add("collapse")

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(oreoSpacingVal),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                visibleOreoButtons.forEachIndexed { idx, btnType ->
                                    val isFirst = (idx == 0)
                                    val isLast = (idx == visibleOreoButtons.size - 1)
                                    val outerCorner = footerOuterCorner.dp
                                    val innerCorner = footerInnerCorner.dp
                                    val btnShape = when {
                                        isFirst && isLast -> RoundedCornerShape(outerCorner)
                                        isFirst -> RoundedCornerShape(topStart = outerCorner, bottomStart = outerCorner, topEnd = innerCorner, bottomEnd = innerCorner)
                                        isLast -> RoundedCornerShape(topStart = innerCorner, bottomStart = innerCorner, topEnd = outerCorner, bottomEnd = outerCorner)
                                        else -> RoundedCornerShape(innerCorner)
                                    }

                                    val btnModifier = Modifier
                                        .size(32.dp)
                                        .clip(btnShape)
                                        .then(
                                            if (footerIconBgEnabled) Modifier.background(footerBgColor) else Modifier
                                        )

                                    when (btnType) {
                                        "edit" -> {
                                            Box(
                                                modifier = btnModifier
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = ripple(bounded = true)
                                                    ) { onToggleEditMode() }
                                                    .testTag("oreo_footer_edit_btn"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit Tiles",
                                                    tint = resolvedOreoFooterIconTint,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        "settings" -> {
                                            Box(
                                                modifier = btnModifier
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = ripple(bounded = true)
                                                    ) { onSettingsClick() }
                                                    .testTag("oreo_footer_settings_btn"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = "System Settings",
                                                    tint = resolvedOreoFooterIconTint,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        "collapse" -> {
                                            Box(
                                                modifier = btnModifier
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = ripple(bounded = true)
                                                    ) { onCollapseToQqs() }
                                                    .testTag("oreo_footer_collapse_btn"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowUp,
                                                    contentDescription = "Collapse",
                                                    tint = resolvedOreoFooterIconTint,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
            // UNIFIED QS CARD (Morphs between QQS and Full QS, or displays Edit Mode Studio)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(shadeDragModifier),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
            val panelGlobalCornerEnabled by PanelPreferences.panelGlobalCornerEnabled.collectAsState()
            val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
            val panelInnerCorner by PanelPreferences.panelInnerCorner.collectAsState()

            val effectiveOuterCorner = panelOuterCorner.dp
            val effectiveInnerCorner = (if (panelGlobalCornerEnabled) panelOuterCorner else panelInnerCorner).dp

            val sliderSyncAll by PanelPreferences.sliderSyncAll.collectAsState()
            val globalProgressStyle by PanelPreferences.progressBarStyle.collectAsState()
            val brightnessStyle by PanelPreferences.brightnessSliderStyle.collectAsState()
            val resolvedBrightnessStyle = if (sliderSyncAll) globalProgressStyle else brightnessStyle
            val isStraightOrWavyBrightness = resolvedBrightnessStyle == ProgressBarStyle.STRAIGHT || resolvedBrightnessStyle == ProgressBarStyle.WAVY

            val isQqsOnly = expansionStage == QsExpansionStage.QQS || (morphProgress < 0.5f && !isLandscape)
            val qqsContainerYOffset = 0.dp

            ContainerCard(
                shape = if (isSplitCarrier) {
                    RoundedCornerShape(
                        topStart = effectiveOuterCorner,
                        topEnd = effectiveOuterCorner,
                        bottomStart = effectiveInnerCorner,
                        bottomEnd = effectiveInnerCorner
                    )
                } else {
                    RoundedCornerShape(effectiveOuterCorner)
                },
                containerColor = com.example.ui.theme.getQsPanelBackgroundColor(isDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .offset(y = qqsContainerYOffset)
                    .then(shadeDragModifier)
            ) {
                val isQqsMode = morphProgress < 0.5f && !isLandscape
                val isBottomSliderInQqs = brightnessInQqs && brightnessAtBottom
                val shouldReduceQqsContainer = isQqsMode && hideQqsDragHandle && !moveCollapseToQqsFooter && !isInlineLineCarrier && (
                    !isSplitCarrier || isBottomSliderInQqs
                )

                val topPadding = if (isSplitCarrier) 16.dp else 12.dp
                val baseBottomPadding = if (isSplitCarrier) {
                    if (shouldReduceQqsContainer) 14.dp else 16.dp
                } else {
                    if (shouldReduceQqsContainer) 10.dp else 12.dp
                }
                val extraHeightForStraightOrWavy = if (isQqsOnly && isStraightOrWavyBrightness) {
                    if (isBottomSliderInQqs) (-16).dp else 0.dp
                } else 0.dp
                val rawBottomPadding = baseBottomPadding + extraHeightForStraightOrWavy
                val bottomPadding = rawBottomPadding.coerceAtLeast(0.dp)
                val overflowCropDp = if (rawBottomPadding < 0.dp) -rawBottomPadding else 0.dp

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .animateContentSize(
                            animationSpec = spring(
                                dampingRatio = 0.80f,
                                stiffness = 380f
                            )
                        )
                        .then(
                            if (overflowCropDp > 0.dp) {
                                Modifier.layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    val cropPx = overflowCropDp.roundToPx()
                                    val newHeight = (placeable.height - cropPx).coerceAtLeast(0)
                                    layout(placeable.width, newHeight) {
                                        placeable.placeRelative(0, 0)
                                    }
                                }
                            } else Modifier
                        )
                        .padding(start = 14.dp, end = 14.dp, top = topPadding, bottom = bottomPadding)
                ) {
                    if (isEditingTiles) {
                        EditModeTileStudio(
                            activeTiles = activeTiles,
                            inactiveTiles = inactiveTiles,
                            gridLayout = gridLayout,
                            editScrollState = editScrollState ?: rememberScrollState(),
                            onDragStateChange = onTileDragStateChange,
                            onTilesUpdated = onTilesReordered,
                            onDone = onToggleEditMode
                        )
                    } else {
                        // 0. Very top QQS Row (Date, Carrier, Right Battery / Settings)
                        val hideDateInExpanded by PanelPreferences.hideDateInExpanded.collectAsState()
                        val isExpandedQs = expansionStage == QsExpansionStage.FULL_QS || morphProgress > 0.5f
                        val isQqsDate = !(hideDateInExpanded && isExpandedQs) && datePosition == com.example.data.DatePosition.QQS_PANEL
                        val showInlineCarrier = isCarrierVisible && !isSplitCarrier && !isInlineLineCarrier
                        val batteryLocTop by PanelPreferences.batteryPercentageLocation.collectAsState()
                        val settingsLocTop by PanelPreferences.settingsIconLocation.collectAsState()
                        val showQqsRightBattery = batteryLocTop == com.example.data.BatteryPercentageLocation.QQS_RIGHT
                        val showInlineSettings = !isSplitCarrier && !isInlineLineCarrier && settingsLocTop == com.example.data.SettingsIconLocation.CARRIER_RIGHT

                        if (isQqsDate || showInlineCarrier || showQqsRightBattery || showInlineSettings) {
                            val dateYOffset = if (isExpandedQs) 2.dp else (-4).dp
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = dateYOffset)
                                    .padding(start = 2.dp, end = 2.dp, bottom = 8.dp)
                                    .testTag("qqs_panel_date_top")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val textColor = if (isDark) Color.LightGray.copy(alpha = 0.9f) else Color(0xFF333333)
                                    if (isQqsDate) {
                                        val currentDate = remember {
                                            val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                                            sdf.format(Date())
                                        }
                                        val resolvedDateColor = if (dateBgEnabled) statusBadgeContentColor else textColor
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(dateBgCorner.dp))
                                                .then(
                                                    if (dateBgEnabled) {
                                                        Modifier
                                                            .background(statusBadgeBgColor)
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    } else Modifier
                                                )
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = if (dateBgEnabled) ripple(bounded = true) else null,
                                                    onClick = {
                                                        launchCalendarApp(context)
                                                        onClosePanel()
                                                    }
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "Date",
                                                tint = resolvedDateColor,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = currentDate,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = resolvedDateColor
                                            )
                                        }
                                        if (showInlineCarrier && !dateBgEnabled && !carrierBgEnabled) {
                                            if (carrierDividerAsLine) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(1.dp)
                                                        .height(12.dp)
                                                        .background(textColor.copy(alpha = 0.35f))
                                                        .testTag("carrier_inline_divider_line")
                                                )
                                            } else {
                                                Text(
                                                    text = "•",
                                                    fontSize = 12.sp,
                                                    color = textColor.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                    if (showInlineCarrier) {
                                        val resolvedCarrierColor = if (carrierBgEnabled) statusBadgeContentColor else textColor
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(carrierBgCorner.dp))
                                                .then(
                                                    if (carrierBgEnabled) {
                                                        Modifier
                                                            .background(statusBadgeBgColor)
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    } else Modifier
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CellTower,
                                                contentDescription = null,
                                                tint = resolvedCarrierColor,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = carrierName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = resolvedCarrierColor
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val textColor = if (isDark) Color.LightGray.copy(alpha = 0.9f) else Color(0xFF333333)
                                    if (showQqsRightBattery) {
                                        val resolvedRightBatteryColor = if (batteryBgEnabled) statusBadgeContentColor else textColor
                                        val rightChargingTint = if (batteryBgEnabled && statusBadgeBgColor.luminance() > 0.5f) Color(0xFF2E7D32) else Color(0xFF4CAF50)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(batteryBgCorner.dp))
                                                .then(
                                                    if (batteryBgEnabled) {
                                                        Modifier
                                                            .background(statusBadgeBgColor)
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    } else Modifier
                                                )
                                                .testTag("qqs_right_battery_indicator")
                                        ) {
                                            Icon(
                                                imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                                                contentDescription = "Battery ${batteryInfo.percentage}%",
                                                tint = if (batteryInfo.isCharging) rightChargingTint else if (batteryInfo.percentage <= 15) Color(0xFFFF5252) else resolvedRightBatteryColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "${batteryInfo.percentage}%",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = resolvedRightBatteryColor
                                            )
                                        }
                                    }

                                    if (showInlineSettings) {
                                        IconButton(
                                            onClick = onSettingsClick,
                                            modifier = Modifier.size(24.dp).testTag("inline_settings_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "System Settings",
                                                tint = textColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 1. Brightness Slider (if not moved to bottom)
                        val showTopSlider = !brightnessAtBottom && (brightnessInQqs || isFullQs)
                        AnimatedVisibility(
                            visible = showTopSlider,
                            enter = expandVertically(spring(dampingRatio = 0.80f, stiffness = 380f)) + fadeIn(tween(200)),
                            exit = shrinkVertically(spring(dampingRatio = 0.80f, stiffness = 380f)) + fadeOut(tween(160))
                        ) {
                            val sliderHeight = 44.dp
                            BrightnessSliderRow(brightness, onBrightnessChange, 1f, sliderHeight, isAtBottom = false, isQqs = !isFullQs)
                        }

                        val spacerHeight = if (isSplitCarrier) 3.dp else 6.dp
                        Spacer(modifier = Modifier.height(spacerHeight))

                        // 2. Quick Toggles Grid & Horizontal Pager (Morphing container)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (isSplitCarrier) Modifier.offset(y = (-3).dp) else Modifier)
                                .animateContentSize(
                                    animationSpec = spring(
                                        dampingRatio = 0.80f,
                                        stiffness = 380f
                                    )
                                )
                        ) {
                            if (morphProgress < 0.5f && !isLandscape) {
                                // Pure QQS Single Row
                                val qqsAlpha = ((0.5f - morphProgress) / 0.5f).coerceIn(0f, 1f)
                                val qqsScale = 1f - (morphProgress * 0.08f)
                                val dQqsScrollState = rememberScrollState()
                                val qqsFadeBgColor = com.example.ui.theme.getQsPanelBackgroundColor(isDark)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            alpha = qqsAlpha
                                            scaleX = qqsScale
                                            scaleY = qqsScale
                                        },
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Min)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .then(
                                                    if (enableQqsHorizontalScroll) {
                                                        Modifier.horizontalScroll(dQqsScrollState)
                                                    } else Modifier
                                                ),
                                            horizontalArrangement = if (enableQqsHorizontalScroll) Arrangement.spacedBy(16.dp) else Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            qqsTiles.forEach { tile ->
                                                key(tile.id) {
                                                    CircularQsTile(
                                                        tile = tile,
                                                        morphProgress = morphProgress * 2f,
                                                        tileWidth = qqsTileSizeValue.dp,
                                                        onClick = { onTileClick(tile) },
                                                        onLongClick = { onTileLongClick(tile) }
                                                    )
                                                }
                                            }
                                        }

                                        if (enableQqsHorizontalScroll) {
                                            if (dQqsScrollState.canScrollBackward) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.CenterStart)
                                                        .width(28.dp)
                                                        .fillMaxHeight()
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                colors = listOf(qqsFadeBgColor, Color.Transparent)
                                                            )
                                                        )
                                                )
                                            }
                                            if (dQqsScrollState.canScrollForward) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.CenterEnd)
                                                        .width(28.dp)
                                                        .fillMaxHeight()
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                colors = listOf(Color.Transparent, qqsFadeBgColor)
                                                            )
                                                        )
                                                )
                                            }
                                        }
                                    }

                                    if (enableQqsComplicationsRow && qqs2x1Tiles.isNotEmpty()) {
                                        Qqs2x1ComplicationsRow(
                                            complications = qqs2x1Tiles,
                                            isDark = isDark,
                                            fadeBgColor = qqsFadeBgColor,
                                            onTileClick = onTileClick,
                                            onTileLongClick = onTileLongClick
                                        )
                                    }
                                }
                            } else {
                                // Full QS Multi-Column Grid
                                val gridAlpha = if (isLandscape) 1f else ((morphProgress - 0.5f) / 0.5f).coerceIn(0f, 1f)
                                val gridScale = if (isLandscape) 1f else 0.94f + (0.06f * gridAlpha)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            alpha = gridAlpha
                                            scaleX = gridScale
                                            scaleY = gridScale
                                        }
                                ) {
                                    val placeholderTile = remember {
                                        QsTileModel(
                                            id = "placeholder_slot",
                                            label = " ",
                                            subtitle = " ",
                                            state = TileState.INACTIVE,
                                            category = TileCategory.SYSTEM,
                                            isVisible = false
                                        )
                                    }
                                    val maxRows = effectiveRows
                                    val columns = effectiveColumns
                                    val compositionContext = rememberCompositionContext()
                                    val disableLabelsInExpanded by PanelPreferences.disableLabelsInExpanded.collectAsState()
                                    val tileRowHeight = if (disableLabelsInExpanded) 76.dp else 100.dp
                                    val viewPagerHeightDp = (tileRowHeight * maxRows) + (10.dp * (maxRows - 1).coerceAtLeast(0))

                                    AndroidView(
                                        factory = { ctx ->
                                            ViewPager2(ctx).apply {
                                                layoutParams = ViewGroup.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                                )
                                                isSaveEnabled = false
                                                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                                    override fun onPageScrolled(
                                                        position: Int,
                                                        positionOffset: Float,
                                                        positionOffsetPixels: Int
                                                    ) {
                                                        viewPagerScrollPosition = position + positionOffset
                                                    }

                                                    override fun onPageSelected(position: Int) {
                                                        viewPagerCurrentPage = position
                                                    }
                                                })
                                                adapter = TilePagerAdapter(
                                                    compositionContext = compositionContext,
                                                    tilePages = tilePages,
                                                    columns = columns,
                                                    maxRows = maxRows,
                                                    placeholderTile = placeholderTile,
                                                    onTileClick = onTileClick,
                                                    onTileLongClick = onTileLongClick
                                                )
                                                viewPagerRef = this
                                            }
                                        },
                                        update = { vp ->
                                            viewPagerRef = vp
                                            val adapter = vp.adapter as? TilePagerAdapter
                                            adapter?.update(
                                                newTilePages = tilePages,
                                                newColumns = columns,
                                                newMaxRows = maxRows,
                                                newPlaceholderTile = placeholderTile,
                                                newOnTileClick = onTileClick,
                                                newOnTileLongClick = onTileLongClick
                                            )
                                            if (vp.currentItem >= tilePages.size && tilePages.isNotEmpty()) {
                                                vp.setCurrentItem(tilePages.size - 1, false)
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(viewPagerHeightDp)
                                    )
                                }
                            }
                        }

                        // Bottom Brightness Slider (if moved to bottom)
                        val showBottomSlider = brightnessAtBottom && (brightnessInQqs || isFullQs)
                        AnimatedVisibility(
                            visible = showBottomSlider,
                            enter = expandVertically(spring(dampingRatio = 0.80f, stiffness = 380f)) + fadeIn(tween(200)),
                            exit = shrinkVertically(spring(dampingRatio = 0.80f, stiffness = 380f)) + fadeOut(tween(160))
                        ) {
                            val sliderHeight = 44.dp
                            val bottomSpacerHeight = 10.dp
                            Column {
                                Spacer(modifier = Modifier.height(bottomSpacerHeight))
                                BrightnessSliderRow(brightness, onBrightnessChange, 1f, sliderHeight, isAtBottom = true, verticalOffset = 0.dp, isQqs = !isFullQs)
                            }
                        }

                        if (!isSplitCarrier) {
                            val bottomSpacerHeight = if (isAndroidOreoStyle) 0.dp else 10.dp
                            if (bottomSpacerHeight > 0.dp) {
                                Spacer(modifier = Modifier.height(bottomSpacerHeight))
                            }
                        }

                        // 3. Footer Bar / Drag Pill (Morphing footer)
                        val footerTint = if (isDark) Color(0xFFCCCCCC) else Color(0xFF333333)
                        val editPos by PanelPreferences.editButtonPosition.collectAsState()
                        val footerIconBgEnabled by PanelPreferences.footerIconBackground.collectAsState()
                        val footerOuterSpacingEnabled by PanelPreferences.footerOuterSpacing.collectAsState()
                        val footerOuterCorner by PanelPreferences.footerOuterCorner.collectAsState()
                        val footerInnerCorner by PanelPreferences.footerInnerCorner.collectAsState()
                        val footerSpacing by PanelPreferences.footerSpacing.collectAsState()
                        val footerBgColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
                        val inactiveOnTileColor = com.example.ui.theme.getInactiveTilesOnElementColor(isDark)
                        val resolvedFooterIconTint = resolveFooterButtonIconTint(
                            footerIconBgEnabled = footerIconBgEnabled,
                            inactiveOnTileColor = inactiveOnTileColor,
                            defaultIconTint = footerTint
                        )
                        val qqsSettingsLoc by PanelPreferences.settingsIconLocation.collectAsState()

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (morphProgress < 0.5f && !isLandscape) {
                                val qqsFooterAlpha = ((0.5f - morphProgress) / 0.5f).coerceIn(0f, 1f)
                                if (isInlineLineCarrier) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .graphicsLayer { alpha = qqsFooterAlpha }
                                    ) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        HorizontalDivider(
                                            thickness = 1.dp,
                                            color = if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.12f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .layout { measurable, constraints ->
                                                    val horizontalBleed = 14.dp.roundToPx()
                                                    val targetWidth = constraints.maxWidth + horizontalBleed * 2
                                                    val placeable = measurable.measure(
                                                        constraints.copy(
                                                            minWidth = targetWidth,
                                                            maxWidth = targetWidth
                                                        )
                                                    )
                                                    layout(constraints.maxWidth, placeable.height) {
                                                        placeable.placeRelative(-horizontalBleed, 0)
                                                    }
                                                }
                                                .testTag("carrier_divider_line")
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier
                                                    .align(Alignment.CenterStart)
                                                    .clip(RoundedCornerShape(carrierBgCorner.dp))
                                                    .then(
                                                        if (carrierBgEnabled) {
                                                            Modifier
                                                                .background(statusBadgeBgColor)
                                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                                        } else Modifier
                                                    )
                                            ) {
                                                val defaultCarrierColor = if (isDark) Color.LightGray.copy(alpha = 0.9f) else Color(0xFF333333)
                                                val carrierColor = if (carrierBgEnabled) statusBadgeContentColor else defaultCarrierColor
                                                Icon(
                                                    imageVector = Icons.Default.CellTower,
                                                    contentDescription = null,
                                                    tint = carrierColor,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = carrierName,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = carrierColor
                                                )
                                            }

                                            val inlineActionButtons = mutableListOf<String>()
                                            if (moveCollapseToQqsFooter) inlineActionButtons.add("collapse")
                                            if (qqsSettingsLoc == com.example.data.SettingsIconLocation.CARRIER_RIGHT) inlineActionButtons.add("settings")

                                            if (inlineActionButtons.isNotEmpty()) {
                                                val spacingVal = if (footerIconBgEnabled) footerSpacing.dp else 4.dp
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(spacingVal),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.align(Alignment.CenterEnd)
                                                ) {
                                                    inlineActionButtons.forEachIndexed { idx, btnType ->
                                                        val isFirst = (idx == 0)
                                                        val isLast = (idx == inlineActionButtons.size - 1)
                                                        val outerCorner = footerOuterCorner.dp
                                                        val innerCorner = footerInnerCorner.dp
                                                        val btnShape = when {
                                                            isFirst && isLast -> RoundedCornerShape(outerCorner)
                                                            isFirst -> RoundedCornerShape(topStart = outerCorner, bottomStart = outerCorner, topEnd = innerCorner, bottomEnd = innerCorner)
                                                            isLast -> RoundedCornerShape(topStart = innerCorner, bottomStart = innerCorner, topEnd = outerCorner, bottomEnd = outerCorner)
                                                            else -> RoundedCornerShape(innerCorner)
                                                        }
                                                        val btnMod = Modifier
                                                            .size(32.dp)
                                                            .clip(btnShape)
                                                            .then(if (footerIconBgEnabled) Modifier.background(footerBgColor) else Modifier)

                                                        when (btnType) {
                                                            "collapse" -> {
                                                                Box(
                                                                    modifier = btnMod
                                                                        .clickable(
                                                                            interactionSource = remember { MutableInteractionSource() },
                                                                            indication = ripple(bounded = true)
                                                                        ) { onExpandFull() }
                                                                        .testTag("qqs_footer_collapse_btn"),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.KeyboardArrowDown,
                                                                        contentDescription = "Expand",
                                                                        tint = resolvedFooterIconTint,
                                                                        modifier = Modifier.size(18.dp)
                                                                    )
                                                                }
                                                            }
                                                            "settings" -> {
                                                                Box(
                                                                    modifier = btnMod
                                                                        .clickable(
                                                                            interactionSource = remember { MutableInteractionSource() },
                                                                            indication = ripple(bounded = true)
                                                                        ) { onSettingsClick() }
                                                                        .testTag("carrier_settings_button"),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Settings,
                                                                        contentDescription = "System Settings",
                                                                        tint = resolvedFooterIconTint,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            if (!hideQqsDragHandle) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.Center)
                                                        .offset(y = 12.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = null,
                                                            onClick = onExpandFull
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val dragColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color(0xFF333333)
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = dragHandleLength.dp, height = 3.dp)
                                                            .clip(CircleShape)
                                                            .background(dragColor)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(if (moveCollapseToQqsFooter && !isSplitCarrier) Modifier.defaultMinSize(minHeight = 32.dp) else Modifier)
                                            .graphicsLayer { alpha = qqsFooterAlpha }
                                    ) {
                                        if (moveCollapseToQqsFooter && !isSplitCarrier) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .align(Alignment.CenterStart)
                                                    .clip(RoundedCornerShape(footerOuterCorner.dp))
                                                    .then(
                                                        if (footerIconBgEnabled) Modifier.background(footerBgColor) else Modifier
                                                    )
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = ripple(bounded = true)
                                                    ) { onExpandFull() }
                                                    .testTag("qqs_footer_collapse_btn"),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.KeyboardArrowDown,
                                                    contentDescription = "Expand",
                                                    tint = resolvedFooterIconTint,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        if (!hideQqsDragHandle && !isSplitCarrier) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .padding(bottom = 0.dp)
                                                    .offset(y = if (moveCollapseToQqsFooter) 0.dp else 3.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                        onClick = onExpandFull
                                                    )
                                                    .padding(vertical = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val dragColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color(0xFF333333)
                                                Box(
                                                    modifier = Modifier
                                                        .size(width = dragHandleLength.dp, height = 3.dp)
                                                        .clip(CircleShape)
                                                        .background(dragColor)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                val fullQsFooterAlpha = if (isLandscape) 1f else ((morphProgress - 0.5f) / 0.5f).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer { alpha = fullQsFooterAlpha }
                                    .padding(vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .align(Alignment.CenterStart)
                                        .clip(RoundedCornerShape(footerOuterCorner.dp))
                                        .then(
                                            if (footerIconBgEnabled) Modifier.background(footerBgColor) else Modifier
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true)
                                        ) { onCollapseToQqs() }
                                        .testTag("collapse_to_qqs_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Collapse",
                                        tint = resolvedFooterIconTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Page Indicator
                                if (tilePages.size > 1) {
                                    val pageIndicatorStyle by PanelPreferences.qsPageIndicatorStyle.collectAsState()
                                    val piActiveLineThickness by PanelPreferences.piActiveLineThickness.collectAsState()
                                    val piInactiveLineThickness by PanelPreferences.piInactiveLineThickness.collectAsState()
                                    val piActiveLineWidth by PanelPreferences.piActiveLineWidth.collectAsState()
                                    val piInactiveLineWidth by PanelPreferences.piInactiveLineWidth.collectAsState()
                                    val piActiveDotRadius by PanelPreferences.piActiveDotRadius.collectAsState()
                                    val piInactiveDotRadius by PanelPreferences.piInactiveDotRadius.collectAsState()
                                    val piActiveSlidingThickness by PanelPreferences.piActiveSlidingThickness.collectAsState()
                                    val piInactiveSlidingThickness by PanelPreferences.piInactiveSlidingThickness.collectAsState()
                                    QsPageIndicator(
                                        pageCount = tilePages.size,
                                        scrollPosition = viewPagerScrollPosition,
                                        style = pageIndicatorStyle,
                                        activeColor = com.example.ui.theme.getMiscElementColor(isDark),
                                        inactiveColor = footerTint.copy(alpha = 0.35f),
                                        onPageClick = { pageIdx -> viewPagerRef?.setCurrentItem(pageIdx, true) },
                                        modifier = Modifier.align(Alignment.Center),
                                        activeLineThickness = piActiveLineThickness,
                                        inactiveLineThickness = piInactiveLineThickness,
                                        activeLineWidth = piActiveLineWidth,
                                        inactiveLineWidth = piInactiveLineWidth,
                                        activeDotRadius = piActiveDotRadius,
                                        inactiveDotRadius = piInactiveDotRadius,
                                        activeSlidingThickness = piActiveSlidingThickness,
                                        inactiveSlidingThickness = piInactiveSlidingThickness
                                    )
                                }

                                val spacingVal = if (footerIconBgEnabled) {
                                    footerSpacing.dp
                                } else {
                                    4.dp
                                }

                                val visibleFooterButtons = mutableListOf<String>()
                                if (editPos == EditButtonPosition.FOOTER || editPos == EditButtonPosition.BOTH) {
                                    visibleFooterButtons.add("edit")
                                }
                                val settingsLocFooter by PanelPreferences.settingsIconLocation.collectAsState()
                                if (settingsLocFooter == com.example.data.SettingsIconLocation.EXPANDED_QS) {
                                    visibleFooterButtons.add("settings")
                                }
                                visibleFooterButtons.add("power")

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(spacingVal),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    visibleFooterButtons.forEachIndexed { idx, btnType ->
                                        val isFirst = (idx == 0)
                                        val isLast = (idx == visibleFooterButtons.size - 1)
                                        val outerCorner = footerOuterCorner.dp
                                        val innerCorner = footerInnerCorner.dp
                                        val btnShape = when {
                                            isFirst && isLast -> RoundedCornerShape(outerCorner)
                                            isFirst -> RoundedCornerShape(topStart = outerCorner, bottomStart = outerCorner, topEnd = innerCorner, bottomEnd = innerCorner)
                                            isLast -> RoundedCornerShape(topStart = innerCorner, bottomStart = innerCorner, topEnd = outerCorner, bottomEnd = outerCorner)
                                            else -> RoundedCornerShape(innerCorner)
                                        }

                                        val btnModifier = Modifier
                                            .size(32.dp)
                                            .clip(btnShape)
                                            .then(
                                                if (footerIconBgEnabled) Modifier.background(footerBgColor) else Modifier
                                            )

                                        when (btnType) {
                                            "edit" -> {
                                                Box(
                                                    modifier = btnModifier
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = ripple(bounded = true)
                                                        ) { onToggleEditMode() }
                                                        .testTag("footer_edit_tiles_btn"),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit Tiles",
                                                        tint = resolvedFooterIconTint,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            "settings" -> {
                                                Box(
                                                    modifier = btnModifier
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = ripple(bounded = true)
                                                        ) { onSettingsClick() }
                                                        .testTag("footer_settings_button"),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Settings,
                                                        contentDescription = "System Settings",
                                                        tint = resolvedFooterIconTint,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            "power" -> {
                                                Box(
                                                    modifier = btnModifier
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = ripple(bounded = true)
                                                        ) {
                                                            val opened = QuickPanelAccessibilityService.openPowerDialog()
                                                            if (!opened) {
                                                                coroutineScope.launch(Dispatchers.IO) {
                                                                    try {
                                                                        val engine = com.example.system.NativeTileManager.instance?.privilegeEngine ?: com.example.system.PrivilegeEngine(context)
                                                                        engine.executeCommand("cmd statusbar power-menu || input keyevent --longpress 26")
                                                                    } catch (e: Exception) {}
                                                                }
                                                            }
                                                            onClosePanel()
                                                        }
                                                        .testTag("footer_power_button"),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PowerSettingsNew,
                                                        contentDescription = "Power Menu",
                                                        tint = resolvedFooterIconTint,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showSplitCarrierCard) {
            val panelGlobalCornerEnabled by PanelPreferences.panelGlobalCornerEnabled.collectAsState()
            val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
            val panelInnerCorner by PanelPreferences.panelInnerCorner.collectAsState()

            val effectiveOuterCorner = panelOuterCorner.dp
            val effectiveInnerCorner = (if (panelGlobalCornerEnabled) panelOuterCorner else panelInnerCorner).dp

            val carrierCardAlpha = if (isSplitCarrierConfigured) {
                (1f - morphProgress * 1.5f).coerceIn(0f, 1f)
            } else 1f

            ContainerCard(
                shape = RoundedCornerShape(
                    topStart = effectiveInnerCorner,
                    topEnd = effectiveInnerCorner,
                    bottomStart = effectiveOuterCorner,
                    bottomEnd = effectiveOuterCorner
                ),
                containerColor = com.example.ui.theme.getQsPanelBackgroundColor(isDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = qqsContainerYOffset)
                    .graphicsLayer { alpha = carrierCardAlpha }
                    .then(shadeDragModifier)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clip(RoundedCornerShape(carrierBgCorner.dp))
                            .then(
                                if (carrierBgEnabled) {
                                    Modifier
                                        .background(statusBadgeBgColor)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                } else Modifier
                            )
                    ) {
                        val defaultCarrierColor = if (isDark) Color.LightGray.copy(alpha = 0.9f) else Color(0xFF333333)
                        val carrierColor = if (carrierBgEnabled) statusBadgeContentColor else defaultCarrierColor
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = null,
                            tint = carrierColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = carrierName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = carrierColor
                        )
                    }

                    val settingsLocCarrier by PanelPreferences.settingsIconLocation.collectAsState()
                    val splitActionButtons = mutableListOf<String>()
                    if (moveCollapseToQqsFooter) splitActionButtons.add("collapse")
                    if (settingsLocCarrier == com.example.data.SettingsIconLocation.CARRIER_RIGHT) splitActionButtons.add("settings")

                    if (splitActionButtons.isNotEmpty()) {
                        val footerIconBgEnabled by PanelPreferences.footerIconBackground.collectAsState()
                        val footerOuterCorner by PanelPreferences.footerOuterCorner.collectAsState()
                        val footerInnerCorner by PanelPreferences.footerInnerCorner.collectAsState()
                        val footerSpacing by PanelPreferences.footerSpacing.collectAsState()
                        val footerBgColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
                        val inactiveOnTileColor = com.example.ui.theme.getInactiveTilesOnElementColor(isDark)
                        val defaultIconColor = if (isDark) Color.LightGray.copy(alpha = 0.9f) else Color(0xFF333333)
                        val resolvedIconColor = resolveFooterButtonIconTint(
                            footerIconBgEnabled = footerIconBgEnabled,
                            inactiveOnTileColor = inactiveOnTileColor,
                            defaultIconTint = defaultIconColor
                        )
                        val spacingVal = if (footerIconBgEnabled) footerSpacing.dp else 4.dp

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(spacingVal),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            splitActionButtons.forEachIndexed { idx, btnType ->
                                val isFirst = (idx == 0)
                                val isLast = (idx == splitActionButtons.size - 1)
                                val outerCorner = footerOuterCorner.dp
                                val innerCorner = footerInnerCorner.dp
                                val btnShape = when {
                                    isFirst && isLast -> RoundedCornerShape(outerCorner)
                                    isFirst -> RoundedCornerShape(topStart = outerCorner, bottomStart = outerCorner, topEnd = innerCorner, bottomEnd = innerCorner)
                                    isLast -> RoundedCornerShape(topStart = innerCorner, bottomStart = innerCorner, topEnd = outerCorner, bottomEnd = outerCorner)
                                    else -> RoundedCornerShape(innerCorner)
                                }
                                val btnMod = Modifier
                                    .size(if (footerIconBgEnabled || moveCollapseToQqsFooter) 32.dp else 28.dp)
                                    .clip(btnShape)
                                    .then(if (footerIconBgEnabled) Modifier.background(footerBgColor) else Modifier)

                                when (btnType) {
                                    "collapse" -> {
                                        Box(
                                            modifier = btnMod
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = ripple(bounded = true)
                                                ) { onExpandFull() }
                                                .testTag("qqs_footer_collapse_btn"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Expand",
                                                tint = resolvedIconColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    "settings" -> {
                                        Box(
                                            modifier = btnMod
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = ripple(bounded = true)
                                                ) { onSettingsClick() }
                                                .testTag("carrier_settings_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = "System Settings",
                                                tint = resolvedIconColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!hideQqsDragHandle) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(y = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onExpandFull
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val dragColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color(0xFF333333)
                            Box(
                                modifier = Modifier
                                    .size(width = dragHandleLength.dp, height = 3.dp)
                                    .clip(CircleShape)
                                    .background(dragColor)
                            )
                        }
                    }
                }
            }
        }
        }
    }
    }

    val notificationsContent: @Composable () -> Unit = {
        NotificationsCard(
            notifications = notifications,
            isPermissionGranted = isNotificationPermissionGranted,
            expansionStage = expansionStage,
            onNotificationClick = onNotificationClick,
            onNotificationDismiss = onNotificationDismiss,
            onActionClick = onNotificationActionClick,
            onClearAll = onDismissAllNotifications,
            onRequestPermission = onRequestNotificationAccess,
            onNavigateToNotificationHub = onNavigateToNotificationHub,
            onOpenNotificationManager = onOpenNotificationManager,
            onStageChange = { stage ->
                when (stage) {
                    QsExpansionStage.FULL_QS -> onExpandFull()
                    QsExpansionStage.COLLAPSED -> onClosePanel()
                    QsExpansionStage.QQS -> onCollapseToQqs()
                }
            }
        )
    }

    if (isLandscape && !isEditingTiles) {
        // LANDSCAPE SPLIT SHADE: Left side Quick Settings + Media, Right side Notifications List
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // LEFT COLUMN (Quick Settings & Media Player)
            Column(
                modifier = Modifier
                    .weight(1.08f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing)
            ) {
                qsCardContent()
                if (!isAndroidMarshmallowStyle) {
                    mediaCardContent()
                }
            }

            // RIGHT COLUMN (Scrollable Notifications)
            Column(
                modifier = Modifier
                    .weight(0.92f)
                    .fillMaxHeight()
            ) {
                notificationsContent()
            }
        }
    } else {
        // PORTRAIT / EDIT MODE (Single Column Vertical Stack)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isFullQs && !isEditingTiles) {
                        Modifier.wrapContentHeight()
                    } else {
                        Modifier.fillMaxHeight()
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(if (isAndroidOreoStyle || isAndroidMarshmallowStyle) 0.dp else ExpressiveCardShapes.CardSpacing)
        ) {
            qsCardContent()
            if (!isAndroidMarshmallowStyle) {
                mediaCardContent()
            }

            // 4. SCROLLABLE NOTIFICATIONS LIST (Shown in QQS, smoothly morphs & glides away when expanding to Full QS)
            val isNotificationsVisible = !isFullQs && !isEditingTiles
            AnimatedVisibility(
                visible = isNotificationsVisible,
                enter = fadeIn(animationSpec = tween(220)) + expandVertically(
                    animationSpec = spring(dampingRatio = 0.80f, stiffness = 380f)
                ),
                exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(
                    animationSpec = spring(dampingRatio = 0.80f, stiffness = 380f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isNotificationsVisible) Modifier.weight(1f, fill = true) else Modifier)
                    .then(if (isAndroidMarshmallowStyle && !isFullQs) Modifier.padding(top = 8.dp) else Modifier)
                    .clipToBounds()
            ) {
                notificationsContent()
            }
        }
    }
}

private class TilePageViewHolder(val composeView: ComposeView) : RecyclerView.ViewHolder(composeView)

private class TilePagerAdapter(
    private val compositionContext: androidx.compose.runtime.CompositionContext,
    private var tilePages: List<List<QsTileModel>>,
    private var columns: Int,
    private var maxRows: Int,
    private var placeholderTile: QsTileModel,
    private var onTileClick: (QsTileModel) -> Unit,
    private var onTileLongClick: (QsTileModel) -> Unit
) : RecyclerView.Adapter<TilePageViewHolder>() {

    fun update(
        newTilePages: List<List<QsTileModel>>,
        newColumns: Int,
        newMaxRows: Int,
        newPlaceholderTile: QsTileModel,
        newOnTileClick: (QsTileModel) -> Unit,
        newOnTileLongClick: (QsTileModel) -> Unit
    ) {
        this.tilePages = newTilePages
        this.columns = newColumns
        this.maxRows = newMaxRows
        this.placeholderTile = newPlaceholderTile
        this.onTileClick = newOnTileClick
        this.onTileLongClick = newOnTileLongClick
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = tilePages.size.coerceAtLeast(1)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TilePageViewHolder {
        val composeView = ComposeView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setParentCompositionContext(compositionContext)
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
        }
        return TilePageViewHolder(composeView)
    }

    override fun onBindViewHolder(holder: TilePageViewHolder, position: Int) {
        val pageTiles = tilePages.getOrElse(position) { emptyList() }
        val currentColumns = columns
        val currentMaxRows = maxRows
        val currentPlaceholder = placeholderTile
        val currentOnClick = onTileClick
        val currentOnLongClick = onTileLongClick

        holder.composeView.setContent {
            TilePageGrid(
                pageTiles = pageTiles,
                columns = currentColumns,
                maxRows = currentMaxRows,
                placeholderTile = currentPlaceholder,
                onTileClick = currentOnClick,
                onTileLongClick = currentOnLongClick
            )
        }
    }
}

@Composable
private fun TilePageGrid(
    pageTiles: List<QsTileModel>,
    columns: Int,
    maxRows: Int,
    placeholderTile: QsTileModel,
    onTileClick: (QsTileModel) -> Unit,
    onTileLongClick: (QsTileModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = pageTiles.chunked(columns)
    val targetTileWidth = when (columns) {
        2 -> 130.dp
        3 -> 106.dp
        else -> 74.dp
    }
    val disableLabelsInExpanded by PanelPreferences.disableLabelsInExpanded.collectAsState()
    val isCompactColumns = columns == 3 || columns == 4

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (disableLabelsInExpanded && isCompactColumns) 12.dp else 10.dp)
    ) {
        for (rowIndex in 0 until maxRows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (disableLabelsInExpanded && isCompactColumns) Modifier.height(72.dp) else Modifier),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = if (disableLabelsInExpanded && isCompactColumns) Alignment.CenterVertically else Alignment.Top
            ) {
                if (rowIndex < rows.size) {
                    val rowTiles = rows[rowIndex]
                    rowTiles.forEach { tile ->
                        key(tile.id) {
                            CircularQsTile(
                                tile = tile,
                                morphProgress = 1f,
                                tileWidth = targetTileWidth,
                                columns = columns,
                                onClick = { onTileClick(tile) },
                                onLongClick = { onTileLongClick(tile) }
                            )
                        }
                    }
                    repeat(columns - rowTiles.size) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer { alpha = 0f }
                        ) {
                            CircularQsTile(
                                tile = placeholderTile,
                                morphProgress = 1f,
                                tileWidth = targetTileWidth,
                                columns = columns,
                                isInteractive = false
                            )
                        }
                    }
                } else {
                    repeat(columns) {
                        Box(
                            modifier = Modifier
                                .graphicsLayer { alpha = 0f }
                        ) {
                            CircularQsTile(
                                tile = placeholderTile,
                                morphProgress = 1f,
                                tileWidth = targetTileWidth,
                                columns = columns,
                                isInteractive = false
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Notifications Section: Native XML & RecyclerView layout for ultra-smooth 120fps scrolling and swiping.
 */
@Composable
fun NotificationsCard(
    notifications: List<NotificationItem>,
    isPermissionGranted: Boolean,
    expansionStage: QsExpansionStage = QsExpansionStage.QQS,
    onNotificationClick: (NotificationItem) -> Unit,
    onNotificationDismiss: (NotificationItem) -> Unit,
    onActionClick: (NotificationActionModel) -> Unit,
    onClearAll: () -> Unit,
    onRequestPermission: () -> Unit,
    onNavigateToNotificationHub: () -> Unit = {},
    onOpenNotificationManager: () -> Unit = onNavigateToNotificationHub,
    onStageChange: (QsExpansionStage) -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current
    val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.toArgb()
    val notifBgColor = com.example.ui.theme.getQsNotificationContainerColor(isDark)
    val notifBgColorInt = notifBgColor.toArgb()

    // Observe Clear All FAB preferences for reactive live updates
    val clearAllFabOffsetX by PanelPreferences.clearAllFabOffsetX.collectAsState()
    val clearAllFabOffsetY by PanelPreferences.clearAllFabOffsetY.collectAsState()
    val useClearAllFab by PanelPreferences.useClearAllFab.collectAsState()
    val clearAllFabPos by PanelPreferences.clearAllFabPosition.collectAsState()
    val clearAllFabShapeId by PanelPreferences.clearAllFabShapeId.collectAsState()
    val clearAllFabIcon by PanelPreferences.clearAllFabIcon.collectAsState()
    val clearAllFabSize by PanelPreferences.clearAllFabSize.collectAsState()
    val disableClearAllBg by PanelPreferences.disableClearAllBackground.collectAsState()

    val hostView = androidx.compose.ui.platform.LocalView.current
    val panelRootView = remember(hostView) {
        var parent = hostView.parent
        while (parent is android.view.View && parent.parent is android.view.View) {
            parent = parent.parent
        }
        parent as? android.view.View ?: hostView
    }

    // Observe Notification Category Sorting preferences for live updates in shade
    val categorySortingEnabled by com.example.data.NotificationCategoryManager.isSortingEnabled.collectAsState()
    val customCategories by com.example.data.NotificationCategoryManager.categories.collectAsState()
    val categoryExpandedStates by com.example.data.NotificationCategoryManager.expandedStates.collectAsState()

    val showMediaInNotifContainer by PanelPreferences.showMediaInNotificationContainer.collectAsState()
    val mediaSessionsList by com.example.service.MediaSessionManagerHelper.sessionsList.collectAsState()
    val forceShowMediaPreview by PanelPreferences.forceShowMediaPreview.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val activeMediaSessions = remember(showMediaInNotifContainer, mediaSessionsList, forceShowMediaPreview) {
        if (showMediaInNotifContainer) {
            if (mediaSessionsList.isNotEmpty()) {
                mediaSessionsList
            } else if (forceShowMediaPreview) {
                listOf(MediaState.Preview)
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .testTag("notifications_card")
    ) {
        AndroidView(
            factory = { ctx ->
                com.example.ui.notification.NotificationListView(ctx).apply {
                    clipChildren = true
                    clipToPadding = true
                    this.onNotificationClick = onNotificationClick
                    this.onNotificationDismiss = onNotificationDismiss
                    this.onActionClick = onActionClick
                    this.onClearAll = onClearAll
                    this.onRequestPermission = onRequestPermission
                    this.onOpenNotificationManager = onOpenNotificationManager
                    this.rootPanelView = panelRootView
                    this.onStageChange = onStageChange
                    this.currentExpansionStage = expansionStage
                }
            },
            update = { view ->
                // Read category state flows inside update block so Compose re-invokes updateData when categories change
                val _catSync = Triple(categorySortingEnabled, customCategories, categoryExpandedStates)
                view.clipChildren = true
                view.clipToPadding = true
                view.onNotificationClick = onNotificationClick
                view.onNotificationDismiss = onNotificationDismiss
                view.onActionClick = onActionClick
                view.onClearAll = onClearAll
                view.onRequestPermission = onRequestPermission
                view.onOpenNotificationManager = onOpenNotificationManager
                view.rootPanelView = panelRootView
                view.onStageChange = onStageChange
                view.currentExpansionStage = expansionStage
                view.updateData(
                    notifications = notifications,
                    isPermissionGranted = isPermissionGranted,
                    isDark = isDark,
                    primaryColor = primaryColor,
                    panelCardBackgroundColor = notifBgColorInt,
                    mediaSessions = activeMediaSessions
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        )
    }
}

@Composable
fun NotificationHeaderRow(
    isDark: Boolean,
    primaryColor: Int,
    onOpenNotificationManager: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val disableNotifMonet by PanelPreferences.disableMonetNotifText.collectAsState()
    val showMonetSurface by PanelPreferences.showMonetSurfaceBackground.collectAsState()

    val defaultHeaderColorInt = if (disableNotifMonet) {
        if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK
    } else {
        primaryColor
    }

    val headerLabelColorInt = if (!showMonetSurface) {
        android.graphics.Color.WHITE
    } else {
        defaultHeaderColorInt
    }

    val headerColor = androidx.compose.ui.graphics.Color(headerLabelColorInt)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_notif_bell),
                contentDescription = "Notifications",
                tint = headerColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "NOTIFICATIONS",
                color = headerColor,
                fontSize = 11.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                letterSpacing = 0.08.sp
            )
        }
        IconButton(
            onClick = { onOpenNotificationManager() },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_notif_tune),
                contentDescription = "Notification Settings",
                tint = headerColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Individual Notification Item View (Direct solid card with horizontal swipe-to-dismiss & smart expansion)
 */
@Composable
fun NotificationItemRow(
    item: NotificationItem,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    onActionClick: (NotificationActionModel) -> Unit,
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
) {
    val isDark = LocalIsDarkTheme.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val disablePanelAnimations by com.example.data.PanelPreferences.disablePanelAnimations.collectAsState()
    var offsetX by remember(item.key) { mutableFloatStateOf(0f) }
    var isDismissed by remember(item.key) { mutableStateOf(false) }
    var isExpanded by remember(item.key) { mutableStateOf(false) }
    var showMenu by remember(item.key) { mutableStateOf(false) }

    val isExpandable = remember(item) {
        item.actions.isNotEmpty() || !item.subText.isNullOrBlank() || item.text.length > 40 || item.bigPictureBitmap != null || item.hasProgress
    }

    val resolvedAppName = item.appName.ifBlank { item.packageName }
    val timeFormatted = remember(item.postTime) {
        val date = java.util.Date(item.postTime)
        val format = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        format.format(date)
    }

    val isAndroidOreoStyle by PanelPreferences.isAndroidOreoStyle.collectAsState()
    val isAndroidMarshmallowStyle by PanelPreferences.isAndroidMarshmallowStyle.collectAsState()
    val isFlatLegacyStyle = isAndroidOreoStyle || isAndroidMarshmallowStyle

    val isPanelContainerCustomizedNotif = PanelPreferences.qsPanelBackgroundColor.collectAsState().value != null ||
        PanelPreferences.getPanelContainerUseCustom(isDark).collectAsState().value ||
        PanelPreferences.getPanelContainerColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.DEFAULT ||
        PanelPreferences.getPanelContainerHue(isDark).collectAsState().value != 0f ||
        PanelPreferences.getPanelContainerSat(isDark).collectAsState().value != 1f ||
        PanelPreferences.getPanelContainerVal(isDark).collectAsState().value != 1f ||
        PanelPreferences.showMonetSurfaceBackground.collectAsState().value

    val notifContainerColor = if (isFlatLegacyStyle && !isPanelContainerCustomizedNotif) {
        if (isDark) Color(0xFF212121) else Color.White
    } else {
        com.example.ui.theme.getQsNotificationContainerColor(isDark)
    }

    val panelGlobalCornerEnabled by PanelPreferences.panelGlobalCornerEnabled.collectAsState()
    val panelOuterCorner by PanelPreferences.panelOuterCorner.collectAsState()
    val panelInnerCorner by PanelPreferences.panelInnerCorner.collectAsState()
    val effectiveInnerCorner = (if (panelGlobalCornerEnabled) panelOuterCorner else panelInnerCorner).dp
    val effectiveShape = if (isAndroidOreoStyle) RoundedCornerShape(0.dp) else RoundedCornerShape(effectiveInnerCorner)

    if (!isDismissed) {
        val horizontalDraggableState = rememberDraggableState { delta ->
            if (item.isClearable) {
                offsetX += delta
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .draggable(
                    state = horizontalDraggableState,
                    orientation = Orientation.Horizontal,
                    enabled = item.isClearable,
                    onDragStopped = {
                        if (kotlin.math.abs(offsetX) > 180f && item.isClearable) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isDismissed = true
                            onDismiss()
                        } else {
                            offsetX = 0f
                        }
                    }
                )
        ) {
            ContainerCard(
                shape = effectiveShape,
                containerColor = notifContainerColor,
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showMenu = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationX = offsetX
                        alpha = (1f - (kotlin.math.abs(offsetX) / 350f)).coerceIn(0f, 1f)
                        clip = false
                    }
                    .then(
                        if (isAndroidOreoStyle) {
                            Modifier.drawBehind {
                                val strokeWidth = 1.dp.toPx()
                                val lineColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.2f)
                                drawLine(lineColor, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth)
                                drawLine(lineColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth)
                            }
                        } else Modifier
                    )
                    .testTag("notification_item_${item.key}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left side: Crisp Avatar or Large Icon, falling back to App Icon
                    NotificationLeadingAvatar(
                        avatarBitmap = item.avatarBitmap,
                        appIcon = item.appIcon
                    )

                    // Right side: Header, Body, BigPicture Attachment, and Actions
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        // Header: App Name + Time + Expand + Dismiss + More Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                val typeBadge = when {
                                    item.hasProgress -> " • PROGRESS"
                                    item.isOngoing -> " • ONGOING"
                                    else -> ""
                                }
                                Text(
                                    text = "$resolvedAppName$typeBadge",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MonetTertiary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "•  $timeFormatted",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )

                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isExpandable) {
                                    IconButton(
                                        onClick = { isExpanded = !isExpanded },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (item.isClearable) {
                                    IconButton(
                                        onClick = {
                                            isDismissed = true
                                            onDismiss()
                                        },
                                        modifier = Modifier.size(24.dp).testTag("dismiss_notification_${item.key}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                // Long-press / Options Overflow Menu for hiding persistent notifications
                                Box {
                                    IconButton(
                                        onClick = { showMenu = true },
                                        modifier = Modifier.size(24.dp).testTag("notif_more_btn_${item.key}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Options",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Hide app") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.VisibilityOff,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                com.example.service.QuickPanelNotificationListenerService.hidePackage(item.packageName, context)
                                                showMenu = false
                                            }
                                        )
                                        if (!item.channelId.isNullOrBlank()) {
                                            DropdownMenuItem(
                                                text = { Text("Hide channel: ${item.channelId}") },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Default.Block,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                },
                                                onClick = {
                                                    com.example.service.QuickPanelNotificationListenerService.hideChannel(item.channelId, context)
                                                    showMenu = false
                                                }
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = { Text("Notification settings") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                try {
                                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                        putExtra(Settings.EXTRA_APP_PACKAGE, item.packageName)
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {}
                                                showMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Body: Title + Text + Subtext
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val notifTitleCol = if (isDark) Color.White else Color(0xFF333333)
                            val notifTextCol = if (isDark) Color(0xFFD0D7DE) else Color(0xFF333333)
                            if (item.title.isNotBlank()) {
                                Text(
                                    text = item.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = notifTitleCol,
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (item.text.isNotBlank()) {
                                Text(
                                    text = item.text,
                                    fontSize = 12.sp,
                                    color = notifTextCol,
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (isExpanded && !item.subText.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.subText,
                                    fontSize = 11.sp,
                                    color = MonetTertiary.copy(alpha = 0.9f)
                                )
                            }
                        }

                        // Download / Ongoing Progress Bar
                        if (item.hasProgress) {
                            val notifProgressColor = com.example.ui.theme.getSlidersElementColor(isDark)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (item.isIndeterminateProgress) {
                                androidx.compose.material3.LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp)),
                                    color = notifProgressColor,
                                    trackColor = notifProgressColor.copy(alpha = 0.2f)
                                )
                            } else {
                                val progressFraction = if (item.progressMax > 0) {
                                    (item.progress.toFloat() / item.progressMax.toFloat()).coerceIn(0f, 1f)
                                } else 0f
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp)),
                                    color = notifProgressColor,
                                    trackColor = notifProgressColor.copy(alpha = 0.2f)
                                )
                            }
                        }

                        // BigPicture & Attachment Image (rendered in expanded state maintaining correct aspect ratio)
                        if (isExpanded && item.bigPictureBitmap != null && !item.bigPictureBitmap.isRecycled) {
                            val safeBigPicture = remember(item.bigPictureBitmap) {
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && item.bigPictureBitmap.config == Bitmap.Config.HARDWARE) {
                                        item.bigPictureBitmap.copy(Bitmap.Config.ARGB_8888, false)?.asImageBitmap()
                                    } else {
                                        item.bigPictureBitmap.asImageBitmap()
                                    }
                                } catch (_: Throwable) {
                                    null
                                }
                            }
                            if (safeBigPicture != null) {
                                val aspectRatio = remember(item.bigPictureBitmap) {
                                    try {
                                        val w = item.bigPictureBitmap.width.toFloat()
                                        val h = item.bigPictureBitmap.height.toFloat()
                                        if (h > 0) (w / h).coerceIn(0.6f, 2.4f) else 16f / 9f
                                    } catch (_: Throwable) {
                                        16f / 9f
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Image(
                                    bitmap = safeBigPicture,
                                    contentDescription = "Notification attachment image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(aspectRatio)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            1.dp,
                                            if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f),
                                            RoundedCornerShape(12.dp)
                                        )
                                )
                            }
                        }

                        // Action Buttons (e.g. Reply, Mark Read, etc.)
                        if (isExpanded && item.actions.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                item.actions.take(3).forEach { action ->
                                    Surface(
                                        onClick = { onActionClick(action) },
                                        shape = RoundedCornerShape(8.dp),
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.testTag("notif_action_${action.title}")
                                    ) {
                                        Text(
                                            text = action.title,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationLeadingAvatar(
    avatarBitmap: Bitmap?,
    appIcon: Drawable?,
    modifier: Modifier = Modifier
) {
    if (avatarBitmap != null && !avatarBitmap.isRecycled) {
        val safeImageBitmap = remember(avatarBitmap) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && avatarBitmap.config == Bitmap.Config.HARDWARE) {
                    avatarBitmap.copy(Bitmap.Config.ARGB_8888, false)?.asImageBitmap()
                } else {
                    avatarBitmap.asImageBitmap()
                }
            } catch (_: Throwable) {
                null
            }
        }
        if (safeImageBitmap != null) {
            Box(
                modifier = modifier.size(42.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Image(
                    bitmap = safeImageBitmap,
                    contentDescription = "Notification Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
                if (appIcon != null) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                            .padding(1.dp)
                    ) {
                        NotificationAppIcon(drawable = appIcon, size = 14.dp, shape = CircleShape)
                    }
                }
            }
            return
        }
    }
    if (appIcon != null) {
        NotificationAppIcon(
            drawable = appIcon,
            size = 40.dp,
            shape = RoundedCornerShape(10.dp),
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MonetPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MonetPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private object AppIconBitmapCache {
    private val cache = android.util.LruCache<Int, Bitmap>(96)

    fun getOrCreate(drawable: Drawable): Bitmap? {
        val key = System.identityHashCode(drawable)
        cache.get(key)?.let {
            if (!it.isRecycled) return it else cache.remove(key)
        }

        return try {
            val bmp = if (drawable is android.graphics.drawable.BitmapDrawable && drawable.bitmap != null && !drawable.bitmap.isRecycled) {
                val b = drawable.bitmap
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && b.config == Bitmap.Config.HARDWARE) {
                    try {
                        b.copy(Bitmap.Config.ARGB_8888, false)
                    } catch (_: Throwable) {
                        null
                    }
                } else {
                    b
                }
            } else {
                val w = if (drawable.intrinsicWidth in 1..512) drawable.intrinsicWidth else 96
                val h = if (drawable.intrinsicHeight in 1..512) drawable.intrinsicHeight else 96
                val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(b)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                b
            }
            if (bmp != null && !bmp.isRecycled) {
                cache.put(key, bmp)
                bmp
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun NotificationAppIcon(
    drawable: Drawable?,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp)
) {
    if (drawable != null) {
        val bitmap = remember(drawable) {
            AppIconBitmapCache.getOrCreate(drawable)
        }
        val safeImageBitmap = remember(bitmap) {
            try {
                if (bitmap != null && !bitmap.isRecycled) bitmap.asImageBitmap() else null
            } catch (_: Throwable) {
                null
            }
        }
        if (safeImageBitmap != null) {
            Image(
                bitmap = safeImageBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .size(size)
                    .clip(shape)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MonetPrimary,
                modifier = modifier.size(size)
            )
        }
    } else {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = MonetPrimary,
            modifier = modifier.size(size)
        )
    }
}

/**
 * Additional QQS row rendering 2x1 live complication pills (2 side-by-side per row; horizontal scroll for 3+).
 */
@Composable
fun Qqs2x1ComplicationsRow(
    complications: List<QsTileModel>,
    isDark: Boolean,
    fadeBgColor: Color,
    onTileClick: (QsTileModel) -> Unit,
    onTileLongClick: (QsTileModel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (complications.isEmpty()) return

    val cardHeightDp by PanelPreferences.qqs2x1TileHeightDp.collectAsState()
    val scrollState = rememberScrollState()
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .testTag("qqs_2x1_complications_row")
    ) {
        val spacing = 8.dp
        val cardWidth = if (complications.size == 1) {
            maxWidth
        } else {
            ((maxWidth - spacing) / 2f).coerceAtLeast(136.dp)
        }
        val canScroll = complications.size > 2

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (canScroll) Modifier.horizontalScroll(scrollState) else Modifier),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                complications.forEach { tile ->
                    key(tile.id) {
                        Qqs2x1ComplicationCard(
                            tile = tile,
                            isDark = isDark,
                            onClick = { onTileClick(tile) },
                            onLongClick = { onTileLongClick(tile) },
                            modifier = Modifier
                                .width(cardWidth)
                                .height(cardHeightDp.dp)
                        )
                    }
                }
            }

            if (canScroll) {
                if (scrollState.canScrollBackward) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .width(24.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(fadeBgColor, Color.Transparent)
                                )
                            )
                    )
                }
                if (scrollState.canScrollForward) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(24.dp)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, fadeBgColor)
                                )
                            )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Qqs2x1ComplicationCard(
    tile: QsTileModel,
    isDark: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val compSpec = tile.complicationSpec ?: return
    val isActive = tile.state == TileState.ACTIVE
    val isUnavailable = tile.state == TileState.UNAVAILABLE

    val primaryColor = com.example.ui.theme.getQsTilesElementColor(isDark)
    val onPrimaryColor = com.example.ui.theme.getQsTilesOnElementColor(isDark)
    val inactiveTileColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
    val inactiveOnTileColor = com.example.ui.theme.getInactiveTilesOnElementColor(isDark)
    val isInactiveTilesCustomized = PanelPreferences.getInactiveTilesUseCustom(isDark).collectAsState().value ||
        PanelPreferences.getInactiveTilesColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.DEFAULT ||
        PanelPreferences.getInactiveTilesHue(isDark).collectAsState().value != 0f ||
        PanelPreferences.getInactiveTilesSat(isDark).collectAsState().value != 1f ||
        PanelPreferences.getInactiveTilesVal(isDark).collectAsState().value != 1f
    val tileOutlineEnabled by PanelPreferences.tileOutlineEnabled.collectAsState()
    val tileOutlineThicknessDp by PanelPreferences.tileOutlineThicknessDp.collectAsState()
    val showQsTileGradient by PanelPreferences.showQsTileGradient.collectAsState()
    val useHorizontalLine by PanelPreferences.qqs2x1HorizontalLine.collectAsState()
    val usePerimeterProgress by PanelPreferences.qqs2x1PerimeterProgress.collectAsState()
    val activeCornerDp by PanelPreferences.qqs2x1ActiveCornerDp.collectAsState()
    val inactiveCornerDp by PanelPreferences.qqs2x1InactiveCornerDp.collectAsState()
    val cardHeightDp by PanelPreferences.qqs2x1TileHeightDp.collectAsState()
    val complicationArcThicknessDp by PanelPreferences.complicationArcThicknessDp.collectAsState()

    val targetCornerDp = (if (!isActive) inactiveCornerDp else activeCornerDp).dp
    val animatedCornerDp by animateDpAsState(
        targetValue = targetCornerDp,
        animationSpec = tween(durationMillis = 250),
        label = "qqs_2x1_corner_${tile.id}"
    )
    val cardShape = RoundedCornerShape(animatedCornerDp)

    val primaryThemeColor = MaterialTheme.colorScheme.primary
    val tertiaryThemeColor = MaterialTheme.colorScheme.tertiary
    val gradientBrush = remember(primaryThemeColor, tertiaryThemeColor) {
        Brush.linearGradient(
            colors = listOf(primaryThemeColor, tertiaryThemeColor),
            start = androidx.compose.ui.geometry.Offset.Zero,
            end = androidx.compose.ui.geometry.Offset.Infinite
        )
    }

    val useFilledActiveBg = isActive && !tileOutlineEnabled
    val containerBg by animateColorAsState(
        targetValue = when {
            tileOutlineEnabled -> Color.Transparent
            isActive -> primaryColor
            isUnavailable -> inactiveTileColor.copy(alpha = 0.5f)
            else -> inactiveTileColor
        },
        animationSpec = tween(durationMillis = 250),
        label = "qqs_2x1_bg_${tile.id}"
    )

    val outlineColor = when {
        isActive -> primaryColor
        isUnavailable -> inactiveTileColor.copy(alpha = 0.5f)
        else -> inactiveTileColor
    }

    val titleColor = when {
        isActive && tileOutlineEnabled -> primaryColor
        isActive -> onPrimaryColor
        isUnavailable -> (if (isInactiveTilesCustomized && !tileOutlineEnabled) inactiveOnTileColor else (if (isDark) Color.White else Color(0xFF1E293B))).copy(alpha = 0.45f)
        isInactiveTilesCustomized && !tileOutlineEnabled -> inactiveOnTileColor
        else -> if (isDark) Color.White.copy(alpha = 0.92f) else Color(0xFF1E293B)
    }
    val sublineColor = titleColor.copy(alpha = if (isActive) 0.85f else 0.72f)

    val ringModel = remember(compSpec, useFilledActiveBg, isActive, primaryColor, onPrimaryColor, isDark) {
        compSpec.copy(
            isActive = isActive,
            accentColor = when {
                useFilledActiveBg -> onPrimaryColor
                isActive -> primaryColor
                else -> if (isDark) Color(0xFFCCCCCC) else Color(0xFF555555)
            }
        )
    }

    val sublineText = compSpec.subline.ifBlank { tile.subtitle }

    val animatedLineProgress by animateFloatAsState(
        targetValue = compSpec.progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "qqs_2x1_line_progress_${tile.id}"
    )

    val progressDisplayMode = remember(useHorizontalLine, usePerimeterProgress) {
        PanelPreferences.resolveQqs2x1ProgressDisplayMode(
            useHorizontalLine = useHorizontalLine,
            usePerimeterProgress = usePerimeterProgress
        )
    }

    val perimeterPath = remember { Path() }
    val perimeterProgressPath = remember { Path() }
    val perimeterPathMeasure = remember { PathMeasure() }
    val activeArcOverride = com.example.ui.theme.resolveActiveComplicationArcOverride(isDark)
    val inactiveArcOverride = com.example.ui.theme.resolveInactiveComplicationArcOverride(isDark)
    val trackOverride = com.example.ui.theme.resolveComplicationArcTrackOverride(isDark)
    val perimeterTrackColor = trackOverride ?: when {
        useFilledActiveBg -> onPrimaryColor.copy(alpha = 0.25f)
        isActive -> primaryColor.copy(alpha = 0.22f)
        isDark -> Color.White.copy(alpha = 0.14f)
        else -> Color.Black.copy(alpha = 0.12f)
    }
    val defaultPerimeterFillColor = when {
        useFilledActiveBg -> onPrimaryColor
        isActive -> primaryColor
        else -> compSpec.accentColor ?: primaryColor
    }
    val perimeterFillColor = if (isActive) {
        activeArcOverride ?: defaultPerimeterFillColor
    } else {
        inactiveArcOverride ?: defaultPerimeterFillColor
    }

    Row(
        modifier = modifier
            .clip(cardShape)
            .then(
                if (tileOutlineEnabled) {
                    if (isActive && showQsTileGradient) {
                        Modifier.border(tileOutlineThicknessDp.dp, gradientBrush, cardShape)
                    } else {
                        Modifier.border(tileOutlineThicknessDp.dp, outlineColor, cardShape)
                    }
                } else {
                    Modifier
                }
            )
            .then(
                if (useFilledActiveBg && showQsTileGradient) {
                    Modifier.background(gradientBrush)
                } else {
                    Modifier.background(containerBg)
                }
            )
            .then(
                if (progressDisplayMode.showPerimeterBar) {
                    Modifier.drawWithContent {
                        drawContent()
                        val strokePx = complicationArcThicknessDp.coerceIn(2.0f, 5.5f).dp.toPx()
                        val gapPx = if (tileOutlineEnabled) {
                            tileOutlineThicknessDp.dp.toPx() + 1.5.dp.toPx()
                        } else {
                            1.5.dp.toPx()
                        }
                        val inset = strokePx / 2f + gapPx
                        val left = inset
                        val top = inset
                        val right = size.width - inset
                        val bottom = size.height - inset
                        if (right > left && bottom > top) {
                            val w = right - left
                            val h = bottom - top
                            val r = (animatedCornerDp.toPx() - inset).coerceIn(0f, minOf(w, h) / 2f)
                            val cx = (left + right) / 2f
                            perimeterPath.reset()
                            perimeterPath.moveTo(cx, top)
                            if (r > 0.5f) {
                                perimeterPath.lineTo(right - r, top)
                                perimeterPath.arcTo(
                                    rect = Rect(right - 2f * r, top, right, top + 2f * r),
                                    startAngleDegrees = -90f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                perimeterPath.lineTo(right, bottom - r)
                                perimeterPath.arcTo(
                                    rect = Rect(right - 2f * r, bottom - 2f * r, right, bottom),
                                    startAngleDegrees = 0f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                perimeterPath.lineTo(left + r, bottom)
                                perimeterPath.arcTo(
                                    rect = Rect(left, bottom - 2f * r, left + 2f * r, bottom),
                                    startAngleDegrees = 90f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                perimeterPath.lineTo(left, top + r)
                                perimeterPath.arcTo(
                                    rect = Rect(left, top, left + 2f * r, top + 2f * r),
                                    startAngleDegrees = 180f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                perimeterPath.lineTo(cx, top)
                            } else {
                                perimeterPath.lineTo(right, top)
                                perimeterPath.lineTo(right, bottom)
                                perimeterPath.lineTo(left, bottom)
                                perimeterPath.lineTo(left, top)
                                perimeterPath.lineTo(cx, top)
                            }
                            perimeterPath.close()

                            val strokeStyle = Stroke(
                                width = strokePx,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                            drawPath(
                                path = perimeterPath,
                                color = perimeterTrackColor,
                                style = strokeStyle
                            )
                            if (animatedLineProgress > 0.005f) {
                                perimeterPathMeasure.setPath(perimeterPath, false)
                                val totalLen = perimeterPathMeasure.length
                                if (totalLen > 0f) {
                                    if (animatedLineProgress >= 0.995f) {
                                        drawPath(
                                            path = perimeterPath,
                                            color = perimeterFillColor,
                                            style = strokeStyle
                                        )
                                    } else {
                                        perimeterProgressPath.reset()
                                        perimeterPathMeasure.getSegment(
                                            0f,
                                            totalLen * animatedLineProgress,
                                            perimeterProgressPath,
                                            true
                                        )
                                        drawPath(
                                            path = perimeterProgressPath,
                                            color = perimeterFillColor,
                                            style = strokeStyle
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                enabled = !isUnavailable,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .padding(
                horizontal = if (!progressDisplayMode.showIconArc) 12.dp else 10.dp,
                vertical = if (cardHeightDp < 48) 4.dp else 6.dp
            )
            .testTag("qqs_2x1_complication_${tile.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val scaledRingSize = if (!progressDisplayMode.showIconArc) {
            (cardHeightDp * 0.43f).coerceIn(18f, 30f).dp
        } else {
            (cardHeightDp - 18).coerceIn(26, 48).dp
        }
        val scaledIconSize = if (!progressDisplayMode.showIconArc) {
            (cardHeightDp * 0.36f).coerceIn(16f, 24f).dp
        } else {
            (cardHeightDp * 0.32f).coerceIn(14f, 24f).dp
        }
        com.example.ui.tile.ComplicationTileView(
            model = ringModel,
            showGridCardContainer = false,
            isInteractive = false,
            showTitle = false,
            showBelowSubline = false,
            showRingBackground = false,
            showProgressArc = progressDisplayMode.showIconArc,
            showInsideSubline = false,
            customRingSize = scaledRingSize,
            customIconSize = scaledIconSize,
            inactiveIconTintOverride = if (!isActive && !tileOutlineEnabled && isInactiveTilesCustomized) inactiveOnTileColor else null
        )

        if (progressDisplayMode.showHorizontalLine) {
            val lineTrackColor = trackOverride ?: if (useFilledActiveBg) {
                onPrimaryColor.copy(alpha = 0.28f)
            } else if (isDark) {
                Color.White.copy(alpha = 0.16f)
            } else {
                Color.Black.copy(alpha = 0.12f)
            }
            val defaultLineFillColor = when {
                useFilledActiveBg -> onPrimaryColor
                isActive -> primaryColor
                else -> compSpec.accentColor ?: primaryColor
            }
            val lineFillColor = if (isActive) {
                activeArcOverride ?: defaultLineFillColor
            } else {
                inactiveArcOverride ?: defaultLineFillColor
            }
            val lineHeightDp = complicationArcThicknessDp.coerceIn(2.5f, 5.5f).dp

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tile.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        style = androidx.compose.ui.text.TextStyle(
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                    if (sublineText.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sublineText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = sublineColor,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            style = androidx.compose.ui.text.TextStyle(
                                fontFeatureSettings = "tnum",
                                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(lineHeightDp)
                        .testTag("qqs_2x1_horizontal_line_${tile.id}")
                ) {
                    val strokePx = size.height
                    val capInset = strokePx / 2f
                    val y = size.height / 2f
                    val endX = (size.width - capInset).coerceAtLeast(capInset)
                    drawLine(
                        color = lineTrackColor,
                        start = Offset(capInset, y),
                        end = Offset(endX, y),
                        strokeWidth = strokePx,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    if (animatedLineProgress > 0.005f) {
                        val fillEndX = (capInset + (endX - capInset) * animatedLineProgress)
                            .coerceIn(capInset, endX)
                        drawLine(
                            color = lineFillColor,
                            start = Offset(capInset, y),
                            end = Offset(fillEndX, y),
                            strokeWidth = strokePx,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = tile.label,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = androidx.compose.ui.text.TextStyle(
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                    )
                )
                if (sublineText.isNotBlank()) {
                    Text(
                        text = sublineText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = sublineColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = androidx.compose.ui.text.TextStyle(
                            fontFeatureSettings = "tnum",
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Fully Circular Tile Icon (Android 11 shape paradigm)
 * In compact mode (QQS), renders ONLY the circle with no labels.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CircularQsTile(
    tile: QsTileModel,
    compact: Boolean = false,
    morphProgress: Float = 1f,
    tileWidth: Dp = if (morphProgress >= 0.5f) 88.dp else 48.dp,
    isInteractive: Boolean = true,
    columns: Int = 3,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = LocalHapticFeedback.current

    val qqsTileSizeValue by PanelPreferences.qqsTileSize.collectAsState()
    val enabledTileShapeId by PanelPreferences.enabledTileShapeId.collectAsState()
    val disabledTileShapeId by PanelPreferences.disabledTileShapeId.collectAsState()
    val tileOutlineEnabled by PanelPreferences.tileOutlineEnabled.collectAsState()
    val tileOutlineThicknessDp by PanelPreferences.tileOutlineThicknessDp.collectAsState()
    val baseQqsSize = qqsTileSizeValue.dp.coerceIn(36.dp, 64.dp)

    val isActive = tile.state == TileState.ACTIVE
    val isUnavailable = tile.state == TileState.UNAVAILABLE

    val isDark = LocalIsDarkTheme.current
    val primaryColor = com.example.ui.theme.getQsTilesElementColor(isDark)
    val onPrimaryColor = com.example.ui.theme.getQsTilesOnElementColor(isDark)
    val cardContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
    val systemOutlineColor = androidx.compose.material3.MaterialTheme.colorScheme.outline

    val chosenShapeId = if (isActive) enabledTileShapeId else disabledTileShapeId

    val tileShape = remember(chosenShapeId) {
        com.example.ui.shape.QsShapeManager.getComposeShape(chosenShapeId)
    }

    val inactiveTileColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
    val inactiveOnTileColor = com.example.ui.theme.getInactiveTilesOnElementColor(isDark)

    val outlineColor = remember(tile.state, isDark, primaryColor, inactiveTileColor) {
        when (tile.state) {
            TileState.ACTIVE -> primaryColor
            TileState.INACTIVE -> inactiveTileColor
            TileState.UNAVAILABLE -> (if (isDark) Color(0xFF8E918F) else Color(0xFF9E9E9E)).copy(alpha = 0.38f)
        }
    }

    val disableLabelsInExpanded by PanelPreferences.disableLabelsInExpanded.collectAsState()
    val isExpanded = morphProgress >= 0.5f
    val hideLabels = isExpanded && disableLabelsInExpanded
    val isCompactColumns = columns == 3 || columns == 4

    val circleSize = if (isExpanded) {
        if (hideLabels) {
            if (columns == 4) {
                (tileWidth * 0.94f).coerceIn(62.dp, 72.dp)
            } else if (columns == 3) {
                (tileWidth * 0.78f).coerceIn(56.dp, 76.dp)
            } else {
                (tileWidth * 0.72f).coerceIn(56.dp, 84.dp)
            }
        } else {
            56.dp
        }
    } else {
        baseQqsSize
    }
    val iconSize = if (isExpanded) {
        if (hideLabels) circleSize * 0.46f else 26.dp
    } else {
        baseQqsSize * 0.46f
    }
    val labelAlpha = if (hideLabels) 0f else morphProgress.coerceIn(0f, 1f)
    val tileHeight = if (isExpanded) {
        if (hideLabels) circleSize + 6.dp else 100.dp
    } else {
        baseQqsSize
    }

    val isAndroidOreoStyle by PanelPreferences.isAndroidOreoStyle.collectAsState()
    val isAndroidMarshmallowStyle by PanelPreferences.isAndroidMarshmallowStyle.collectAsState()
    val enableLegacyTileShapes by PanelPreferences.enableLegacyTileShapes.collectAsState()
    val renderShapeBg = (!isAndroidOreoStyle && !isAndroidMarshmallowStyle) || enableLegacyTileShapes

    val isInactiveTilesCustomized = PanelPreferences.getInactiveTilesUseCustom(isDark).collectAsState().value ||
        PanelPreferences.getInactiveTilesColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.DEFAULT ||
        PanelPreferences.getInactiveTilesHue(isDark).collectAsState().value != 0f ||
        PanelPreferences.getInactiveTilesSat(isDark).collectAsState().value != 1f ||
        PanelPreferences.getInactiveTilesVal(isDark).collectAsState().value != 1f

    val (backgroundColor, iconColor) = remember(tile.state, isDark, primaryColor, onPrimaryColor, cardContainerColor, tileOutlineEnabled, inactiveTileColor, inactiveOnTileColor, renderShapeBg, isInactiveTilesCustomized) {
        val disabledGrayTint = (if (isDark) Color(0xFF9E9E9E) else Color(0xFF757575)).copy(alpha = 0.50f)
        if (!renderShapeBg) {
            val inactiveFg = if (isInactiveTilesCustomized) inactiveTileColor else (if (isDark) Color(0xFFCCCCCC) else Color(0xFF555555))
            val fg = when (tile.state) {
                TileState.ACTIVE -> primaryColor
                TileState.INACTIVE -> inactiveFg
                TileState.UNAVAILABLE -> disabledGrayTint
            }
            Pair(Color.Transparent, fg)
        } else {
            val inactiveBg = inactiveTileColor
            val inactiveText = if (!tileOutlineEnabled && isInactiveTilesCustomized) {
                inactiveOnTileColor
            } else if (isDark) {
                Color.White.copy(alpha = 0.90f)
            } else {
                Color(0xFF1E293B)
            }
            val unavailableBg = (if (isDark) Color(0xFF555555) else Color(0xFFBDBDBD)).copy(alpha = 0.35f)
            val unavailableText = disabledGrayTint

            val bg = if (tileOutlineEnabled) {
                Color.Transparent
            } else {
                when (tile.state) {
                    TileState.ACTIVE -> primaryColor
                    TileState.INACTIVE -> inactiveBg
                    TileState.UNAVAILABLE -> unavailableBg
                }
            }
            val fg = when (tile.state) {
                TileState.ACTIVE -> if (tileOutlineEnabled) primaryColor else onPrimaryColor
                TileState.INACTIVE -> inactiveText
                TileState.UNAVAILABLE -> unavailableText
            }
            Pair(bg, fg)
        }
    }

    val showQsTileGradient by PanelPreferences.showQsTileGradient.collectAsState()
    val primaryThemeColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val tertiaryThemeColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiary

    val tileGradientBrush = remember(primaryThemeColor, tertiaryThemeColor) {
        Brush.linearGradient(
            colors = listOf(primaryThemeColor, tertiaryThemeColor),
            start = androidx.compose.ui.geometry.Offset.Zero,
            end = androidx.compose.ui.geometry.Offset.Infinite
        )
    }

    val compSpec = tile.complicationSpec

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = if (isCompactColumns && hideLabels) Arrangement.Center else Arrangement.Top,
        modifier = Modifier
            .width(tileWidth)
            .then(if (isCompactColumns && hideLabels) Modifier.height(72.dp).wrapContentHeight(Alignment.CenterVertically) else Modifier.wrapContentHeight())
            .graphicsLayer {
                compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
            }
            .then(
                if (isInteractive) {
                    Modifier.combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !isUnavailable,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClick()
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick()
                        }
                    )
                } else {
                    Modifier
                }
            )
            .testTag("tile_${tile.id}")
    ) {
        if (compSpec != null) {
            val effectiveRingSize = if (renderShapeBg && !isExpanded && compSpec.sublinePlacement == com.example.model.ComplicationSublinePlacement.BELOW_RING) {
                (circleSize - 8.dp).coerceAtLeast(36.dp)
            } else {
                circleSize
            }
            val effectiveLayoutMode = if (isExpanded) {
                com.example.model.ComplicationLayoutMode.GRID_TILE
            } else {
                com.example.model.ComplicationLayoutMode.HEADER_ROW
            }
            val effectiveModel = if (!renderShapeBg && compSpec.sublinePlacement == com.example.model.ComplicationSublinePlacement.BELOW_RING) {
                compSpec.copy(
                    layoutMode = effectiveLayoutMode,
                    sublinePlacement = com.example.model.ComplicationSublinePlacement.INSIDE_RING
                )
            } else {
                compSpec.copy(layoutMode = effectiveLayoutMode)
            }
            com.example.ui.tile.ComplicationTileView(
                model = effectiveModel,
                showGridCardContainer = false,
                isInteractive = false,
                showTitle = false,
                showBelowSubline = renderShapeBg && (isExpanded || compSpec.sublinePlacement == com.example.model.ComplicationSublinePlacement.BELOW_RING),
                showRingBackground = renderShapeBg,
                showInsideSubline = true,
                customRingSize = effectiveRingSize
            )
        } else {
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .then(
                        if (renderShapeBg && tileOutlineEnabled) {
                            if (isActive && showQsTileGradient) {
                                Modifier.insideShapeBorder(tileOutlineThicknessDp.dp, tileGradientBrush, tileShape)
                            } else {
                                Modifier.insideShapeBorder(tileOutlineThicknessDp.dp, outlineColor, tileShape)
                            }
                        } else {
                            Modifier
                        }
                    )
                    .then(if (renderShapeBg) Modifier.clip(tileShape) else Modifier)
                    .then(
                        if (renderShapeBg) {
                            if (isActive && showQsTileGradient && !tileOutlineEnabled) {
                                Modifier.background(tileGradientBrush)
                            } else {
                                Modifier.background(backgroundColor)
                            }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                val isThirdParty = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.category == TileCategory.EXTERNAL || tile.id.contains("/")
                if (isThirdParty) {
                    val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
                    val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
                    ExternalTileIcon(
                        packageName = pkg,
                        serviceClassName = cls,
                        tintColor = iconColor,
                        contentDescription = tile.label,
                        fallbackDrawable = tile.externalDrawable,
                        modifier = Modifier.size(iconSize)
                    )
                } else {
                    tile.iconVector?.let { vector ->
                        Icon(
                            imageVector = vector,
                            contentDescription = tile.label,
                            tint = iconColor,
                            modifier = Modifier.size(iconSize)
                        )
                    } ?: Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = tile.label,
                        tint = iconColor,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }

        if (labelAlpha > 0.02f) {
            Spacer(modifier = Modifier.height(if (renderShapeBg && compSpec != null && compSpec.sublinePlacement == com.example.model.ComplicationSublinePlacement.BELOW_RING) 2.dp else 4.dp))

            val qsTileLabelFontSize by PanelPreferences.qsTileLabelFontSize.collectAsState()
            val qsTileSubtitleFontSize by PanelPreferences.qsTileSubtitleFontSize.collectAsState()

            val tileLabelColor = when {
                isUnavailable -> (if (isDark) Color(0xFF9E9E9E) else Color(0xFF757575)).copy(alpha = 0.50f)
                isAndroidOreoStyle -> if (isActive) primaryColor else (if (isInactiveTilesCustomized) inactiveTileColor else (if (isDark) Color(0xFFCCCCCC) else Color(0xFF555555)))
                else -> if (isDark) Color(0xFFCCCCCC) else Color(0xFF333333)
            }
            val tileSubtitleColor = when {
                isUnavailable -> (if (isDark) Color(0xFF8E918F) else Color(0xFF757575)).copy(alpha = 0.45f)
                isActive -> androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                isDark -> Color.Gray
                else -> Color(0xFF333333).copy(alpha = 0.8f)
            }

            Text(
                text = tile.label,
                fontSize = qsTileLabelFontSize.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = tileLabelColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val skipDuplicateSubline = renderShapeBg &&
                compSpec != null &&
                compSpec.sublinePlacement == com.example.model.ComplicationSublinePlacement.BELOW_RING &&
                tile.subtitle == compSpec.subline
            if (!skipDuplicateSubline) {
                val isMarshmallowStyle = PanelPreferences.isAndroidMarshmallowStyle(context)
                val subtitleOffset = if (isMarshmallowStyle) 1.dp else (-1.5).dp

                Text(
                    text = tile.subtitle,
                    fontSize = qsTileSubtitleFontSize.sp,
                    color = tileSubtitleColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.offset(y = subtitleOffset),
                    style = androidx.compose.ui.text.TextStyle(
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
            }
        }
    }
}

@Composable
fun VolumeSliderRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier.width(68.dp)
        )

        QsProgressSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            sliderTarget = com.example.data.QsSliderTarget.VOLUME,
            activeColor = accentColor,
            inactiveColor = DarkUiSurfaceVariant,
            thumbColor = accentColor,
            modifier = Modifier
                .weight(1f)
                .testTag("volume_slider_${label.lowercase()}")
        )

        Text(
            text = "${(value * 100).toInt()}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.LightGray,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun BrightnessSliderRow(
    brightness: Int,
    onBrightnessChange: (Int) -> Unit,
    sliderAlpha: Float,
    sliderHeight: androidx.compose.ui.unit.Dp,
    isAtBottom: Boolean = false,
    verticalOffset: androidx.compose.ui.unit.Dp = 0.dp,
    isQqs: Boolean = false
) {
    val hideBrightnessPercentage by PanelPreferences.hideBrightnessPercentage.collectAsState()
    val hideBrightnessIcon by PanelPreferences.hideBrightnessIcon.collectAsState()
    val showBrightnessPlusMinus by PanelPreferences.showBrightnessPlusMinus.collectAsState()
    val showBrightnessControlsBackground by PanelPreferences.showBrightnessControlsBackground.collectAsState()
    val showBrightnessGradient by PanelPreferences.showBrightnessGradient.collectAsState()
    val minBrightness by PanelPreferences.minBrightness.collectAsState()
    val maxBrightness by PanelPreferences.maxBrightness.collectAsState()

    val sliderSyncAll by PanelPreferences.sliderSyncAll.collectAsState()
    val globalProgressStyle by PanelPreferences.progressBarStyle.collectAsState()
    val brightnessStyle by PanelPreferences.brightnessSliderStyle.collectAsState()
    val resolvedStyle = if (sliderSyncAll) globalProgressStyle else brightnessStyle

    val isStraightOrWavy = resolvedStyle == ProgressBarStyle.STRAIGHT || resolvedStyle == ProgressBarStyle.WAVY
    val styleOffset = if (isQqs && isStraightOrWavy) (-4).dp else 0.dp
    val totalVerticalOffset = verticalOffset + styleOffset

    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    val nativeTileManager = com.example.system.NativeTileManager.instance
    val autoBrightnessFlow = remember(nativeTileManager) {
        nativeTileManager?.isAutoBrightnessEnabled ?: kotlinx.coroutines.flow.MutableStateFlow(false)
    }
    val isAutoBrightness by autoBrightnessFlow.collectAsState()
    val tertiaryThemeColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiary
    if (sliderAlpha > 0.01f) {
        val availableHeight = if (!isAtBottom) (sliderHeight - 8.dp).coerceAtLeast(28.dp) else sliderHeight
        val controlSize = if (availableHeight < 32.dp) availableHeight else 32.dp
        val clampedBrightness = brightness.coerceIn(minBrightness, maxBrightness)
        val isAtMin = clampedBrightness <= minBrightness
        val isAtMax = clampedBrightness >= maxBrightness

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(sliderHeight)
                .graphicsLayer { alpha = sliderAlpha }
                .then(
                    if (!isAtBottom) {
                        Modifier.padding(bottom = 8.dp)
                    } else {
                        Modifier
                    }
                )
                .offset(y = totalVerticalOffset)
        ) {
            val sliderAccentColor = com.example.ui.theme.getSlidersElementColor(isDark)
            val inactiveTrackColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
            val inactiveOnTileColor = com.example.ui.theme.getInactiveTilesOnElementColor(isDark)
            val brightnessControlsFg = if (showBrightnessControlsBackground) inactiveOnTileColor else sliderAccentColor
            val brightnessGradientBrush = remember(sliderAccentColor, tertiaryThemeColor) {
                Brush.horizontalGradient(listOf(sliderAccentColor, tertiaryThemeColor))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!hideBrightnessIcon) {
                    BrightnessSliderIcon(
                        isHighBrightness = clampedBrightness > (minBrightness + maxBrightness) / 2,
                        showBackground = showBrightnessControlsBackground,
                        bgColor = inactiveTrackColor,
                        accentColor = sliderAccentColor,
                        controlSize = controlSize,
                        isAutoBrightness = isAutoBrightness,
                        onClick = { toggleAutoBrightnessSetting(context) }
                    )
                }

                if (showBrightnessPlusMinus) {
                    BrightnessStepButton(
                        icon = Icons.Default.Remove,
                        contentDescription = "Decrease brightness",
                        showBackground = showBrightnessControlsBackground,
                        isAtLimit = isAtMin,
                        bgColor = inactiveTrackColor,
                        accentColor = brightnessControlsFg,
                        controlSize = controlSize,
                        testTag = "brightness_minus_button",
                        onClick = { onBrightnessChange((brightness - 15).coerceAtLeast(minBrightness)) }
                    )
                }

                QsProgressSlider(
                    value = clampedBrightness.toFloat(),
                    onValueChange = { onBrightnessChange(it.toInt()) },
                    valueRange = minBrightness.toFloat()..maxBrightness.toFloat(),
                    sliderTarget = com.example.data.QsSliderTarget.BRIGHTNESS,
                    activeColor = sliderAccentColor,
                    activeBrush = if (showBrightnessGradient) brightnessGradientBrush else null,
                    inactiveColor = inactiveTrackColor,
                    thumbColor = sliderAccentColor,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("brightness_slider")
                )

                if (showBrightnessPlusMinus) {
                    BrightnessStepButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Increase brightness",
                        showBackground = showBrightnessControlsBackground,
                        isAtLimit = isAtMax,
                        bgColor = inactiveTrackColor,
                        accentColor = brightnessControlsFg,
                        controlSize = controlSize,
                        testTag = "brightness_plus_button",
                        onClick = { onBrightnessChange((brightness + 15).coerceAtMost(maxBrightness)) }
                    )
                }

                if (!hideBrightnessPercentage) {
                    val inactiveOnTileColor = com.example.ui.theme.getInactiveTilesOnElementColor(isDark)
                    val percentageColor = if (showBrightnessControlsBackground) {
                        inactiveOnTileColor
                    } else if (isDark) {
                        Color.White
                    } else {
                        Color(0xFF333333)
                    }
                    val range = (maxBrightness - minBrightness).coerceAtLeast(1)
                    val pct = (((clampedBrightness - minBrightness).toFloat() / range) * 100).toInt().coerceIn(0, 100)

                    BrightnessPercentageBadge(
                        percentage = pct,
                        showBackground = showBrightnessControlsBackground,
                        bgColor = inactiveTrackColor,
                        textColor = percentageColor,
                        heightDp = controlSize
                    )
                }
            }
        }
    }
}

internal fun toggleAutoBrightnessSetting(context: Context) {
    val manager = com.example.system.NativeTileManager.instance
    if (manager != null && manager.toggleAutoBrightness()) {
        return
    }
    if (Settings.System.canWrite(context)) {
        try {
            val current = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            val next = if (current == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            } else {
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            }
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, next)
            com.example.system.ComplicationDataHub.refreshTelemetryNow()
        } catch (_: Throwable) {}
    } else {
        try {
            val permIntent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(permIntent)
            com.example.service.QuickPanelAccessibilityService.hideCustomPanel()
            com.example.service.QuickSettingsOverlayService.instance?.hideOverlayPanelSmoothly()
        } catch (_: Throwable) {}
    }
}

@Composable
internal fun BrightnessSliderIcon(
    isHighBrightness: Boolean,
    showBackground: Boolean,
    bgColor: Color,
    accentColor: Color,
    controlSize: Dp = 32.dp,
    isAutoBrightness: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val iconVector = when {
        isAutoBrightness -> Icons.Default.BrightnessAuto
        isHighBrightness -> Icons.Default.BrightnessMedium
        else -> Icons.Default.BrightnessLow
    }
    val onAccentColor = if (accentColor.luminance() > 0.5f) Color(0xFF0F172A) else Color.White
    val containerColor by animateColorAsState(
        targetValue = when {
            showBackground && isAutoBrightness -> accentColor
            showBackground -> bgColor
            isAutoBrightness -> accentColor.copy(alpha = 0.16f)
            else -> Color.Transparent
        },
        label = "brightness_icon_bg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (showBackground && isAutoBrightness) onAccentColor else accentColor,
        label = "brightness_icon_tint"
    )
    val cornerRadius by animateDpAsState(
        targetValue = if (isAutoBrightness) 8.dp else (controlSize / 2),
        label = "brightness_icon_corner"
    )
    val description = if (isAutoBrightness) "Auto-brightness on, tap to turn off" else "Auto-brightness off, tap to turn on"

    Box(
        modifier = Modifier
            .size(controlSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .testTag("brightness_auto_button"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = description,
            tint = iconTint,
            modifier = Modifier.size(if (showBackground) 18.dp else 20.dp)
        )
    }
}

@Composable
internal fun BrightnessStepButton(
    icon: ImageVector,
    contentDescription: String,
    showBackground: Boolean,
    isAtLimit: Boolean,
    bgColor: Color,
    accentColor: Color,
    controlSize: Dp = 32.dp,
    testTag: String,
    onClick: () -> Unit
) {
    if (showBackground) {
        val cornerRadius by animateDpAsState(
            targetValue = if (isAtLimit) 8.dp else (controlSize / 2),
            label = "brightness_step_corner"
        )
        Box(
            modifier = Modifier
                .size(controlSize)
                .clip(RoundedCornerShape(cornerRadius))
                .background(bgColor)
                .clickable(onClick = onClick)
                .testTag(testTag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = accentColor.copy(alpha = if (isAtLimit) 0.45f else 1f),
                modifier = Modifier.size(18.dp)
            )
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(32.dp)
                .testTag(testTag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
internal fun BrightnessPercentageBadge(
    percentage: Int,
    showBackground: Boolean,
    bgColor: Color,
    textColor: Color,
    heightDp: Dp = 32.dp
) {
    if (showBackground) {
        Box(
            modifier = Modifier
                .height(heightDp)
                .widthIn(min = 42.dp)
                .clip(CircleShape)
                .background(bgColor)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$percentage%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1
            )
        }
    } else {
        Text(
            text = "$percentage%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Isolated Card-Level Backdrop Blur Architecture:
 * Renders an isolated blurred background layer clipped to container bounds,
 * while foreground elements (Text, Icons, Sliders, Tiles) sit on an unblurred top layer
 * remaining 100% sharp, legible, and crisp.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BlurredContainerCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    containerColor: Color = com.example.ui.theme.getQsPanelBackgroundColor(LocalIsDarkTheme.current),
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    val enablePanelShadows by PanelPreferences.enablePanelShadows.collectAsState()
    val panelShadowElevation by PanelPreferences.panelShadowElevation.collectAsState()
    val panelShadowOffsetY by PanelPreferences.panelShadowOffsetY.collectAsState()
    val panelShadowAlpha by PanelPreferences.panelShadowAlpha.collectAsState()
    val isDark = LocalIsDarkTheme.current

    val shadowModifier = if (enablePanelShadows && panelShadowElevation > 0f) {
        Modifier
            .drawBehind {
                val elevationPx = panelShadowElevation.dp.toPx()
                if (elevationPx <= 0.5f || size.width <= 0f || size.height <= 0f) return@drawBehind

                val outline = shape.createOutline(size, layoutDirection, this)

                val ambientBaseAlpha = if (isDark) 0.22f else 0.10f
                val keyBaseAlpha = if (isDark) 0.32f else 0.16f
                val elevationFactor = (panelShadowElevation / 8f).coerceIn(0.6f, 1.8f)
                val alphaScale = (panelShadowAlpha / 0.35f).coerceIn(0.3f, 2.5f)

                val ambientAlpha = (ambientBaseAlpha * elevationFactor * alphaScale).coerceIn(0.04f, 0.45f)
                val keyAlpha = (keyBaseAlpha * elevationFactor * alphaScale).coerceIn(0.06f, 0.55f)

                val ambientRadius = (elevationPx * 1.3f).coerceAtLeast(1f)
                val ambientOffsetY = panelShadowOffsetY.dp.toPx() + (elevationPx * 0.15f)

                val keyRadius = (elevationPx * 0.75f).coerceAtLeast(1f)
                val keyOffsetY = panelShadowOffsetY.dp.toPx() + (elevationPx * 0.55f)

                drawIntoCanvas { canvas ->
                    val paint = androidx.compose.ui.graphics.Paint()
                    val frameworkPaint = paint.asFrameworkPaint()
                    frameworkPaint.isAntiAlias = true

                    // 1. Ambient soft omnidirectional shadow
                    frameworkPaint.color = android.graphics.Color.argb((ambientAlpha * 255).toInt(), 0, 0, 0)
                    frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                        ambientRadius,
                        android.graphics.BlurMaskFilter.Blur.NORMAL
                    )
                    canvas.save()
                    canvas.translate(0f, ambientOffsetY)
                    canvas.drawOutline(outline = outline, paint = paint)
                    canvas.restore()

                    // 2. Key directional drop shadow
                    frameworkPaint.color = android.graphics.Color.argb((keyAlpha * 255).toInt(), 0, 0, 0)
                    frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                        keyRadius,
                        android.graphics.BlurMaskFilter.Blur.NORMAL
                    )
                    canvas.save()
                    canvas.translate(0f, keyOffsetY)
                    canvas.drawOutline(outline = outline, paint = paint)
                    canvas.restore()
                }
            }
            .graphicsLayer {
                clip = false
            }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(shadowModifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(shape)
                .background(containerColor)
                .then(
                    if (onClick != null || onLongClick != null) {
                        Modifier.combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onClick?.invoke() },
                            onLongClick = onLongClick
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            content()
        }
    }
}

@Composable
fun ContainerCard(
    shape: androidx.compose.ui.graphics.Shape = ExpressiveCardShapes.SingleCard,
    modifier: Modifier = Modifier,
    containerColor: Color = com.example.ui.theme.getQsPanelBackgroundColor(LocalIsDarkTheme.current),
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    BlurredContainerCard(
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        onClick = onClick,
        onLongClick = onLongClick,
        content = content
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InactiveTileItem(
    tile: QsTileModel,
    targetTileWidth: Dp,
    draggedTileId: String?,
    tileBoundsMap: MutableMap<String, Rect>,
    isKitKat: Boolean = false,
    isMarshmallow: Boolean = false,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onClick: () -> Unit
) {
    val isThisDragging = draggedTileId == tile.id
    Box(
        modifier = Modifier
            .width(targetTileWidth)
            .onGloballyPositioned { coords ->
                if (draggedTileId != tile.id) {
                    tileBoundsMap[tile.id] = coords.boundsInRoot()
                }
            }
            .pointerInput(tile.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = onDragStart,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        if (isThisDragging) {
            // Hide original drag box
            Spacer(modifier = Modifier.width(targetTileWidth).height(86.dp))
        } else {
            Box(modifier = Modifier.width(targetTileWidth)) {
                EditStudioTileView(
                    tile = tile,
                    tileWidth = targetTileWidth,
                    isKitKat = isKitKat,
                    isMarshmallow = isMarshmallow,
                    isActive = false
                )

                // Plus add button
                if (isKitKat) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .size(20.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF33B5E5))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add tile",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                } else if (isMarshmallow) {
                    val isDark = LocalIsDarkTheme.current
                    val marshmallowAccent = com.example.ui.theme.getQsTilesElementColor(isDark)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(marshmallowAccent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add tile",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 2.dp, end = 2.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add tile",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MarshmallowTileIcon(
    tile: QsTileModel,
    tint: Color,
    modifier: Modifier = Modifier.size(28.dp)
) {
    val isThirdParty = tile.isExternal || tile.category == TileCategory.THIRD_PARTY || tile.category == TileCategory.EXTERNAL || tile.id.contains("/")
    if (isThirdParty) {
        val pkg = tile.packageName ?: if (tile.id.contains("/")) tile.id.substringBefore('/') else null
        val cls = tile.serviceClassName ?: if (tile.id.contains("/")) tile.id.substringAfter('/') else null
        ExternalTileIcon(
            packageName = pkg,
            serviceClassName = cls,
            tintColor = tint,
            contentDescription = tile.label,
            fallbackDrawable = tile.externalDrawable,
            modifier = modifier
        )
    } else {
        val vector = com.example.ui.tile.QsTileIconHelper.getTileImageVector(tile)
        Icon(
            imageVector = vector,
            contentDescription = tile.label,
            tint = tint,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MarshmallowTileItem(
    tile: QsTileModel,
    primaryAccent: Color,
    isTopTile: Boolean,
    onTileClick: (QsTileModel) -> Unit,
    onTileLongClick: (QsTileModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val isActive = tile.state == TileState.ACTIVE
    val isUnavailable = tile.state == TileState.UNAVAILABLE

    val nativeMgr = com.example.system.NativeTileManager.instance
    val liveWifiSsid by (nativeMgr?.wifiSsid ?: remember { kotlinx.coroutines.flow.MutableStateFlow<String?>(null) }).collectAsState()
    val liveBtName by (nativeMgr?.bluetoothDeviceName ?: remember { kotlinx.coroutines.flow.MutableStateFlow<String?>(null) }).collectAsState()
    val displayLabel = remember(tile.id, tile.label, tile.subtitle, tile.state, liveWifiSsid, liveBtName) {
        com.example.system.TileRegistry.resolveMarshmallowTileLabel(
            tile = tile,
            wifiSsidName = liveWifiSsid,
            btDeviceName = liveBtName
        )
    }

    val enabledTileShapeId by PanelPreferences.enabledTileShapeId.collectAsState()
    val disabledTileShapeId by PanelPreferences.disabledTileShapeId.collectAsState()
    val enableLegacyTileShapes by PanelPreferences.enableLegacyTileShapes.collectAsState()
    val tileOutlineEnabled by PanelPreferences.tileOutlineEnabled.collectAsState()
    val tileOutlineThicknessDp by PanelPreferences.tileOutlineThicknessDp.collectAsState()

    val chosenShapeId = if (isActive) enabledTileShapeId else disabledTileShapeId
    val tileShape = remember(chosenShapeId) {
        com.example.ui.shape.QsShapeManager.getComposeShape(chosenShapeId)
    }

    val isInactiveTilesCustomized = PanelPreferences.getInactiveTilesUseCustom(isDark).collectAsState().value ||
        PanelPreferences.getInactiveTilesColorMode(isDark).collectAsState().value != com.example.data.ComplicationArcMonetMode.DEFAULT ||
        PanelPreferences.getInactiveTilesHue(isDark).collectAsState().value != 0f ||
        PanelPreferences.getInactiveTilesSat(isDark).collectAsState().value != 1f ||
        PanelPreferences.getInactiveTilesVal(isDark).collectAsState().value != 1f

    val textPrimary = Color(MonetThemeHelper.getTextPrimary(context, isDark))
    val textSecondary = Color(MonetThemeHelper.getTextSecondary(context, isDark))
    val inactiveTileColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)
    val disabledGrayTint = (if (isDark) Color(0xFF9E9E9E) else Color(0xFF757575)).copy(alpha = 0.50f)

    val iconTint = when {
        isUnavailable -> disabledGrayTint
        enableLegacyTileShapes -> {
            if (isActive) {
                if (tileOutlineEnabled) primaryAccent else Color.White
            } else {
                if (isInactiveTilesCustomized) inactiveTileColor else textSecondary
            }
        }
        else -> {
            if (isActive) primaryAccent else (if (isInactiveTilesCustomized) inactiveTileColor else textSecondary)
        }
    }
    val textTint = when {
        isUnavailable -> disabledGrayTint
        isActive -> textPrimary
        else -> if (isInactiveTilesCustomized) inactiveTileColor else textSecondary
    }
    val tileBgColor = when {
        tileOutlineEnabled -> Color.Transparent
        isUnavailable -> (if (isDark) Color(0xFF555555) else Color(0xFFBDBDBD)).copy(alpha = 0.35f)
        isActive -> primaryAccent
        else -> inactiveTileColor
    }
    val legacyOutlineColor = when {
        isUnavailable -> disabledGrayTint.copy(alpha = 0.38f)
        isActive -> primaryAccent
        else -> inactiveTileColor
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .combinedClickable(
                enabled = !isUnavailable,
                onClick = { onTileClick(tile) },
                onLongClick = { onTileLongClick(tile) }
            )
            .padding(vertical = if (isTopTile) 4.dp else 6.dp)
            .testTag("tile_${tile.id}")
    ) {
        val compSpec = tile.complicationSpec
        if (compSpec != null) {
            com.example.ui.tile.ComplicationTileView(
                model = compSpec.copy(
                    layoutMode = com.example.model.ComplicationLayoutMode.HEADER_ROW,
                    sublinePlacement = com.example.model.ComplicationSublinePlacement.INSIDE_RING
                ),
                showGridCardContainer = false,
                isInteractive = false,
                showTitle = false,
                showBelowSubline = false,
                showRingBackground = enableLegacyTileShapes,
                showInsideSubline = true,
                customRingSize = if (enableLegacyTileShapes) 46.dp else 28.dp
            )
        } else if (enableLegacyTileShapes) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .then(
                        if (tileOutlineEnabled) {
                            Modifier.insideShapeBorder(tileOutlineThicknessDp.dp, legacyOutlineColor, tileShape)
                        } else Modifier
                    )
                    .clip(tileShape)
                    .background(tileBgColor),
                contentAlignment = Alignment.Center
            ) {
                MarshmallowTileIcon(
                    tile = tile,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            MarshmallowTileIcon(
                tile = tile,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
        }
        if (isTopTile) {
            Spacer(modifier = Modifier.height(3.dp))
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .padding(vertical = 2.dp),
                thickness = 1.dp,
                color = Color(MonetThemeHelper.getCardOutline(context, isDark)).copy(alpha = 0.35f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = displayLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = textTint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = displayLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = textTint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MarshmallowTilesGrid(
    activeTiles: List<QsTileModel>,
    primaryAccent: Color,
    onTileClick: (QsTileModel) -> Unit,
    onTileLongClick: (QsTileModel) -> Unit
) {
    if (activeTiles.isEmpty()) return

    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val topTiles = activeTiles.take(2)
        val remainingTiles = if (activeTiles.size > 2) activeTiles.drop(2) else emptyList()

        if (topTiles.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (topTiles.size == 1) {
                    Spacer(modifier = Modifier.weight(0.5f))
                    MarshmallowTileItem(
                        tile = topTiles[0],
                        primaryAccent = primaryAccent,
                        isTopTile = true,
                        onTileClick = onTileClick,
                        onTileLongClick = onTileLongClick,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(0.5f))
                } else {
                    topTiles.forEach { tile ->
                        MarshmallowTileItem(
                            tile = tile,
                            primaryAccent = primaryAccent,
                            isTopTile = true,
                            onTileClick = onTileClick,
                            onTileLongClick = onTileLongClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        val rows = remainingTiles.chunked(3)
        rows.forEach { rowTiles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (rowTiles.size) {
                    1 -> {
                        Spacer(modifier = Modifier.weight(1f))
                        MarshmallowTileItem(
                            tile = rowTiles[0],
                            primaryAccent = primaryAccent,
                            isTopTile = false,
                            onTileClick = onTileClick,
                            onTileLongClick = onTileLongClick,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    2 -> {
                        Spacer(modifier = Modifier.weight(0.5f))
                        MarshmallowTileItem(
                            tile = rowTiles[0],
                            primaryAccent = primaryAccent,
                            isTopTile = false,
                            onTileClick = onTileClick,
                            onTileLongClick = onTileLongClick,
                            modifier = Modifier.weight(1f)
                        )
                        MarshmallowTileItem(
                            tile = rowTiles[1],
                            primaryAccent = primaryAccent,
                            isTopTile = false,
                            onTileClick = onTileClick,
                            onTileLongClick = onTileLongClick,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                    3 -> {
                        rowTiles.forEach { tile ->
                            MarshmallowTileItem(
                                tile = tile,
                                primaryAccent = primaryAccent,
                                isTopTile = false,
                                onTileClick = onTileClick,
                                onTileLongClick = onTileLongClick,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun resolveFooterButtonIconTint(
    footerIconBgEnabled: Boolean,
    inactiveOnTileColor: Color,
    defaultIconTint: Color
): Color {
    return if (footerIconBgEnabled) inactiveOnTileColor else defaultIconTint
}

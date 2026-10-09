package com.example.ui.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.runtime.DisposableEffect
import com.example.data.EditButtonPosition
import com.example.data.TileShape
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.provider.Settings
import android.text.format.DateUtils
import com.example.data.local.TileEntity
import com.example.model.TileCategory
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.wrapContentHeight
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.RecyclerView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
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
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
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

    var editActiveTiles by remember { mutableStateOf<List<QsTileModel>?>(null) }
    var editInactiveTiles by remember { mutableStateOf<List<QsTileModel>?>(null) }

    val enterEditMode = {
        editActiveTiles = tiles.filter { it.isVisible }
        editInactiveTiles = tiles.filter { !it.isVisible }
        isEditingTiles = true
    }

    val persistReorder: (List<QsTileModel>, List<QsTileModel>) -> Unit = { activeList, inactiveList ->
        onTilesReordered?.invoke(activeList, inactiveList)
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
                        tileId = if (isExt) compId else tile.id,
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
                        tileId = if (isExt) compId else tile.id,
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
                // Ignore
            }
        }
    }

    val saveAndExitEditMode: () -> Unit = {
        val finalActive = editActiveTiles
        val finalInactive = editInactiveTiles
        if (finalActive != null && finalInactive != null) {
            persistReorder(finalActive, finalInactive)
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

    val shadeDragModifier = Modifier.pointerInput(maxDragDistancePx, expansionStage, isEditingTiles) {
        if (isEditingTiles) return@pointerInput // Disable vertical swipe collapse while editing tiles
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
                dragTranslationYState.floatValue = 0f
                isDraggingMorph = false
                manualDragMorphFraction = if (expansionStage == QsExpansionStage.FULL_QS) 1f else 0f
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
                        // Swiped UP in FULL_QS -> Collapse to QQS
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        dragTranslationYState.floatValue = 0f
                        onStageChange(QsExpansionStage.QQS)
                    } else {
                        // Dragged UP less than 36dp -> Snap back open!
                        coroutineScope.launch {
                            val anim = androidx.compose.animation.core.Animatable(dragTranslationYState.floatValue)
                            anim.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 400f)) {
                                dragTranslationYState.floatValue = this.value
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
                    } else if (dragAccumulatedY > with(density) { 50.dp.toPx() } || manualDragMorphFraction > 0.40f) {
                        // Swiped DOWN in QQS -> Morph transition expands to FULL_QS without shifting panel!
                        isDraggingMorph = false
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStageChange(QsExpansionStage.FULL_QS)
                        dragTranslationYState.floatValue = 0f
                    } else {
                        // Dragged UP less than 36dp -> Snap back open!
                        isDraggingMorph = false
                        coroutineScope.launch {
                            val anim = androidx.compose.animation.core.Animatable(dragTranslationYState.floatValue)
                            anim.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 400f)) {
                                dragTranslationYState.floatValue = this.value
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
                            // Swiped UP in FULL_QS -> Collapse to QQS
                            dragTranslationYState.floatValue = 0f
                            onStageChange(QsExpansionStage.QQS)
                        } else {
                            // Swiped UP in QQS -> Trigger smooth dismissal following finger!
                            val panelHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
                            val startVal = dragTranslationYState.floatValue
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
                    } else {
                        // Dragged less than threshold -> Snap back open!
                        coroutineScope.launch {
                            val anim = androidx.compose.animation.core.Animatable(dragTranslationYState.floatValue)
                            anim.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 400f)) {
                                dragTranslationYState.floatValue = this.value
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
                        clip = true
                        compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                    }
                    .padding(horizontal = if (isLandscape) 24.dp else 12.dp, vertical = 6.dp)
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (expansionStage == QsExpansionStage.FULL_QS && !isLandscape) {
                        Modifier.wrapContentHeight()
                    } else {
                        Modifier.fillMaxHeight()
                    }
                )
                .widthIn(max = if (isLandscape) 920.dp else 640.dp)
                .align(Alignment.TopCenter)
        ) {
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

            Spacer(modifier = Modifier.height(6.dp))

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
                            .then(
                                if (expansionStage == QsExpansionStage.FULL_QS && !isEditingTiles) {
                                    Modifier.wrapContentHeight()
                                } else {
                                    Modifier.weight(1f, fill = false)
                                }
                            )
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
                            onTilesReordered = { activeList, inactiveList ->
                                editActiveTiles = activeList
                                editInactiveTiles = inactiveList
                            },
                            dragMorphFraction = manualDragMorphFraction,
                            isDraggingMorph = isDraggingMorph,
                            shadeDragModifier = shadeDragModifier,
                            shadeDragToCloseModifier = shadeDragToCloseModifier
                        )
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
    val editButtonPos by PanelPreferences.editButtonPosition.collectAsState()
    val showDateInQqs by PanelPreferences.showDateInQqsHeader.collectAsState()
    val datePosState by PanelPreferences.datePosition.collectAsState()
    val batteryInfo = rememberQqsBatteryState()

    val effectiveDatePos = remember(datePosState, showDateInQqs) {
        if (showDateInQqs && datePosState == com.example.data.DatePosition.BELOW_TIME) {
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
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = currentTime,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onTimeClick
                        )
                        .testTag("qqs_header_time")
                )
                if (effectiveDatePos == com.example.data.DatePosition.NEXT_TO_TIME) {
                    Text(
                        text = "•  $currentDate",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDateClick
                            )
                            .testTag("qqs_header_date_inline")
                    )
                }
            }

            if (effectiveDatePos == com.example.data.DatePosition.BELOW_TIME) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = currentDate,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDateClick
                            )
                            .testTag("qqs_header_date_below")
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (effectiveDatePos == com.example.data.DatePosition.HEADER_RIGHT) {
                Text(
                    text = currentDate,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDateClick
                        )
                        .padding(end = 4.dp)
                        .testTag("qqs_header_date_right")
                )
            }

            // Battery Indicator in QQS Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .testTag("qqs_header_battery_indicator")
            ) {
                Icon(
                    imageVector = if (batteryInfo.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    contentDescription = "Battery ${batteryInfo.percentage}%",
                    tint = if (batteryInfo.isCharging) Color(0xFF4CAF50) else if (batteryInfo.percentage <= 15) Color(0xFFFF5252) else Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "${batteryInfo.percentage}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (isEditingTiles) {
                Surface(
                    onClick = onToggleEditMode,
                    shape = RoundedCornerShape(16.dp),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("header_done_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Done",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            } else {
                if (editButtonPos == EditButtonPosition.HEADER || editButtonPos == EditButtonPosition.BOTH) {
                    IconButton(
                        onClick = onToggleEditMode,
                        modifier = Modifier.size(36.dp).testTag("header_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Tiles",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(36.dp).testTag("header_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "System Settings",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
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
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val density = LocalDensity.current

    var localActive by remember { mutableStateOf(activeTiles) }
    var localInactive by remember { mutableStateOf(inactiveTiles) }

    var draggedTileId by remember { mutableStateOf<String?>(null) }
    var dragSourceIndex by remember { mutableIntStateOf(-1) }
    var hoveredTargetIndex by remember { mutableIntStateOf(-1) }
    var currentDragCenter by remember { mutableStateOf(Offset.Zero) }
    var autoScrollDirection by remember { mutableIntStateOf(0) } // -1 = up, 1 = down, 0 = idle

    val slotBoundsMap = remember { mutableStateMapOf<Int, Rect>() }
    val tileBoundsMap = remember { mutableStateMapOf<String, Rect>() }
    var activeGridContainerBounds by remember { mutableStateOf<Rect?>(null) }
    var inactiveDropZoneBounds by remember { mutableStateOf<Rect?>(null) }
    var studioViewportBounds by remember { mutableStateOf<Rect?>(null) }
    var studioCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

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

    val isOverInactiveDropZone = draggedTileId != null &&
        inactiveDropZoneBounds != null &&
        currentDragCenter.y >= (inactiveDropZoneBounds?.top ?: Float.MAX_VALUE)

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
                Column {
                    Text(
                        text = "EDIT QUICK SETTINGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Hold & drag to reorder. Tap '-' to remove.",
                        fontSize = 11.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF666666)
                    )
                }

                Surface(
                    onClick = onDone,
                    shape = RoundedCornerShape(16.dp),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("inline_edit_done_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Done",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Active Tiles Grid Section
            val activeRows = remember(localActive, gridLayout.columns) {
                localActive.chunked(gridLayout.columns)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        activeGridContainerBounds = coords.boundsInRoot()
                    }
            ) {
                activeRows.forEachIndexed { rowIndex, rowTiles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowTiles.forEachIndexed { colIndex, tile ->
                            val slotIndex = rowIndex * gridLayout.columns + colIndex
                            val isDragging = draggedTileId == tile.id
                            key(slotIndex) {
                                Box(
                                    modifier = Modifier
                                        .width(targetTileWidth)
                                        .onGloballyPositioned { coords ->
                                            slotBoundsMap[slotIndex] = coords.boundsInRoot()
                                            tileBoundsMap[tile.id] = coords.boundsInRoot()
                                        }
                                        .pointerInput(slotIndex) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { offset ->
                                                    val currentTile = localActive.getOrNull(slotIndex) ?: return@detectDragGesturesAfterLongPress
                                                    draggedTileId = currentTile.id
                                                    dragSourceIndex = slotIndex
                                                    hoveredTargetIndex = -1
                                                    onDragStateChange(true)
                                                    val myBounds = slotBoundsMap[slotIndex] ?: tileBoundsMap[currentTile.id] ?: Rect.Zero
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
                                    val isHoverTarget = draggedTileId != null && hoveredTargetIndex == slotIndex
                                    if (isDragging) {
                                        // Open slot placeholder while the tile floats in isolated overlay
                                        Box(
                                            modifier = Modifier
                                                .width(targetTileWidth)
                                                .height(86.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(MonetPrimary.copy(alpha = 0.12f))
                                                .border(
                                                    width = 1.5.dp,
                                                    color = MonetPrimary.copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(16.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DragIndicator,
                                                contentDescription = null,
                                                tint = MonetPrimary.copy(alpha = 0.6f),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .width(targetTileWidth)
                                                .then(
                                                    if (isHoverTarget) {
                                                        Modifier
                                                            .clip(RoundedCornerShape(16.dp))
                                                            .background(MonetPrimary.copy(alpha = 0.22f))
                                                            .border(
                                                                width = 2.dp,
                                                                color = MonetPrimary,
                                                                shape = RoundedCornerShape(16.dp)
                                                            )
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                        ) {
                                            CircularQsTile(
                                                tile = tile,
                                                morphProgress = 1f,
                                                tileWidth = targetTileWidth,
                                                isInteractive = false,
                                                onClick = {},
                                                onLongClick = {}
                                            )

                                            // Minus remove button
                                            if (draggedTileId == null) {
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
                                                            val updatedActive = localActive.toMutableList()
                                                            val moved = updatedActive.firstOrNull { it.id == tile.id }
                                                            if (moved != null) {
                                                                updatedActive.remove(moved)
                                                                val updatedInactive = localInactive.toMutableList().apply {
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

            Spacer(modifier = Modifier.height(4.dp))

            val cardContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
            val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
            val availableTilesBg = if (isOverInactiveDropZone) {
                primaryColor.copy(alpha = 0.12f)
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

            // Inactive / Available Tiles Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        inactiveDropZoneBounds = coords.boundsInRoot()
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(availableTilesBg)
                    .then(
                        if (isOverInactiveDropZone) {
                            Modifier.border(2.dp, primaryColor, RoundedCornerShape(16.dp))
                        } else {
                            Modifier
                        }
                    )
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isOverInactiveDropZone) {
                            "DROP TO REMOVE FROM ACTIVE PANEL"
                        } else {
                            "AVAILABLE TILES (${localInactive.size})"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (isOverInactiveDropZone) "Release to drop" else "Tap '+' or drag to add",
                        fontSize = 10.sp,
                        color = if (isDark) Color.Gray else Color(0xFF666666)
                    )
                }

                if (localInactive.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All tiles are currently in your active panel",
                            fontSize = 12.sp,
                            color = if (isDark) Color.Gray else Color(0xFF888888)
                        )
                    }
                } else {
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

                    val systemInactive = localInactive.filter { !it.isExternal }
                    val externalInactive = localInactive.filter { it.isExternal }

                    if (systemInactive.isNotEmpty()) {
                        Text(
                            text = "SYSTEM TILES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                        val systemRows = remember(systemInactive, gridLayout.columns) {
                            systemInactive.chunked(gridLayout.columns)
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            systemRows.forEach { rowTiles ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    rowTiles.forEach { tile ->
                                        InactiveTileItem(
                                            tile = tile,
                                            targetTileWidth = targetTileWidth,
                                            draggedTileId = draggedTileId,
                                            tileBoundsMap = tileBoundsMap,
                                            onDragStart = { offset ->
                                                draggedTileId = tile.id
                                                onDragStateChange(true)
                                                val myBounds = tileBoundsMap[tile.id] ?: Rect.Zero
                                                currentDragCenter = myBounds.topLeft + offset
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            onDrag = { dragAmount ->
                                                currentDragCenter += dragAmount
                                                checkAutoScroll(currentDragCenter)
                                            },
                                            onDragEnd = {
                                                autoScrollDirection = 0
                                                onDragStateChange(false)
                                                val dropBounds = inactiveDropZoneBounds
                                                val activeContainer = activeGridContainerBounds

                                                val isDraggedToActive = (activeContainer != null && currentDragCenter.y <= activeContainer.bottom + 10f) ||
                                                    (dropBounds != null && currentDragCenter.y < dropBounds.top)

                                                if (isDraggedToActive) {
                                                    var targetSlot = localActive.size
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
                                                autoScrollDirection = 0
                                                onDragStateChange(false)
                                                draggedTileId = null
                                            },
                                            onClick = {
                                                handleAddTileToActive(tile, null)
                                            }
                                        )
                                    }
                                    repeat(gridLayout.columns - rowTiles.size) {
                                        Spacer(modifier = Modifier.width(targetTileWidth))
                                    }
                                }
                            }
                        }
                    }

                    if (externalInactive.isNotEmpty()) {
                        Text(
                            text = "THIRD-PARTY APP TILES",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                        )
                        val externalRows = remember(externalInactive, gridLayout.columns) {
                            externalInactive.chunked(gridLayout.columns)
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            externalRows.forEach { rowTiles ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    rowTiles.forEach { tile ->
                                        InactiveTileItem(
                                            tile = tile,
                                            targetTileWidth = targetTileWidth,
                                            draggedTileId = draggedTileId,
                                            tileBoundsMap = tileBoundsMap,
                                            onDragStart = { offset ->
                                                draggedTileId = tile.id
                                                onDragStateChange(true)
                                                val myBounds = tileBoundsMap[tile.id] ?: Rect.Zero
                                                currentDragCenter = myBounds.topLeft + offset
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            onDrag = { dragAmount ->
                                                currentDragCenter += dragAmount
                                                checkAutoScroll(currentDragCenter)
                                            },
                                            onDragEnd = {
                                                autoScrollDirection = 0
                                                onDragStateChange(false)
                                                val dropBounds = inactiveDropZoneBounds
                                                val activeContainer = activeGridContainerBounds

                                                val isDraggedToActive = (activeContainer != null && currentDragCenter.y <= activeContainer.bottom + 10f) ||
                                                    (dropBounds != null && currentDragCenter.y < dropBounds.top)

                                                if (isDraggedToActive) {
                                                    var targetSlot = localActive.size
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
                                                autoScrollDirection = 0
                                                onDragStateChange(false)
                                                draggedTileId = null
                                            },
                                            onClick = {
                                                handleAddTileToActive(tile, null)
                                            }
                                        )
                                    }
                                    repeat(gridLayout.columns - rowTiles.size) {
                                        Spacer(modifier = Modifier.width(targetTileWidth))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Done Action Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onDone,
                    shape = RoundedCornerShape(20.dp),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("edit_mode_done_bottom_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Done Editing",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
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
                            shadowElevation = 24f
                        }
                ) {
                    CircularQsTile(
                        tile = draggedTile,
                        morphProgress = 1f,
                        tileWidth = targetTileWidth,
                        isInteractive = false,
                        onClick = {},
                        onLongClick = {}
                    )
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
    val brightnessAtBottom by PanelPreferences.brightnessAtBottom.collectAsState()
    val brightnessInQqs by PanelPreferences.brightnessInQqs.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val carrierName = remember(context) {
        try {
            val tm = context.getSystemService(android.content.Context.TELEPHONY_SERVICE) as? android.telephony.TelephonyManager
            val name = tm?.networkOperatorName
            if (!name.isNullOrBlank() && name != "Android") name else "Google Fi"
        } catch (e: Exception) {
            "Google Fi"
        }
    }
    val activeTiles = remember(tiles, isEditingTiles, editActiveTiles) {
        if (isEditingTiles && editActiveTiles != null) {
            editActiveTiles
        } else {
            tiles.filter { it.isVisible }
        }
    }
    val inactiveTiles = remember(tiles, isEditingTiles, editInactiveTiles) {
        if (isEditingTiles && editInactiveTiles != null) {
            editInactiveTiles
        } else {
            tiles.filter { !it.isVisible }
        }
    }
    val qqsTileCount by PanelPreferences.qqsTileCount.collectAsState()
    val qqsTiles = remember(activeTiles, qqsTileCount) {
        activeTiles.take(qqsTileCount.coerceIn(2, 6))
    }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
        animationSpec = if (isDraggingMorph) {
            androidx.compose.animation.core.snap()
        } else {
            spring(
                dampingRatio = 0.80f,
                stiffness = 380f
            )
        },
        label = "qs_morph_progress"
    )

    val isSplitCarrier = showCarrier && showCarrierDivider && morphProgress < 0.5f && !isLandscape && !isEditingTiles

    val qsCardContent: @Composable () -> Unit = {
        // UNIFIED QS CARD (Morphs between QQS and Full QS, or displays Edit Mode Studio)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val qsCardCorner = 20.dp
            ContainerCard(
                shape = if (isSplitCarrier) {
                    RoundedCornerShape(topStart = qsCardCorner, topEnd = qsCardCorner, bottomStart = 4.dp, bottomEnd = 4.dp)
                } else {
                    RoundedCornerShape(qsCardCorner)
                },
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = 0.80f,
                            stiffness = 380f
                        )
                    )
                    .then(shadeDragModifier)
            ) {
                val isQqsMode = morphProgress < 0.5f && !isLandscape
                val isBottomSliderInQqs = brightnessInQqs && brightnessAtBottom
                val shouldReduceQqsContainer = isQqsMode && hideQqsDragHandle && (
                    !isSplitCarrier || isBottomSliderInQqs
                )

                val topPadding = if (isSplitCarrier) 16.dp else 12.dp
                val bottomPadding = if (isSplitCarrier) {
                    if (shouldReduceQqsContainer) 14.dp else 16.dp
                } else {
                    if (shouldReduceQqsContainer) 10.dp else 12.dp
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
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
                        // 1. Brightness Slider (if not moved to bottom)
                        val showTopSlider = !brightnessAtBottom && (brightnessInQqs || isFullQs)
                        AnimatedVisibility(
                            visible = showTopSlider,
                            enter = expandVertically(spring(dampingRatio = 0.80f, stiffness = 380f)) + fadeIn(tween(200)),
                            exit = shrinkVertically(spring(dampingRatio = 0.80f, stiffness = 380f)) + fadeOut(tween(160))
                        ) {
                            val sliderHeight = 44.dp
                            BrightnessSliderRow(brightness, onBrightnessChange, 1f, sliderHeight, isAtBottom = false)
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            alpha = qqsAlpha
                                            scaleX = qqsScale
                                            scaleY = qqsScale
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
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
                                    val viewPagerHeightDp = (100.dp * maxRows) + (10.dp * (maxRows - 1).coerceAtLeast(0))

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
                                BrightnessSliderRow(brightness, onBrightnessChange, 1f, sliderHeight, isAtBottom = true, verticalOffset = 0.dp)
                            }
                        }

                        if (!isSplitCarrier) {
                            val bottomSpacerHeight = 10.dp
                            Spacer(modifier = Modifier.height(bottomSpacerHeight))
                        }

                        // 3. Footer Bar / Drag Pill (Morphing footer)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(
                                    animationSpec = spring(
                                        dampingRatio = 0.80f,
                                        stiffness = 380f
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (morphProgress < 0.5f && !isLandscape) {
                                val qqsFooterAlpha = ((0.5f - morphProgress) / 0.5f).coerceIn(0f, 1f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer { alpha = qqsFooterAlpha }
                                ) {
                                    if (showCarrier && !isSplitCarrier) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            modifier = Modifier
                                                .align(Alignment.CenterStart)
                                                .padding(start = 9.dp, top = if (brightnessAtBottom && showCarrier) 9.dp else 12.dp, bottom = 6.dp)
                                        ) {
                                            val carrierColor = if (isDark) Color.LightGray.copy(alpha = 0.9f) else Color(0xFF333333)
                                            Icon(
                                                imageVector = Icons.Default.CellTower,
                                                contentDescription = null,
                                                tint = carrierColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = carrierName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = carrierColor
                                            )
                                        }
                                    }

                                    if (!hideQqsDragHandle && !isSplitCarrier) {
                                        Box(
                                            modifier = Modifier
                                                .align(if (!showCarrier) Alignment.Center else Alignment.BottomCenter)
                                                .padding(bottom = 0.dp)
                                                .offset(y = 3.dp)
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
                            } else {
                                val fullQsFooterAlpha = if (isLandscape) 1f else ((morphProgress - 0.5f) / 0.5f).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer { alpha = fullQsFooterAlpha }
                                    .padding(vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val footerTint = if (isDark) Color(0xFFCCCCCC) else Color(0xFF333333)
                                val editPos by PanelPreferences.editButtonPosition.collectAsState()
                                IconButton(
                                    onClick = onCollapseToQqs,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .align(Alignment.CenterStart)
                                        .testTag("collapse_to_qqs_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Collapse",
                                        tint = footerTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Page Indicator Dots
                                if (tilePages.size > 1) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.align(Alignment.Center)
                                    ) {
                                        repeat(tilePages.size) { pageIdx ->
                                            val positionDiff = kotlin.math.abs(pageIdx - viewPagerScrollPosition)
                                            val progress = (1f - positionDiff).coerceIn(0f, 1f)
                                            val dotSize = androidx.compose.ui.unit.lerp(4.5.dp, 7.dp, progress)
                                            val dotColor = androidx.compose.ui.graphics.lerp(
                                                footerTint.copy(alpha = 0.35f),
                                                androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                                progress
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 2.5.dp)
                                                    .size(dotSize)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                                    .clickable {
                                                        viewPagerRef?.setCurrentItem(pageIdx, true)
                                                    }
                                            )
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    if (editPos == EditButtonPosition.FOOTER || editPos == EditButtonPosition.BOTH) {
                                        IconButton(
                                            onClick = onToggleEditMode,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .testTag("footer_edit_tiles_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Tiles",
                                                tint = footerTint,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
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
                                        },
                                        modifier = Modifier.size(32.dp).testTag("footer_power_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PowerSettingsNew,
                                            contentDescription = "Power Menu",
                                            tint = footerTint,
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

        if (isSplitCarrier) {
            ContainerCard(
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        val carrierColor = if (isDark) Color.LightGray.copy(alpha = 0.9f) else Color(0xFF333333)
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

    val liveMediaState by com.example.service.MediaSessionManagerHelper.mediaState.collectAsState()
    val mediaSessionsList by com.example.service.MediaSessionManagerHelper.sessionsList.collectAsState()
    val selectedSessionIndex by com.example.service.MediaSessionManagerHelper.selectedSessionIndex.collectAsState()

    LaunchedEffect(Unit) {
        com.example.service.MediaSessionManagerHelper.refreshState()
    }

    val mediaCardContent: @Composable () -> Unit = {
        if (!isEditingTiles && liveMediaState != null) {
            val media = liveMediaState!!
            val isMediaExpanded = isFullQs || isLandscape
            val mediaTopGap = 10.dp
            Spacer(modifier = Modifier.height(mediaTopGap))
            AdaptiveMediaPlayerCard(
                mediaState = media,
                isExpanded = isMediaExpanded,
                sessions = mediaSessionsList,
                selectedIndex = selectedSessionIndex,
                onSelectSession = { index ->
                    com.example.service.MediaSessionManagerHelper.selectSession(index)
                },
                cornerRadius = 18.dp,
                backgroundColor = if (isDark) Color(0xFF353A3B) else Color(0xFFE2E7EA),
                accentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
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
                onCardClick = {
                    com.example.service.MediaSessionManagerHelper.openActivePlayerApp(context)
                    onClosePanel()
                }
            )
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
                mediaCardContent()
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
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.80f,
                        stiffness = 380f
                    )
                )
                .then(
                    if (isFullQs && !isEditingTiles) {
                        Modifier.wrapContentHeight()
                    } else {
                        Modifier.fillMaxHeight()
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(ExpressiveCardShapes.CardSpacing)
        ) {
            qsCardContent()
            mediaCardContent()

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

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        for (rowIndex in 0 until maxRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (rowIndex < rows.size) {
                    val rowTiles = rows[rowIndex]
                    rowTiles.forEach { tile ->
                        key(tile.id) {
                            CircularQsTile(
                                tile = tile,
                                morphProgress = 1f,
                                tileWidth = targetTileWidth,
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
    val hostView = androidx.compose.ui.platform.LocalView.current
    val panelRootView = remember(hostView) {
        var parent = hostView.parent
        while (parent is android.view.View && parent.parent is android.view.View) {
            parent = parent.parent
        }
        parent as? android.view.View ?: hostView
    }

    AndroidView(
        factory = { ctx ->
            com.example.ui.notification.NotificationListView(ctx).apply {
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
                primaryColor = primaryColor
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .testTag("notifications_card")
    )
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
        item.actions.isNotEmpty() || !item.subText.isNullOrBlank() || item.text.length > 40 || item.bigPictureBitmap != null
    }

    val resolvedAppName = item.appName.ifBlank { item.packageName }
    val timeFormatted = remember(item.postTime) {
        val date = java.util.Date(item.postTime)
        val format = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        format.format(date)
    }

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
                shape = shape,
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
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
                    }
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
                                Text(
                                    text = resolvedAppName,
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
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    val qqsTileSizeValue by PanelPreferences.qqsTileSize.collectAsState()
    val enabledTileShapePref by PanelPreferences.enabledTileShape.collectAsState()
    val disabledTileShapePref by PanelPreferences.disabledTileShape.collectAsState()
    val baseQqsSize = qqsTileSizeValue.dp.coerceIn(36.dp, 64.dp)

    val isActive = tile.state == TileState.ACTIVE
    val isUnavailable = tile.state == TileState.UNAVAILABLE

    val isDark = LocalIsDarkTheme.current
    val primaryColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
    val onPrimaryColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
    val cardContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant

    val chosenShape = if (isActive) enabledTileShapePref else disabledTileShapePref

    val tileShape = remember(chosenShape) {
        when (chosenShape) {
            TileShape.CIRCLE -> CircleShape
            TileShape.SQUIRCLE -> RoundedCornerShape(14.dp)
            TileShape.SQUARE -> RoundedCornerShape(4.dp)
            TileShape.TEARDROP -> RoundedCornerShape(topStart = 24.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            TileShape.PILL -> RoundedCornerShape(20.dp)
            // 8 Custom Shape Placeholders - easily customize shapes here
            TileShape.CUSTOM_1 -> RoundedCornerShape(topStart = 24.dp, topEnd = 6.dp, bottomEnd = 24.dp, bottomStart = 6.dp)
            TileShape.CUSTOM_2 -> RoundedCornerShape(topStart = 6.dp, topEnd = 24.dp, bottomEnd = 6.dp, bottomStart = 24.dp)
            TileShape.CUSTOM_3 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 6.dp, bottomStart = 6.dp)
            TileShape.CUSTOM_4 -> RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
            TileShape.CUSTOM_5 -> RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 4.dp, bottomStart = 20.dp)
            TileShape.CUSTOM_6 -> RoundedCornerShape(topStart = 24.dp, topEnd = 14.dp, bottomEnd = 24.dp, bottomStart = 14.dp)
            TileShape.CUSTOM_7 -> RoundedCornerShape(18.dp)
            TileShape.CUSTOM_8 -> RoundedCornerShape(8.dp)
        }
    }

    val circleSize = if (morphProgress >= 0.5f) 56.dp else baseQqsSize
    val iconSize = if (morphProgress >= 0.5f) 26.dp else baseQqsSize * 0.46f
    val labelAlpha = morphProgress.coerceIn(0f, 1f)
    val tileHeight = if (morphProgress >= 0.5f) 100.dp else baseQqsSize

    val (backgroundColor, iconColor) = remember(tile.state, isDark, primaryColor, onPrimaryColor, cardContainerColor) {
        val inactiveBg = if (isDark) {
            Color(
                red = (cardContainerColor.red * 0.85f).coerceIn(0f, 1f),
                green = (cardContainerColor.green * 0.85f).coerceIn(0f, 1f),
                blue = (cardContainerColor.blue * 0.85f).coerceIn(0f, 1f),
                alpha = cardContainerColor.alpha
            )
        } else {
            Color(
                red = (cardContainerColor.red + (1f - cardContainerColor.red) * 0.3f).coerceIn(0f, 1f),
                green = (cardContainerColor.green + (1f - cardContainerColor.green) * 0.3f).coerceIn(0f, 1f),
                blue = (cardContainerColor.blue + (1f - cardContainerColor.blue) * 0.3f).coerceIn(0f, 1f),
                alpha = cardContainerColor.alpha
            )
        }
        val inactiveText = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF333333)
        val unavailableBg = inactiveBg.copy(alpha = 0.5f)
        val unavailableText = inactiveText.copy(alpha = 0.4f)

        val bg = when (tile.state) {
            TileState.ACTIVE -> primaryColor
            TileState.INACTIVE -> inactiveBg
            TileState.UNAVAILABLE -> unavailableBg
        }
        val fg = when (tile.state) {
            TileState.ACTIVE -> onPrimaryColor
            TileState.INACTIVE -> inactiveText
            TileState.UNAVAILABLE -> unavailableText
        }
        Pair(bg, fg)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(tileWidth)
            .wrapContentHeight()
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
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(tileShape)
                .background(backgroundColor),
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

        if (labelAlpha > 0.02f) {
            Spacer(modifier = Modifier.height(4.dp))

            val tileLabelColor = if (isDark) Color(0xFFCCCCCC) else Color(0xFF333333)
            val tileSubtitleColor = if (isActive) androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else if (isDark) Color.Gray else Color(0xFF333333).copy(alpha = 0.8f)

            Text(
                text = tile.label,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = tileLabelColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = tile.subtitle,
                fontSize = 9.sp,
                color = tileSubtitleColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.offset(y = 1.dp),
                style = androidx.compose.ui.text.TextStyle(
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )
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

        val progressBarStyle by PanelPreferences.progressBarStyle.collectAsState()
        QsProgressSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            style = progressBarStyle,
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
    verticalOffset: androidx.compose.ui.unit.Dp = 0.dp
) {
    if (sliderAlpha > 0.01f) {
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
                .offset(y = verticalOffset)
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
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (brightness > 128) Icons.Default.BrightnessMedium else Icons.Default.BrightnessLow,
                        contentDescription = "Brightness",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                val cardContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                val isDark = LocalIsDarkTheme.current
                val inactiveTrackColor = if (isDark) {
                    Color(
                        red = (cardContainerColor.red * 0.85f).coerceIn(0f, 1f),
                        green = (cardContainerColor.green * 0.85f).coerceIn(0f, 1f),
                        blue = (cardContainerColor.blue * 0.85f).coerceIn(0f, 1f),
                        alpha = cardContainerColor.alpha
                    )
                } else {
                    // Same color as inactive tiles in light mode (30% lighter than container color)
                    Color(
                        red = (cardContainerColor.red + (1f - cardContainerColor.red) * 0.3f).coerceIn(0f, 1f),
                        green = (cardContainerColor.green + (1f - cardContainerColor.green) * 0.3f).coerceIn(0f, 1f),
                        blue = (cardContainerColor.blue + (1f - cardContainerColor.blue) * 0.3f).coerceIn(0f, 1f),
                        alpha = cardContainerColor.alpha
                    )
                }

                val progressBarStyle by PanelPreferences.progressBarStyle.collectAsState()
                QsProgressSlider(
                    value = brightness.toFloat(),
                    onValueChange = { onBrightnessChange(it.toInt()) },
                    valueRange = 5f..255f,
                    style = progressBarStyle,
                    activeColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    inactiveColor = inactiveTrackColor,
                    thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("brightness_slider")
                )

                val percentageColor = if (isDark) Color.White else Color(0xFF333333)

                Text(
                    text = "${((brightness / 255f) * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = percentageColor
                )
            }
        }
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
    containerColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(shape)
            .background(containerColor, shape)
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

@Composable
fun ContainerCard(
    shape: androidx.compose.ui.graphics.Shape = ExpressiveCardShapes.SingleCard,
    modifier: Modifier = Modifier,
    containerColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
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
            Box(
                modifier = Modifier
                    .width(targetTileWidth)
                    .height(86.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MonetPrimary.copy(alpha = 0.12f))
                    .border(
                        width = 1.5.dp,
                        color = MonetPrimary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DragIndicator,
                    contentDescription = null,
                    tint = MonetPrimary.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Box(modifier = Modifier.width(targetTileWidth)) {
                CircularQsTile(
                    tile = tile,
                    morphProgress = 1f,
                    tileWidth = targetTileWidth,
                    isInteractive = false,
                    onClick = {},
                    onLongClick = {}
                )

                // Plus add button
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

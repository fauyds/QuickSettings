package com.example.ui.tile

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ComplicationAction
import com.example.model.ComplicationLayoutMode
import com.example.model.ComplicationSublinePlacement
import com.example.model.ComplicationTileModel
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.QuickPanelTheme

/**
 * XML-inflatable Wear OS Complication + Quick Settings Tile hybrid View component.
 *
 * Inflates [R.layout.view_complication_tile] containing a Jetpack Compose [ComposeView] that draws
 * the circular container, centered icon, animated Canvas progress arc, and dynamic subline.
 *
 * Supports XML attributes via [R.styleable.ComplicationTile] as well as runtime [bind] updates.
 */
class ComplicationTile @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val composeView: ComposeView
    private val modelState = mutableStateOf(defaultInitialModel(context, attrs))
    private val darkThemeState = mutableStateOf(resolveSystemDarkMode(context))
    private val showSurfaceCardState = mutableStateOf(true)
    private val isInteractiveState = mutableStateOf(true)
    private val showTitleState = mutableStateOf(true)
    private val showBelowSublineState = mutableStateOf(true)

    var onActionListener: ((ComplicationAction) -> Unit)? = null
    var onSettingsListener: ((String) -> Unit)? = null

    init {
        clipChildren = false
        clipToPadding = false
        LayoutInflater.from(context).inflate(R.layout.view_complication_tile, this, true)
        composeView = findViewById(R.id.compose_complication_tile)
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setupComposeContent()
    }

    private fun setupComposeContent() {
        composeView.setContent {
            val currentModel = modelState.value
            val isDark = darkThemeState.value
            val showCard = showSurfaceCardState.value
            val interactive = isInteractiveState.value
            val showTitle = showTitleState.value
            val showBelowSubline = showBelowSublineState.value

            QuickPanelTheme(darkTheme = isDark) {
                ComplicationTileView(
                    model = currentModel,
                    showGridCardContainer = showCard,
                    isInteractive = interactive,
                    showTitle = showTitle,
                    showBelowSubline = showBelowSubline,
                    onAction = { action ->
                        val customListener = onActionListener
                        if (customListener != null) {
                            customListener.invoke(action)
                        } else {
                            ComplicationTileController.executeAction(context, action)
                        }
                    },
                    onOpenSettings = { settingsAction ->
                        val customSettings = onSettingsListener
                        if (customSettings != null) {
                            customSettings.invoke(settingsAction)
                        } else {
                            ComplicationTileController.openSettings(context, settingsAction)
                        }
                    }
                )
            }
        }
    }

    fun setParentComposition(parent: CompositionContext) {
        composeView.setParentCompositionContext(parent)
    }

    fun bind(model: ComplicationTileModel) {
        modelState.value = model
    }

    var model: ComplicationTileModel
        get() = modelState.value
        set(value) {
            modelState.value = value
        }

    fun setProgress(progress: Float) {
        modelState.value = modelState.value.copy(progress = progress.coerceIn(0f, 1f))
    }

    fun setSubline(subline: String) {
        modelState.value = modelState.value.copy(subline = subline)
    }

    fun setTitle(title: String) {
        modelState.value = modelState.value.copy(title = title)
    }

    fun setAccentColor(@ColorInt color: Int) {
        modelState.value = modelState.value.copy(accentColor = Color(color))
    }

    fun setSublinePlacement(placement: ComplicationSublinePlacement) {
        modelState.value = modelState.value.copy(sublinePlacement = placement)
    }

    fun setLayoutMode(mode: ComplicationLayoutMode) {
        modelState.value = modelState.value.copy(layoutMode = mode)
    }

    fun setDarkTheme(isDark: Boolean) {
        darkThemeState.value = isDark
    }

    fun setShowGridCardContainer(show: Boolean) {
        showSurfaceCardState.value = show
    }

    fun setInteractive(interactive: Boolean) {
        isInteractiveState.value = interactive
    }

    fun setShowTitle(show: Boolean) {
        showTitleState.value = show
    }

    fun setShowBelowSubline(show: Boolean) {
        showBelowSublineState.value = show
    }

    companion object {
        private fun resolveSystemDarkMode(context: Context): Boolean {
            val nightMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightMask == Configuration.UI_MODE_NIGHT_YES
        }

        private fun defaultInitialModel(context: Context, attrs: AttributeSet?): ComplicationTileModel {
            if (attrs == null) {
                return ComplicationTileModel(
                    id = "complication_tile",
                    title = "Status",
                    subline = "78%",
                    progress = 0.78f,
                    icon = Icons.Default.Speed,
                    tapAction = ComplicationAction.Toggle(tileId = "battery_saver"),
                    settingsAction = Settings.ACTION_SETTINGS
                )
            }

            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ComplicationTile)
            return try {
                val title = typedArray.getString(R.styleable.ComplicationTile_ctTitle) ?: "Metric"
                val subline = typedArray.getString(R.styleable.ComplicationTile_ctSubline) ?: "0%"
                val progress = typedArray.getFloat(R.styleable.ComplicationTile_ctProgress, 0.5f).coerceIn(0f, 1f)
                val accentInt = if (typedArray.hasValue(R.styleable.ComplicationTile_ctAccentColor)) {
                    typedArray.getColor(R.styleable.ComplicationTile_ctAccentColor, 0)
                } else {
                    0
                }
                val trackInt = if (typedArray.hasValue(R.styleable.ComplicationTile_ctTrackColor)) {
                    typedArray.getColor(R.styleable.ComplicationTile_ctTrackColor, 0)
                } else {
                    0
                }
                val iconResId = typedArray.getResourceId(R.styleable.ComplicationTile_ctIcon, 0).takeIf { it != 0 }
                val isActive = typedArray.getBoolean(R.styleable.ComplicationTile_ctIsActive, false)
                val placementIdx = typedArray.getInt(R.styleable.ComplicationTile_ctSublinePlacement, 0)
                val layoutModeIdx = typedArray.getInt(R.styleable.ComplicationTile_ctLayoutMode, 0)
                val actionTypeIdx = typedArray.getInt(R.styleable.ComplicationTile_ctActionType, 0)
                val actionTarget = typedArray.getString(R.styleable.ComplicationTile_ctActionTarget) ?: "battery_saver"
                val settingsAction = typedArray.getString(R.styleable.ComplicationTile_ctSettingsAction) ?: Settings.ACTION_SETTINGS

                val placement = if (placementIdx == 1) {
                    ComplicationSublinePlacement.BELOW_RING
                } else {
                    ComplicationSublinePlacement.INSIDE_RING
                }
                val layoutMode = when (layoutModeIdx) {
                    1 -> ComplicationLayoutMode.HEADER_ROW
                    2 -> ComplicationLayoutMode.GRID_TILE
                    else -> ComplicationLayoutMode.ADAPTIVE
                }
                val tapAction = when (actionTypeIdx) {
                    1 -> ComplicationAction.LaunchApp(packageName = actionTarget)
                    2 -> ComplicationAction.Shortcut(
                        shortcutId = actionTarget,
                        intentAction = actionTarget.takeIf { it.contains(".") }
                    )
                    else -> ComplicationAction.Toggle(tileId = actionTarget, isActive = isActive)
                }

                val fallbackIcon = inferDefaultIcon(actionTarget, title)

                ComplicationTileModel(
                    id = "xml_${title.lowercase().replace(' ', '_')}",
                    title = title,
                    subline = subline,
                    progress = progress,
                    icon = if (iconResId == null) fallbackIcon else null,
                    iconResId = iconResId,
                    accentColor = if (accentInt != 0) Color(accentInt) else null,
                    trackColor = if (trackInt != 0) Color(trackInt) else null,
                    isActive = isActive,
                    sublinePlacement = placement,
                    layoutMode = layoutMode,
                    tapAction = tapAction,
                    settingsAction = settingsAction
                )
            } finally {
                typedArray.recycle()
            }
        }

        private fun inferDefaultIcon(target: String, title: String): ImageVector {
            val key = "$target $title".lowercase()
            return when {
                "battery" in key -> Icons.Default.BatteryFull
                "bright" in key -> Icons.Default.BrightnessMedium
                "storage" in key -> Icons.Default.Storage
                "sound" in key || "volume" in key || "dnd" in key -> Icons.Default.VolumeUp
                else -> Icons.Default.Bolt
            }
        }
    }
}

/**
 * Pure Compose Wear OS Complication + Quick Settings Tile hybrid component.
 * Uses [BoxWithConstraints] to adapt cleanly between compact header rows and full grid layouts.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ComplicationTileView(
    model: ComplicationTileModel,
    modifier: Modifier = Modifier,
    showGridCardContainer: Boolean = false,
    isInteractive: Boolean = true,
    showTitle: Boolean = true,
    showBelowSubline: Boolean = true,
    customRingSize: Dp? = null,
    onClickOverride: (() -> Unit)? = null,
    onLongClickOverride: (() -> Unit)? = null,
    onAction: ((ComplicationAction) -> Unit)? = null,
    onOpenSettings: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = LocalIsDarkTheme.current
    val colorScheme = MaterialTheme.colorScheme

    val resolvedAccentTarget = model.accentColor ?: colorScheme.primary
    val animatedAccent by animateColorAsState(
        targetValue = resolvedAccentTarget,
        animationSpec = tween(durationMillis = 300),
        label = "complication_accent_${model.id}"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = model.progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "complication_progress_${model.id}"
    )

    val inactiveTileColor = com.example.ui.theme.getInactiveTilesElementColor(isDark)

    val ringFillColor by animateColorAsState(
        targetValue = if (model.isActive) {
            animatedAccent
        } else {
            inactiveTileColor
        },
        animationSpec = tween(durationMillis = 250),
        label = "complication_fill_${model.id}"
    )

    val trackColor = if (model.isActive) {
        Color.White.copy(alpha = 0.28f)
    } else {
        if (isDark) animatedAccent.copy(alpha = 0.20f) else animatedAccent.copy(alpha = 0.16f)
    }

    val progressArcColor = if (model.isActive) {
        Color.White
    } else {
        animatedAccent
    }

    val iconTint = if (model.isActive) colorScheme.onPrimary else colorScheme.onSurface
    val sublineColor = if (model.isActive) colorScheme.onPrimary.copy(alpha = 0.92f) else colorScheme.onSurfaceVariant

    val handleTap = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (onClickOverride != null) {
            onClickOverride.invoke()
        } else if (onAction != null) {
            onAction.invoke(model.tapAction)
        } else {
            ComplicationTileController.executeAction(context, model.tapAction)
        }
    }

    val handleLongPress = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (onLongClickOverride != null) {
            onLongClickOverride.invoke()
        } else if (onOpenSettings != null) {
            onOpenSettings.invoke(model.settingsAction)
        } else {
            ComplicationTileController.openSettings(context, model.settingsAction)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = model.contentDescription
            }
            .testTag("complication_tile_${model.id}"),
        contentAlignment = Alignment.Center
    ) {
        val isCompactHeader = when (model.layoutMode) {
            ComplicationLayoutMode.HEADER_ROW -> true
            ComplicationLayoutMode.GRID_TILE -> false
            ComplicationLayoutMode.ADAPTIVE -> {
                (maxHeight.value.isFinite() && maxHeight < 72.dp) ||
                    (maxWidth.value.isFinite() && maxWidth < 64.dp)
            }
        }

        val ringSize: Dp = customRingSize ?: if (isCompactHeader) {
            if (model.sublinePlacement == ComplicationSublinePlacement.INSIDE_RING) 46.dp else 40.dp
        } else {
            val available = if (maxWidth.value.isFinite()) maxWidth else 88.dp
            (available * 0.68f).coerceIn(54.dp, 68.dp)
        }

        val strokeWidthDp: Dp = if (ringSize <= 48.dp) 3.dp else 4.dp
        val iconSize: Dp = when {
            ringSize <= 48.dp && model.sublinePlacement == ComplicationSublinePlacement.INSIDE_RING ->
                (ringSize * 0.34f).coerceIn(13.dp, 17.dp)
            ringSize <= 48.dp -> (ringSize * 0.42f).coerceIn(16.dp, 22.dp)
            model.sublinePlacement == ComplicationSublinePlacement.INSIDE_RING ->
                (ringSize * 0.32f).coerceIn(18.dp, 22.dp)
            else -> (ringSize * 0.40f).coerceIn(22.dp, 28.dp)
        }

        val containerBgModifier = if (!isCompactHeader && showGridCardContainer) {
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isDark) colorScheme.surfaceContainer.copy(alpha = 0.72f)
                    else colorScheme.surface
                )
                .border(
                    width = 0.8.dp,
                    color = colorScheme.outlineVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 8.dp, vertical = 10.dp)
        } else if (customRingSize != null) {
            Modifier
        } else {
            Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
        }

        Column(
            modifier = containerBgModifier
                .then(
                    if (isInteractive) {
                        Modifier.combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = if (!isCompactHeader && showGridCardContainer) {
                                ripple(color = animatedAccent)
                            } else {
                                null
                            },
                            role = Role.Button,
                            onClick = handleTap,
                            onLongClick = handleLongPress
                        )
                    } else {
                        Modifier
                    }
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Complication Ring Container (enforces >= 48dp touch area on header items cleanly)
            Box(
                modifier = Modifier
                    .size(ringSize)
                    .clip(CircleShape)
                    .background(ringFillColor)
                    .then(
                        if (isInteractive && (isCompactHeader || !showGridCardContainer)) {
                            Modifier.combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = animatedAccent),
                                role = Role.Button,
                                onClick = handleTap,
                                onLongClick = handleLongPress
                            )
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Smooth circular progress arc drawn via Compose Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = strokeWidthDp.toPx()
                    val inset = strokePx / 2f + 1.dp.toPx()
                    val arcSize = Size(
                        width = (size.width - inset * 2f).coerceAtLeast(1f),
                        height = (size.height - inset * 2f).coerceAtLeast(1f)
                    )
                    val topLeft = Offset(inset, inset)

                    // Background track ring
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )

                    // Smooth progress arc (0f..1f -> 0°..360° clockwise from 12 o'clock)
                    if (animatedProgress > 0.005f) {
                        drawArc(
                            color = progressArcColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center content: Icon + optional Inside-Ring dynamic subline
                if (model.sublinePlacement == ComplicationSublinePlacement.INSIDE_RING && model.subline.isNotBlank()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        ComplicationCenterIcon(
                            icon = model.icon,
                            iconResId = model.iconResId,
                            tint = iconTint,
                            size = iconSize,
                            contentDescription = null
                        )
                        Text(
                            text = model.subline,
                            fontSize = if (isCompactHeader) 9.sp else 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = sublineColor,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            style = TextStyle(
                                platformStyle = PlatformTextStyle(includeFontPadding = false),
                                fontFeatureSettings = "tnum"
                            )
                        )
                    }
                } else {
                    ComplicationCenterIcon(
                        icon = model.icon,
                        iconResId = model.iconResId,
                        tint = iconTint,
                        size = iconSize,
                        contentDescription = null
                    )
                }
            }

            // Below-Ring dynamic subline and/or Grid Tile title
            val showSublineBelow = showBelowSubline && model.sublinePlacement == ComplicationSublinePlacement.BELOW_RING && model.subline.isNotBlank()
            val showTitleBelow = showTitle && !isCompactHeader && model.title.isNotBlank()

            if (showSublineBelow || showTitleBelow) {
                Spacer(modifier = Modifier.height(if (isCompactHeader || !showTitle) 2.dp else 5.dp))
            }

            if (showTitleBelow) {
                Text(
                    text = model.title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 96.dp),
                    style = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))
                )
            }

            if (showSublineBelow) {
                Text(
                    text = model.subline,
                    fontSize = if (isCompactHeader) 10.sp else 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = sublineColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                        fontFeatureSettings = "tnum"
                    )
                )
            }
        }
    }
}

@Composable
private fun ComplicationCenterIcon(
    icon: ImageVector?,
    iconResId: Int?,
    tint: Color,
    size: Dp,
    contentDescription: String?
) {
    when {
        icon != null -> {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(size)
            )
        }
        iconResId != null -> {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = contentDescription,
                colorFilter = ColorFilter.tint(tint),
                modifier = Modifier.size(size)
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(size)
            )
        }
    }
}

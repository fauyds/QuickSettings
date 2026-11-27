package com.example.ui.overlay

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.RuntimeColorFilter
import android.graphics.RuntimeShader
import android.graphics.RuntimeXfermode
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewOutlineProvider
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CornerBasedShape
import com.example.data.GlassBackdropMode
import com.example.data.RefractionStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline as ComposeOutline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RenderEffect as ComposeRenderEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.data.PanelPreferences
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import java.util.function.Consumer
import kotlin.math.roundToInt

const val DEFAULT_FROSTED_BLUR_RADIUS_DP = 24f
const val MAX_REFRACTION_REGIONS = 18

/**
 * Bounding rectangle (in host root coordinates) and corner radii for the QS/QQS card container.
 * Used when `blurOnlyBehindQsCard` is enabled so only pixels directly behind the QS/QQS card blur.
 */
data class QsCardBlurBounds(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 0f,
    val bottom: Float = 0f,
    val topCornerRadiusPx: Float = 0f,
    val bottomCornerRadiusPx: Float = 0f
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)
    val isValid: Boolean get() = width > 1f && height > 1f

    fun translate(dx: Float, dy: Float): QsCardBlurBounds {
        if (!isValid) return this
        return copy(
            left = left + dx,
            top = top + dy,
            right = right + dx,
            bottom = bottom + dy
        )
    }
}

val LocalOnQsCardBoundsChanged = compositionLocalOf<((QsCardBlurBounds) -> Unit)?> { null }
val LocalOnBlurRegionBoundsChanged = compositionLocalOf<((String, QsCardBlurBounds) -> Unit)?> { null }
val LocalOnNotificationBlurRegionsChanged = compositionLocalOf<((List<QsCardBlurBounds>) -> Unit)?> { null }

/**
 * Attaches layout measurement to a frosted QS/QQS card so backdrop hosts can scope behind-panel
 * blur exclusively underneath the active QS/QQS cards (main QS card and QQS carrier footer)
 * when `blurOnlyBehindQsCard` is enabled.
 */
fun Modifier.reportQsCardBlurBounds(
    topCornerRadiusDp: Float,
    bottomCornerRadiusDp: Float = topCornerRadiusDp,
    regionKey: String = "qs_card",
    isVisible: Boolean = true
): Modifier = composed {
    val onRegionBoundsChanged = LocalOnBlurRegionBoundsChanged.current
    val onBoundsChanged = LocalOnQsCardBoundsChanged.current
    if (onRegionBoundsChanged == null && onBoundsChanged == null) return@composed this
    val density = LocalDensity.current
    val topCornerPx = with(density) { topCornerRadiusDp.coerceAtLeast(0f).dp.toPx() }
    val bottomCornerPx = with(density) { bottomCornerRadiusDp.coerceAtLeast(0f).dp.toPx() }

    DisposableEffect(onRegionBoundsChanged, onBoundsChanged, regionKey, isVisible) {
        if (!isVisible) {
            onRegionBoundsChanged?.invoke(regionKey, QsCardBlurBounds())
            if (regionKey == "qs_card") {
                onBoundsChanged?.invoke(QsCardBlurBounds())
            }
        }
        onDispose {
            onRegionBoundsChanged?.invoke(regionKey, QsCardBlurBounds())
            if (regionKey == "qs_card") {
                onBoundsChanged?.invoke(QsCardBlurBounds())
            }
        }
    }

    this.onGloballyPositioned { coords ->
        if (coords.isAttached) {
            val measured = if (isVisible) {
                val rect = coords.boundsInRoot()
                QsCardBlurBounds(
                    left = rect.left,
                    top = rect.top,
                    right = rect.right,
                    bottom = rect.bottom,
                    topCornerRadiusPx = topCornerPx,
                    bottomCornerRadiusPx = bottomCornerPx
                )
            } else {
                QsCardBlurBounds()
            }
            onRegionBoundsChanged?.invoke(regionKey, measured)
            if (regionKey == "qs_card") {
                onBoundsChanged?.invoke(measured)
            }
        }
    }
}

/**
 * Computes the pixel blur radius to pass to window blur APIs.
 * Quantizes intermediate animation values into 4px steps (or 8px steps in [performanceMode])
 * so SurfaceFlinger does not get flooded on every spring frame while still reaching exact
 * 0px (closed/off) and full target radius (open).
 */
fun computeWindowBlurBehindRadiusPx(
    enabled: Boolean,
    density: Float,
    fraction: Float = 1f,
    maxBlurDp: Float = DEFAULT_FROSTED_BLUR_RADIUS_DP,
    performanceMode: Boolean = false
): Int {
    if (!enabled || density <= 0f || maxBlurDp <= 0f || (performanceMode && maxBlurDp < 0.75f)) return 0
    val clampedFraction = fraction.coerceIn(0f, 1f)
    if (clampedFraction <= 0.01f) return 0
    val maxPx = (maxBlurDp * density).roundToInt().coerceAtLeast(1)
    if (performanceMode && maxPx <= 1) return 0
    if (clampedFraction >= 0.99f) return maxPx
    val rawPx = (maxPx * clampedFraction).roundToInt().coerceIn(0, maxPx)
    if (rawPx <= 0) return 0
    val step = if (performanceMode) 8 else 4
    return ((rawPx + step / 2) / step * step).coerceIn(step.coerceAtMost(maxPx), maxPx)
}

/**
 * Quantizes blur region coordinates to [stepPx] boundaries so sub-pixel layout jitter
 * does not force RenderEffect/RuntimeShader rebuilds across frames when Performance Mode is active.
 */
fun quantizeBlurRegionsForPerformance(
    regions: List<QsCardBlurBounds>,
    stepPx: Float = 2f
): List<QsCardBlurBounds> {
    if (regions.isEmpty() || stepPx <= 0f) return regions
    fun q(v: Float): Float = (v / stepPx).roundToInt() * stepPx
    return regions.map { b ->
        if (!b.isValid) b
        else b.copy(
            left = q(b.left),
            top = q(b.top),
            right = q(b.right),
            bottom = q(b.bottom)
        )
    }
}

/**
 * Routing plan separating full-screen behind-panel blur from QS/QQS-card-only bounded blur.
 */
data class QsBlurWindowPlan(
    val fullScreenBlurRadiusPx: Int,
    val qsCardBlurRadiusPx: Int
)

fun computeQsBlurWindowPlan(
    desiredRadiusPx: Int,
    blurOnlyBehindQsCard: Boolean
): QsBlurWindowPlan {
    val safeRadius = desiredRadiusPx.coerceAtLeast(0)
    return if (blurOnlyBehindQsCard) {
        QsBlurWindowPlan(fullScreenBlurRadiusPx = 0, qsCardBlurBlurRadiusZero = false, qsCardBlurRadiusPx = safeRadius)
    } else {
        QsBlurWindowPlan(fullScreenBlurRadiusPx = safeRadius, qsCardBlurBlurRadiusZero = true, qsCardBlurRadiusPx = 0)
    }
}

private fun QsBlurWindowPlan(
    fullScreenBlurRadiusPx: Int,
    qsCardBlurBlurRadiusZero: Boolean,
    qsCardBlurRadiusPx: Int
): QsBlurWindowPlan = QsBlurWindowPlan(
    fullScreenBlurRadiusPx = fullScreenBlurRadiusPx,
    qsCardBlurRadiusPx = if (qsCardBlurBlurRadiusZero) 0 else qsCardBlurRadiusPx
)

/**
 * Enables or disables full-screen behind-window blur on [params] via Android 12+ (API 31+)
 * [WindowManager.LayoutParams.FLAG_BLUR_BEHIND] and [WindowManager.LayoutParams.blurBehindRadius].
 * Note: FLAG_BLUR_BEHIND intentionally blurs the entire display behind the window; when scoping blur
 * exclusively behind the QS/QQS card, use [QsCardBlurWindowHost] (`Window.setBackgroundBlurRadius`).
 * Returns true if [params] changed and `WindowManager.updateViewLayout` should be invoked.
 */
fun applyWindowBlurBehind(
    params: WindowManager.LayoutParams,
    blurRadiusPx: Int
): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
    return applyWindowBlurBehindApi31(params, blurRadiusPx)
}

@RequiresApi(Build.VERSION_CODES.S)
private fun applyWindowBlurBehindApi31(
    params: WindowManager.LayoutParams,
    blurRadiusPx: Int
): Boolean {
    val prevFlags = params.flags
    val prevRadius = params.blurBehindRadius
    val clampedRadius = blurRadiusPx.coerceAtLeast(0)
    if (clampedRadius > 0) {
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
        params.blurBehindRadius = clampedRadius
    } else {
        params.flags = params.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
        params.blurBehindRadius = 0
    }
    return params.flags != prevFlags || params.blurBehindRadius != prevRadius
}

/**
 * Creates non-touchable, non-focusable [WindowManager.LayoutParams] for the backing window
 * used when "Blur Behind QS/QQS Only" is enabled. Note that FLAG_BLUR_BEHIND is never set here
 * because blur scoping uses [Window.setBackgroundBlurRadius] clipped strictly to the drawable bounds.
 */
fun createQsBlurBackdropWindowParams(layoutType: Int): WindowManager.LayoutParams {
    return WindowManager.LayoutParams(
        1,
        1,
        layoutType,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    ).apply {
        format = PixelFormat.TRANSLUCENT
        gravity = Gravity.TOP or Gravity.START
        x = 0
        y = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutInDisplayCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }
}

/**
 * Updates [blurParams] geometry for bounded [Window.setBackgroundBlurRadius] (NEVER sets FLAG_BLUR_BEHIND,
 * ensuring apps/home screen outside [bounds] remain completely sharp).
 * Returns true if geometry/flags changed.
 */
fun applyQsBlurBackdropParams(
    blurParams: WindowManager.LayoutParams,
    bounds: QsCardBlurBounds?,
    radiusPx: Int,
    offsetX: Int = 0,
    offsetY: Int = 0
): Boolean {
    val active = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        radiusPx > 0 &&
        bounds != null &&
        bounds.isValid

    val targetX: Int
    val targetY: Int
    val targetW: Int
    val targetH: Int

    if (active && bounds != null) {
        targetX = (bounds.left + offsetX).roundToInt()
        targetY = (bounds.top + offsetY).roundToInt()
        targetW = bounds.width.roundToInt().coerceAtLeast(1)
        targetH = bounds.height.roundToInt().coerceAtLeast(1)
    } else {
        targetX = 0
        targetY = 0
        targetW = 0
        targetH = 0
    }

    // Crucially ensure FLAG_BLUR_BEHIND is NEVER set on this bounded card blur window
    val blurBehindCleared = applyWindowBlurBehind(blurParams, 0)
    val geometryChanged = blurParams.x != targetX ||
        blurParams.y != targetY ||
        blurParams.width != targetW ||
        blurParams.height != targetH

    if (geometryChanged) {
        blurParams.x = targetX
        blurParams.y = targetY
        blurParams.width = targetW
        blurParams.height = targetH
    }

    return blurBehindCleared || geometryChanged
}

/**
 * Hosts a backing [Dialog] window (`Window.setBackgroundBlurRadius` on API 31+) added strictly
 * underneath the main overlay [ComposeView]. Unlike `FLAG_BLUR_BEHIND` (which blurs the full display),
 * `Window.setBackgroundBlurRadius` clips blur strictly to the QS/QQS card's rounded bounds.
 */
class QsCardBlurWindowHost(
    context: Context,
    private val layoutType: Int
) {
    private val backgroundDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        // Non-zero (1/255) alpha ensures GradientDrawable.getOutline() provides a valid RoundRect alpha
        // to ViewRootImpl.updateBackgroundBlurCorners() while remaining visually invisible.
        setColor(AndroidColor.argb(1, 0, 0, 0))
    }

    private val contentView = View(context).apply {
        setBackgroundColor(AndroidColor.TRANSPARENT)
    }

    private var dialog: Dialog? = null
    private var lastAppliedRadiusPx: Int = -1
    private var lastAppliedCornerPx: Float = -1f
    private var isShown: Boolean = false

    init {
        try {
            val d = Dialog(context, android.R.style.Theme_Translucent_NoTitleBar).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                setContentView(contentView)
            }
            d.window?.apply {
                setType(layoutType)
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                setDimAmount(0f)
                addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )
                setFormat(PixelFormat.TRANSLUCENT)
                setGravity(Gravity.TOP or Gravity.START)
                val lp = createQsBlurBackdropWindowParams(layoutType).apply {
                    token = attributes.token
                    packageName = context.packageName
                    title = "QsCardBlurBackdrop"
                }
                attributes = lp
                setBackgroundDrawable(backgroundDrawable)
                decorView.setPadding(0, 0, 0, 0)
                decorView.visibility = View.GONE
                decorView.outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View, outline: Outline) {
                        val corner = (view.tag as? Float)?.coerceAtLeast(0f) ?: 0f
                        outline.setRoundRect(0, 0, view.width, view.height, corner)
                    }
                }
                decorView.clipToOutline = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setBackgroundBlurRadius(0)
                }
            }
            dialog = d
        } catch (_: Throwable) {
            dialog = null
        }
    }

    fun showBehindOverlay() {
        val d = dialog ?: return
        if (isShown) return
        try {
            d.show()
            d.window?.decorView?.visibility = View.GONE
            isShown = true
        } catch (_: Throwable) {
            isShown = false
        }
    }

    fun update(
        bounds: QsCardBlurBounds?,
        radiusPx: Int,
        offsetX: Int = 0,
        offsetY: Int = 0
    ) {
        val d = dialog ?: return
        val window = d.window ?: return
        if (!isShown) return

        val active = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            radiusPx > 0 &&
            bounds != null &&
            bounds.isValid

        val decor = window.decorView
        val targetVisibility = if (active) View.VISIBLE else View.GONE

        if (active && bounds != null) {
            // Outline.getRadius() used by ViewRootImpl.updateBackgroundBlurCorners() expects uniform corner radius
            val cornerPx = maxOf(bounds.topCornerRadiusPx, bounds.bottomCornerRadiusPx).coerceAtLeast(0f)
            if (cornerPx != lastAppliedCornerPx) {
                lastAppliedCornerPx = cornerPx
                backgroundDrawable.cornerRadius = cornerPx
                window.setBackgroundDrawable(backgroundDrawable)
                decor.tag = cornerPx
                decor.invalidateOutline()
            }
        }

        val lp = WindowManager.LayoutParams().apply { copyFrom(window.attributes) }
        val paramsChanged = applyQsBlurBackdropParams(
            blurParams = lp,
            bounds = bounds,
            radiusPx = radiusPx,
            offsetX = offsetX,
            offsetY = offsetY
        )

        if (decor.visibility != targetVisibility) {
            decor.visibility = targetVisibility
        }

        if (paramsChanged) {
            try {
                window.attributes = lp
            } catch (_: Throwable) {
                // Ignore if window detached mid-frame
            }
        }

        val targetBlurRadius = if (active) radiusPx else 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && lastAppliedRadiusPx != targetBlurRadius) {
            lastAppliedRadiusPx = targetBlurRadius
            try {
                window.setBackgroundBlurRadius(targetBlurRadius)
            } catch (_: Throwable) {
                // Ignore on devices/builds rejecting dynamic background blur update
            }
        }
    }

    fun dismiss() {
        val d = dialog ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                d.window?.setBackgroundBlurRadius(0)
            }
            d.window?.setBackgroundDrawable(null)
            d.window?.decorView?.outlineProvider = null
            d.window?.decorView?.tag = null
            if (d.isShowing) {
                d.dismiss()
            }
        } catch (_: Throwable) {
            // Ignore cleanup exceptions
        } finally {
            isShown = false
            dialog = null
        }
    }
}

/**
 * Manages multiple keyed [QsCardBlurWindowHost] windows (main QS/QQS card, split QQS carrier footer,
 * and a pre-allocated pool of individual notification card blur windows) underneath the overlay so
 * each card receives hardware [Window.setBackgroundBlurRadius] strictly within its own rounded bounds
 * when `blurOnlyBehindQsCard` is enabled, keeping gaps and empty space sharp.
 */
class QsMultiCardBlurWindowHost(
    context: Context,
    layoutType: Int,
    regionKeys: List<String> = listOf("qs_card", "qqs_carrier_footer"),
    private val enabled: Boolean = true,
    private val notificationSlotCount: Int = MAX_NOTIFICATION_BLUR_SLOTS
) {
    private val hosts: Map<String, QsCardBlurWindowHost> = if (enabled) {
        regionKeys.associateWith { QsCardBlurWindowHost(context, layoutType) }
    } else {
        emptyMap()
    }

    private val notificationHosts: List<QsCardBlurWindowHost> = if (enabled) {
        List(notificationSlotCount.coerceAtLeast(0).coerceAtMost(MAX_NOTIFICATION_BLUR_SLOTS)) {
            QsCardBlurWindowHost(context, layoutType)
        }
    } else {
        emptyList()
    }

    fun showBehindOverlay() {
        if (!enabled) return
        hosts.values.forEach { it.showBehindOverlay() }
        notificationHosts.forEach { it.showBehindOverlay() }
    }

    fun updateAll(
        regions: Map<String, QsCardBlurBounds>,
        radiusPx: Int,
        offsetX: Int = 0,
        offsetY: Int = 0,
        notificationRegions: List<QsCardBlurBounds> = emptyList()
    ) {
        if (!enabled) return
        hosts.forEach { (key, host) ->
            val bounds = regions[key]
            host.update(
                bounds = bounds,
                radiusPx = radiusPx,
                offsetX = offsetX,
                offsetY = offsetY
            )
        }
        notificationHosts.forEachIndexed { index, host ->
            val bounds = notificationRegions.getOrNull(index)
            host.update(
                bounds = bounds,
                radiusPx = radiusPx,
                offsetX = offsetX,
                offsetY = offsetY
            )
        }
    }

    fun dismiss() {
        hosts.values.forEach { it.dismiss() }
        notificationHosts.forEach { it.dismiss() }
    }

    companion object {
        const val MAX_NOTIFICATION_BLUR_SLOTS = 16
    }
}

fun isCrossWindowBlurSupported(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
    return try {
        val wm = context.getSystemService(WindowManager::class.java)
        wm?.isCrossWindowBlurEnabled ?: false
    } catch (_: Throwable) {
        false
    }
}

@Composable
fun rememberCrossWindowBlurSupported(): Boolean {
    val context = LocalContext.current
    var supported by remember(context) {
        mutableStateOf(isCrossWindowBlurSupported(context))
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        DisposableEffect(context) {
            val wm = try {
                context.getSystemService(WindowManager::class.java)
            } catch (_: Throwable) {
                null
            }
            val listener = Consumer<Boolean> { enabled ->
                supported = enabled
            }
            try {
                wm?.addCrossWindowBlurEnabledListener(listener)
                wm?.let { supported = it.isCrossWindowBlurEnabled }
            } catch (_: Throwable) {
                // Keep snapshot value on environments without system WindowManager support
            }
            onDispose {
                try {
                    wm?.removeCrossWindowBlurEnabledListener(listener)
                } catch (_: Throwable) {
                    // Ignore cleanup failures
                }
            }
        }
    }

    return supported
}

private const val FROSTED_BACKDROP_AGSL = """
    uniform float saturationBoost;
    uniform float frostLift;
    uniform float tintMix;
    uniform half4 frostTint;

    half4 main(half4 c) {
        if (c.a <= 0.001) return c;
        half3 unpremul = c.rgb / c.a;
        half luma = dot(unpremul, half3(0.2126, 0.7152, 0.0722));
        half3 vibrant = clamp(mix(half3(luma), unpremul, half(saturationBoost)), 0.0, 1.0);
        half3 frosted = clamp(vibrant + half3(frostLift), 0.0, 1.0);
        half3 outRgb = mix(frosted, frostTint.rgb, half(tintMix));
        return half4(outRgb * c.a, c.a);
    }
"""

private const val FROSTED_SURFACE_XFERMODE_AGSL = """
    uniform float dispersion;

    half4 main(half4 src, half4 dst) {
        half3 softScreen = clamp(src.rgb + dst.rgb * (1.0 - src.a * (1.0 - half(dispersion))), 0.0, 1.0);
        half outA = clamp(src.a + dst.a * (1.0 - src.a), 0.0, 1.0);
        return half4(softScreen, outA);
    }
"""

private const val LIQUID_GLASS_BACKDROP_REFRACTION_AGSL = """
    uniform shader content;
    uniform float2 resolution;
    uniform float density;
    uniform float sensitivity;
    uniform float isDark;
    uniform float performanceMode;
    uniform int refractionStyle;
    uniform float patternScale;
    uniform float dispersion;
    uniform float styleParam;
    uniform float diamondCrossed;
    uniform float4 uRects[18];
    uniform float2 uRadii[18];
    uniform int regionCount;

    float smootherstep(float e0, float e1, float x) {
        float t = clamp((x - e0) / max(e1 - e0, 0.0001), 0.0, 1.0);
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    float sminC2(float a, float b, float k) {
        float safeK = max(k, 0.0001);
        float h = max(safeK - abs(a - b), 0.0) / safeK;
        return min(a, b) - (h * h * h) * (safeK * 0.16666667);
    }

    float2 wrapMod2(float2 p, float2 period) {
        return p - period * floor(p / max(period, float2(0.0001)));
    }

    float2 hash22(float2 p) {
        float3 p3 = float3(p.x, p.y, p.x);
        p3 = p3 * float3(0.1031, 0.1030, 0.0973);
        p3 = p3 - floor(p3);
        p3 += dot(p3, p3.yzx + 33.33);
        float2 r = (p3.xx + p3.yz) * p3.zy;
        return r - floor(r);
    }

    float resolvedRadius(float2 p, float2 halfSize, float topR, float botR, float dScale) {
        float maxR = min(halfSize.x, halfSize.y);
        float tR = clamp(topR, 0.0, maxR);
        float bR = clamp(botR, 0.0, maxR);
        float yBlend = clamp(halfSize.y * 0.35, 4.0 * dScale, 28.0 * dScale);
        return mix(tR, bR, smootherstep(-yBlend, yBlend, p.y));
    }

    float sdRoundRect(float2 p, float2 halfSize, float topR, float botR, float dScale) {
        float cr = resolvedRadius(p, halfSize, topR, botR, dScale);
        float2 d = abs(p) - (halfSize - float2(cr));
        return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - cr;
    }

    float smoothLensDist(
        float2 local,
        float2 halfSize,
        float topR,
        float botR,
        float bevelW,
        float dScale
    ) {
        float cr = resolvedRadius(local, halfSize, topR, botR, dScale);
        float dGeom = -sdRoundRect(local, halfSize, topR, botR, dScale);

        float cx = min(halfSize.x * 0.12, 16.0 * dScale);
        float cy = min(halfSize.y * 0.12, 16.0 * dScale);
        float sx = sqrt(halfSize.x * halfSize.x + cx * cx) - sqrt(local.x * local.x + cx * cx);
        float sy = sqrt(halfSize.y * halfSize.y + cy * cy) - sqrt(local.y * local.y + cy * cy);

        float maxHalf = min(halfSize.x, halfSize.y);
        float kSmooth = clamp(max(cr * 1.15, bevelW * 0.78), 4.0, maxHalf * 0.92);
        float wallDist = sminC2(sx, sy, kSmooth);

        float blendStart = min(cr * 0.30, bevelW * 0.22);
        float blendEnd = max(blendStart + 4.0 * dScale, bevelW * 0.72);
        float tBlend = smootherstep(blendStart, blendEnd, dGeom);
        return mix(dGeom, wallDist, tBlend);
    }

    float2 smoothLensNormal(
        float2 local,
        float2 halfSize,
        float topR,
        float botR,
        float bevelW,
        float dScale
    ) {
        float eps = clamp(1.75 * dScale, 1.5, 8.0);
        float gx = smoothLensDist(local + float2(eps, 0.0), halfSize, topR, botR, bevelW, dScale)
                 - smoothLensDist(local - float2(eps, 0.0), halfSize, topR, botR, bevelW, dScale);
        float gy = smoothLensDist(local + float2(0.0, eps), halfSize, topR, botR, bevelW, dScale)
                 - smoothLensDist(local - float2(0.0, eps), halfSize, topR, botR, bevelW, dScale);
        float2 grad = float2(-gx, -gy);
        float softFloor = 0.35 * eps;
        return grad * inversesqrt(dot(grad, grad) + softFloor * softFloor);
    }

    // Returns float4(dispX, dispY, edgeHighlight, chromaFactor) in pattern-local space
    float4 evalPatternRefraction(
        int style,
        float2 local,
        float cellPx,
        float strengthPx,
        float pParam,
        float pDiamondCross
    ) {
        float safeCell = max(cellPx, 6.0);
        float param = clamp(pParam, 0.0, 1.0);
        float2 disp = float2(0.0);
        float edgeGlow = 0.0;
        float chromaFactor = 0.55;

        if (style == 1) {
            // GRID: Orthogonal convex pillow cells with tunable Tile Curvature (param)
            float2 u = wrapMod2(local, float2(safeCell)) / safeCell - 0.5;
            float edgeDist = 0.5 - max(abs(u.x), abs(u.y));
            float bevel = smootherstep(0.0, 0.16, edgeDist);
            float r2 = clamp(dot(u, u) * 4.0, 0.0, 1.0);
            float flatBevelMag = (1.0 - smootherstep(0.06, 0.24, edgeDist)) * 1.35 + 0.35;
            float domeCurveMag = 0.75 + 1.35 * (1.0 - r2 * 0.45);
            float2 curvedU = u * (1.0 + 0.55 * r2 * param);
            disp = -curvedU * (mix(flatBevelMag, domeCurveMag, param) * bevel * (1.15 + 0.95 * param) * strengthPx);
            chromaFactor = clamp(length(u) * mix(1.35, 2.05, param), 0.25, 1.0);
        } else if (style == 2) {
            // RIBBED: Vertical fluted cylindrical glass ribs
            float ribPitch = max(safeCell * 0.55, 5.0);
            float phase = (local.x / ribPitch) * 6.2831853;
            float sWave = sin(phase);
            float cWave = cos(phase);
            disp = float2(-sWave * 1.15 * strengthPx, cWave * 0.08 * strengthPx);
            chromaFactor = clamp(abs(sWave) * 0.95 + 0.15, 0.15, 1.0);
        } else if (style == 3) {
            // HEX: True dual-lattice honeycomb hexagon cells
            float hexSize = max(safeCell * 0.95, 8.0);
            float2 r = float2(1.0, 1.7320508);
            float2 h = r * 0.5;
            float2 pHex = local / hexSize;
            float2 a = wrapMod2(pHex, r) - h;
            float2 b = wrapMod2(pHex - h, r) - h;
            float2 gv = dot(a, a) < dot(b, b) ? a : b;
            float hexDist = max(abs(gv.x), dot(abs(gv), normalize(r)));
            float edgeDist = 0.5 - hexDist;
            float bevel = smootherstep(0.0, 0.14, edgeDist);
            disp = -gv * (2.10 * bevel * strengthPx);
            chromaFactor = clamp(hexDist * 2.1, 0.25, 1.0);
        } else if (style == 4) {
            // HAMMER: Smooth hammered dimpled glass with tunable Hammer Depth (param)
            float hScale = max(safeCell * 0.68, 7.0);
            float2 p = local / hScale;
            float2 nCell = floor(p);
            float2 fCell = p - nCell;
            float minD = 8.0;
            float2 bestVec = float2(0.0);
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    float2 g = float2(float(dx), float(dy));
                    float2 rnd = hash22(nCell + g);
                    float2 o = 0.22 + 0.56 * rnd;
                    float2 rVec = g + o - fCell;
                    float d = dot(rVec, rVec);
                    if (d < minD) {
                        minD = d;
                        bestVec = rVec;
                    }
                }
            }
            float dist = sqrt(max(minD, 0.0001));
            float dimpleBase = clamp(1.0 - smootherstep(0.0, mix(0.62, 0.85, param), dist), 0.0, 1.0);
            float dimple = pow(dimpleBase, mix(1.45, 0.65, param));
            disp = -bestVec * (mix(0.45, 2.85, param) * dimple * strengthPx);
            chromaFactor = clamp(dist * mix(1.0, 1.85, param), 0.2, 1.0);
        } else if (style == 5) {
            // VORONOI: Crisp faceted crystal polygons with tunable Shard Tilt (param)
            float vScale = max(safeCell * 0.85, 8.0);
            float2 p = local / vScale;
            float2 nCell = floor(p);
            float2 fCell = p - nCell;
            float d1 = 8.0;
            float d2 = 8.0;
            float2 bestVec = float2(0.0);
            float2 bestId = float2(0.0);
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    float2 g = float2(float(dx), float(dy));
                    float2 cellCoord = nCell + g;
                    float2 o = 0.14 + 0.72 * hash22(cellCoord);
                    float2 rVec = g + o - fCell;
                    float d = length(rVec);
                    if (d < d1) {
                        d2 = d1;
                        d1 = d;
                        bestVec = rVec;
                        bestId = cellCoord;
                    } else if (d < d2) {
                        d2 = d;
                    }
                }
            }
            float2 facetTilt = (hash22(bestId + 19.7) - 0.5) * mix(0.0, 2.85, param);
            float seam = d2 - d1;
            float interior = smootherstep(0.0, 0.07, seam);
            disp = (facetTilt - bestVec * mix(0.85, 0.30, param)) * (0.95 * interior * strengthPx);
            chromaFactor = clamp(length(facetTilt) * 0.75 + 0.25, 0.25, 1.0);
        } else if (style == 6) {
            // CHEVRON: Zigzag V-shaped herringbone ribs with tunable Chevron Width (param)
            float zigW = max(safeCell * mix(0.55, 2.35, param), 6.0);
            float periodY = max(safeCell * 0.65, 6.0);
            float tx = local.x / zigW;
            float fZig = tx - floor(tx) - 0.5;
            float zigH = abs(fZig) * 2.0;
            float slopeSign = fZig * inversesqrt(fZig * fZig + 0.025);
            float phase = (local.y / periodY + zigH * 0.85) * 6.2831853;
            float wSin = sin(phase);
            float2 chevNormal = normalize(float2(slopeSign * mix(0.95, 0.52, param), 1.0));
            disp = -chevNormal * (wSin * 1.05 * strengthPx);
            chromaFactor = clamp(abs(wSin) * 0.9 + 0.2, 0.2, 1.0);
        } else if (style == 7) {
            // WAVY: Flowing multi-directional 2D liquid wave ripples
            float waveScale = max(safeCell * 0.42, 6.0);
            float2 wp = local / waveScale;
            float px = wp.x * 1.65 + cos(wp.y * 1.25) * 1.35;
            float py = wp.y * 1.55 - sin(wp.x * 1.15) * 1.35;
            float2 grad = float2(cos(px) * 1.15, -sin(py) * 1.15);
            disp = grad * (0.95 * strengthPx);
            chromaFactor = clamp(length(grad) * 0.85, 0.25, 1.0);
        } else if (style == 8) {
            // DIAMOND: 45-degree rotated prism lens grid with Facet Depth (param) and optional Crossed ("+") cut
            float2 rot = float2(local.x + local.y, local.y - local.x) * 0.70710678;
            float dCell = max(safeCell * 0.92, 7.0);
            float2 u = wrapMod2(rot, float2(dCell)) / dCell - 0.5;
            float edgeDist = 0.5 - max(abs(u.x), abs(u.y));
            float bevel = smootherstep(0.0, mix(0.06, 0.34, param), edgeDist);
            float2 pyrNorm = abs(u.x) >= abs(u.y)
                ? float2(u.x >= 0.0 ? 0.55 : -0.55, 0.0)
                : float2(0.0, u.y >= 0.0 ? 0.55 : -0.55);
            float2 bevelVec = mix(u, pyrNorm, 0.45 * param);
            float2 dLocal = -bevelVec * (mix(0.75, 2.75, param) * bevel * strengthPx);
            disp = float2(dLocal.x - dLocal.y, dLocal.x + dLocal.y) * 0.70710678;
            chromaFactor = clamp(length(u) * mix(1.35, 1.95, param), 0.25, 1.0);
            if (pDiamondCross > 0.5) {
                float crossX = u.x - u.y;
                float crossY = u.x + u.y;
                float crossSX = crossX * inversesqrt(crossX * crossX + 0.008);
                float crossSY = crossY * inversesqrt(crossY * crossY + 0.008);
                float armX = 1.0 - smootherstep(0.0, 0.24, abs(crossX));
                float armY = 1.0 - smootherstep(0.0, 0.24, abs(crossY));
                disp += float2(-crossSX * armX, -crossSY * armY) * (0.95 * bevel * (0.65 + 0.65 * param) * strengthPx);
                chromaFactor = max(chromaFactor, max(armX, armY) * 0.90);
            }
        } else if (style == 9) {
            // SCALES: Overlapping arched fish-scale tiles with tunable Scallop Curve (param)
            float sScale = max(safeCell * 0.82, 8.0);
            float2 p = local / sScale;
            float2 bestUv = float2(0.0);
            float bestRow = -1000000.0;
            float minEdge = 1.0;
            float baseRow = floor(p.y);
            float ySquash = mix(1.32, 0.76, param);
            float archRadius = mix(0.52, 0.72, param);
            for (int ry = 0; ry <= 1; ry++) {
                float rowIdx = baseRow + float(ry);
                float stagger = abs(rowIdx - 2.0 * floor(rowIdx * 0.5)) > 0.25 ? 0.5 : 0.0;
                float colBase = floor(p.x - stagger);
                for (int cx = 0; cx <= 1; cx++) {
                    float2 center = float2(colBase + float(cx) + stagger, rowIdx);
                    float2 diff = p - center;
                    float2 scaledDiff = float2(diff.x, diff.y * ySquash);
                    float r = length(scaledDiff);
                    if (r < archRadius && rowIdx > bestRow) {
                        bestRow = rowIdx;
                        bestUv = scaledDiff;
                        minEdge = archRadius - r;
                    }
                }
            }
            float archSmooth = smootherstep(0.0, mix(0.07, 0.22, param), minEdge);
            disp = -bestUv * (mix(1.05, 2.45, param) * archSmooth * strengthPx);
            chromaFactor = clamp(length(bestUv) * mix(1.4, 2.0, param), 0.25, 1.0);
        } else if (style == 10) {
            // LOUVRE: Parallel angled diagonal slats with tunable Prism Ramp (param)
            float slatW = max(safeCell * 0.62, 6.0);
            float2 dir = float2(0.8660254, 0.5);
            float u = dot(local, dir) / slatW;
            float f = u - floor(u);
            float fCurved = mix(f, f * f, param * 0.75);
            float ramp = (fCurved - 0.5) * mix(1.2, 3.0, param);
            float edgeSmooth = smootherstep(0.0, 0.10, f) * (1.0 - smootherstep(0.90, 1.0, f));
            disp = -dir * (ramp * edgeSmooth * strengthPx);
            chromaFactor = clamp(abs(ramp) * (0.45 + 0.35 * param) + 0.2, 0.2, 1.0);
        } else if (style == 11) {
            // PLASMA: Multi-frequency 3-axis waves with tunable Molten Warp (param)
            float plScale = max(safeCell * 0.48, 6.0);
            float2 pp = local / plScale;
            float warpAmt = mix(0.25, 1.65, param);
            float w1 = sin(pp.x * 1.45 + sin(pp.y * 0.95) * warpAmt);
            float w2 = sin((pp.x * 0.5 + pp.y * 0.866) * 1.65 + cos(pp.x * 0.8 + w1 * 0.45 * param) * warpAmt);
            float w3 = cos((pp.x * 0.5 - pp.y * 0.866) * 1.55 + w1 * (0.35 + 0.85 * param));
            float2 grad = float2(w1 - w3 * 0.65, w2 + w3 * 0.75);
            disp = grad * (mix(0.45, 1.25, param) * strengthPx);
            chromaFactor = clamp(length(grad) * mix(0.45, 0.95, param), 0.25, 1.0);
        } else if (style == 12) {
            // OCTAGON: Chamfered 8-sided octagonal lens grid
            float oCell = max(safeCell * 0.95, 8.0);
            float2 u = wrapMod2(local, float2(oCell)) / oCell - 0.5;
            float2 au = abs(u);
            float octMetric = max(max(au.x, au.y), (au.x + au.y) * 0.70710678);
            float edgeDist = 0.48 - octMetric;
            float bevel = smootherstep(0.0, 0.14, edgeDist);
            float2 facetGrad = (au.x + au.y) * 0.70710678 > max(au.x, au.y)
                ? float2(u.x >= 0.0 ? 0.7071 : -0.7071, u.y >= 0.0 ? 0.7071 : -0.7071)
                : (au.x >= au.y ? float2(u.x * 2.0, 0.0) : float2(0.0, u.y * 2.0));
            disp = -mix(u * 1.6, facetGrad, 0.55) * (1.25 * bevel * strengthPx);
            chromaFactor = clamp(octMetric * 2.1, 0.25, 1.0);
        } else if (style == 13) {
            // SCAN: Horizontal scanline micro-ribs with tunable Raster Shift (param)
            float scanPitch = max(safeCell * 0.42, 4.5);
            float phase = (local.y / scanPitch) * 6.2831853;
            float sWave = sin(phase);
            float cWave = cos(phase);
            float rowStep = floor(local.y / scanPitch);
            float rowAlt = abs(rowStep - 2.0 * floor(rowStep * 0.5)) > 0.25 ? 1.0 : -1.0;
            float horizShift = (rowAlt * (0.35 + 0.65 * abs(sWave)) + cWave * 0.45) * (1.85 * param * strengthPx);
            disp = float2(horizShift + cWave * 0.08 * strengthPx, -sWave * 1.15 * strengthPx);
            chromaFactor = clamp(abs(sWave) * 0.85 + param * 0.35 + 0.15, 0.15, 1.0);
        }

        return float4(disp.x, disp.y, edgeGlow, chromaFactor);
    }

    half4 main(float2 fragCoord) {
        half4 base = content.eval(fragCoord);
        float s = clamp(sensitivity, 0.1, 1.0);
        float pScale = clamp(patternScale, 0.1, 1.0);
        float pDisp = clamp(dispersion, 0.0, 1.0);
        float dScale = max(density, 1.0);
        float bevelW = (16.0 + 26.0 * s) * mix(0.75, 1.35, pScale) * dScale;
        float cellPx = mix(12.0, 56.0, pScale) * dScale;
        float patternStrengthPx = (5.0 + 24.0 * s) * dScale;

        for (int i = 0; i < 18; i++) {
            if (i >= regionCount) break;
            float4 r = uRects[i];
            if (fragCoord.x < r.x || fragCoord.x > r.z || fragCoord.y < r.y || fragCoord.y > r.w) {
                continue;
            }
            float2 halfSize = float2((r.z - r.x) * 0.5, (r.w - r.y) * 0.5);
            if (halfSize.x <= 1.0 || halfSize.y <= 1.0) continue;

            float2 center = float2((r.x + r.z) * 0.5, (r.y + r.w) * 0.5);
            float2 local = fragCoord - center;
            float topR = uRadii[i].x;
            float botR = uRadii[i].y;
            float sdf = sdRoundRect(local, halfSize, topR, botR, dScale);
            if (sdf > 0.0) continue;

            // Keep internal lens refraction slightly rounded even when actual card corners are squared (0dp)
            float maxR = min(halfSize.x, halfSize.y);
            float minInnerRound = min(maxR * 0.58, max(18.0 * dScale, bevelW * 0.78));
            float lensTopR = clamp(max(topR, minInnerRound), 0.0, maxR);
            float lensBotR = clamp(max(botR, minInnerRound), 0.0, maxR);

            // C2-continuous inward lens distance & singularity-free gradient normal
            float rawLensDist = smoothLensDist(local, halfSize, lensTopR, lensBotR, bevelW, dScale);
            float distIn = max(rawLensDist, 0.0);
            float cornerEase = 1.0 - 0.35 * smootherstep(0.0, max(minInnerRound * 0.45, 2.0), max(-rawLensDist, 0.0));
            float rimDistIn = -sdf;
            float2 n = smoothLensNormal(local, halfSize, lensTopR, lensBotR, bevelW, dScale);

            // Quintic C2 bevel chamfer + soft peripheral dome warp
            float edgeT = 1.0 - smootherstep(0.0, bevelW, distIn);
            float bevelMag = (edgeT * edgeT * mix(0.55, 1.0, edgeT)) * (14.0 + 34.0 * s) * dScale;
            float2 normLocal = local / max(halfSize, float2(1.0));
            float radialWeight = smootherstep(0.18, 1.0, length(normLocal)) * (1.0 - smootherstep(bevelW * 0.5, bevelW * 3.2, distIn));
            float domeMag = radialWeight * (3.5 + 8.5 * s) * dScale;

            float2 disp = -n * (bevelMag + domeMag);
            float chromaWeight = edgeT;

            if (refractionStyle > 0) {
                float4 pat = evalPatternRefraction(refractionStyle, local, cellPx, patternStrengthPx, styleParam, diamondCrossed);
                float edgeBlend = 1.0 - 0.35 * edgeT;
                disp = disp * 0.55 + pat.xy * edgeBlend;
                chromaWeight = max(edgeT * 0.75, pat.w);
            }

            // Chromatic dispersion across RGB wavelengths (3-tap in Performance Mode, 5-tap in full quality)
            float chroma = (0.04 + 0.24 * pDisp) * (0.55 + 0.45 * s) * chromaWeight;
            float2 minUv = float2(0.5);
            float2 maxUv = resolution - float2(0.5);
            float2 uvR  = clamp(fragCoord + disp * (1.0 - chroma), minUv, maxUv);
            float2 uvG  = clamp(fragCoord + disp, minUv, maxUv);
            float2 uvB  = clamp(fragCoord + disp * (1.0 + chroma), minUv, maxUv);

            half4 sR = content.eval(uvR);
            half4 sG = content.eval(uvG);
            half4 sB = content.eval(uvB);
            half4 refracted;
            if (performanceMode > 0.5) {
                refracted = half4(sR.r, sG.g, sB.b, max(sG.a, max(sR.a, sB.a)));
            } else {
                float2 uvRG = clamp(fragCoord + disp * (1.0 - 0.45 * chroma), minUv, maxUv);
                float2 uvGB = clamp(fragCoord + disp * (1.0 + 0.45 * chroma), minUv, maxUv);
                half4 sRG = content.eval(uvRG);
                half4 sGB = content.eval(uvGB);
                half cR = mix(sR.r, sRG.r, half(0.32));
                half cG = mix(sG.g, (sRG.g + sGB.g) * half(0.5), half(0.35));
                half cB = mix(sB.b, sGB.b, half(0.32));
                half cA = max(sG.a, max(sR.a, sB.a));
                refracted = half4(cR, cG, cB, cA);
            }

            // Quintic Fresnel specular rim + organic inner bevel caustic (no interior pattern lines)
            float rimMask = smootherstep(0.0, 1.6 * dScale, rimDistIn) * (1.0 - smootherstep(1.2 * dScale, 6.8 * dScale, rimDistIn));
            float fresnel = edgeT * edgeT * edgeT * cornerEase;
            float keyLight = clamp(dot(n, normalize(float2(-0.55, -0.83))), 0.0, 1.0);
            float backLight = clamp(dot(n, normalize(float2(0.55, 0.83))), 0.0, 1.0);
            float rimGlow = (0.28 + 0.72 * keyLight + 0.35 * backLight) * rimMask * (0.14 + 0.28 * s);
            float innerCaustic = fresnel * (0.035 + 0.075 * s) * (0.55 + 0.45 * keyLight) * (isDark > 0.5 ? 0.65 : 0.90);

            half3 finalRgb = clamp(refracted.rgb + half3(rimGlow + innerCaustic), 0.0, 1.0);
            return half4(finalRgb, refracted.a);
        }

        return base;
    }
"""

private const val FROSTED_GLASS_CARD_SURFACE_AGSL = """
    uniform float2 cardSize;
    uniform float topRadius;
    uniform float botRadius;
    uniform float density;
    uniform float sensitivity;
    uniform float isDark;
    uniform int refractionStyle;
    uniform float patternScale;
    uniform float dispersion;

    float smootherstep(float e0, float e1, float x) {
        float t = clamp((x - e0) / max(e1 - e0, 0.0001), 0.0, 1.0);
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    float sminC2(float a, float b, float k) {
        float safeK = max(k, 0.0001);
        float h = max(safeK - abs(a - b), 0.0) / safeK;
        return min(a, b) - (h * h * h) * (safeK * 0.16666667);
    }

    float2 wrapMod2(float2 p, float2 period) {
        return p - period * floor(p / max(period, float2(0.0001)));
    }

    float2 hash22(float2 p) {
        float3 p3 = float3(p.x, p.y, p.x);
        p3 = p3 * float3(0.1031, 0.1030, 0.0973);
        p3 = p3 - floor(p3);
        p3 += dot(p3, p3.yzx + 33.33);
        float2 r = (p3.xx + p3.yz) * p3.zy;
        return r - floor(r);
    }

    float resolvedRadius(float2 p, float2 halfSize, float topR, float botR, float dScale) {
        float maxR = min(halfSize.x, halfSize.y);
        float tR = clamp(topR, 0.0, maxR);
        float bR = clamp(botR, 0.0, maxR);
        float yBlend = clamp(halfSize.y * 0.35, 4.0 * dScale, 28.0 * dScale);
        return mix(tR, bR, smootherstep(-yBlend, yBlend, p.y));
    }

    float sdRoundRect(float2 p, float2 halfSize, float topR, float botR, float dScale) {
        float cr = resolvedRadius(p, halfSize, topR, botR, dScale);
        float2 d = abs(p) - (halfSize - float2(cr));
        return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - cr;
    }

    float smoothLensDist(
        float2 local,
        float2 halfSize,
        float topR,
        float botR,
        float bevelW,
        float dScale
    ) {
        float cr = resolvedRadius(local, halfSize, topR, botR, dScale);
        float dGeom = -sdRoundRect(local, halfSize, topR, botR, dScale);

        float cx = min(halfSize.x * 0.12, 16.0 * dScale);
        float cy = min(halfSize.y * 0.12, 16.0 * dScale);
        float sx = sqrt(halfSize.x * halfSize.x + cx * cx) - sqrt(local.x * local.x + cx * cx);
        float sy = sqrt(halfSize.y * halfSize.y + cy * cy) - sqrt(local.y * local.y + cy * cy);

        float maxHalf = min(halfSize.x, halfSize.y);
        float kSmooth = clamp(max(cr * 1.15, bevelW * 0.78), 4.0, maxHalf * 0.92);
        float wallDist = sminC2(sx, sy, kSmooth);

        float blendStart = min(cr * 0.30, bevelW * 0.22);
        float blendEnd = max(blendStart + 4.0 * dScale, bevelW * 0.72);
        float tBlend = smootherstep(blendStart, blendEnd, dGeom);
        return mix(dGeom, wallDist, tBlend);
    }

    float2 smoothLensNormal(
        float2 local,
        float2 halfSize,
        float topR,
        float botR,
        float bevelW,
        float dScale
    ) {
        float eps = clamp(1.75 * dScale, 1.5, 8.0);
        float gx = smoothLensDist(local + float2(eps, 0.0), halfSize, topR, botR, bevelW, dScale)
                 - smoothLensDist(local - float2(eps, 0.0), halfSize, topR, botR, bevelW, dScale);
        float gy = smoothLensDist(local + float2(0.0, eps), halfSize, topR, botR, bevelW, dScale)
                 - smoothLensDist(local - float2(0.0, eps), halfSize, topR, botR, bevelW, dScale);
        float2 grad = float2(-gx, -gy);
        float softFloor = 0.35 * eps;
        return grad * inversesqrt(dot(grad, grad) + softFloor * softFloor);
    }

    half4 main(float2 fragCoord) {
        float2 halfSize = cardSize * 0.5;
        if (halfSize.x <= 1.0 || halfSize.y <= 1.0) return half4(0.0);

        float dScale = max(density, 1.0);
        float2 local = fragCoord - halfSize;
        float sdf = sdRoundRect(local, halfSize, topRadius, botRadius, dScale);
        if (sdf > 0.0) return half4(0.0);

        float s = clamp(sensitivity, 0.1, 1.0);
        float pScale = clamp(patternScale, 0.1, 1.0);
        float pDisp = clamp(dispersion, 0.0, 1.0);
        float bevelW = (12.0 + 16.0 * s) * mix(0.75, 1.35, pScale) * dScale;

        // Keep internal bevel curvature slightly rounded even when card corners are squared (0dp)
        float maxR = min(halfSize.x, halfSize.y);
        float minInnerRound = min(maxR * 0.58, max(16.0 * dScale, bevelW * 0.78));
        float lensTopR = clamp(max(topRadius, minInnerRound), 0.0, maxR);
        float lensBotR = clamp(max(botRadius, minInnerRound), 0.0, maxR);

        float rawLensDist = smoothLensDist(local, halfSize, lensTopR, lensBotR, bevelW, dScale);
        float distIn = max(rawLensDist, 0.0);
        float cornerEase = 1.0 - 0.35 * smootherstep(0.0, max(minInnerRound * 0.45, 2.0), max(-rawLensDist, 0.0));
        float rimDistIn = -sdf;
        float2 n = smoothLensNormal(local, halfSize, lensTopR, lensBotR, bevelW, dScale);

        float edgeT = 1.0 - smootherstep(0.0, bevelW, distIn);
        float rimBand = smootherstep(0.0, 1.5 * dScale, rimDistIn) * (1.0 - smootherstep(1.1 * dScale, 5.4 * dScale, rimDistIn));

        float keyLight = clamp(dot(n, normalize(float2(-0.55, -0.83))), 0.0, 1.0);
        float counterLight = clamp(dot(n, normalize(float2(0.55, 0.83))), 0.0, 1.0);

        // Subtle chromatic dispersion across the bevel normal
        float spectrumPhase = n.x * 0.65 - n.y * 0.35;
        float dispAmp = 0.06 + 0.24 * pDisp;
        half3 prismTint = half3(
            clamp(1.0 + dispAmp * spectrumPhase, 0.72, 1.0),
            1.0,
            clamp(1.0 - dispAmp * spectrumPhase, 0.72, 1.0)
        );

        float highlightStrength = (0.14 + 0.26 * s) * (0.28 + 0.72 * keyLight) * rimBand;
        float causticStrength = (edgeT * edgeT * edgeT) * cornerEase * (0.04 + 0.07 * s) * (0.4 + 0.6 * keyLight);
        float shadowStrength = (isDark > 0.5 ? 0.26 : 0.12) * s * counterLight * rimBand;

        half3 lightColor = prismTint * half(highlightStrength + causticStrength);
        float outAlpha = clamp(highlightStrength + causticStrength + shadowStrength, 0.0, 0.65);
        return half4(lightColor, half(outAlpha));
    }
"""

fun isAndroid16FrostedGlassSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA

fun isRuntimeShaderRefractionSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
fun createAndroid16FrostedBackdropColorFilter(
    isDark: Boolean,
    blurRadiusDp: Float,
    transparency: Float = 0.40f
): RuntimeColorFilter? {
    return try {
        val radiusNormalized = (blurRadiusDp.coerceIn(0f, 60f) / 60f)
        val glassOpacity = (1f - transparency.coerceIn(0f, 0.95f)).coerceIn(0.05f, 1f)
        RuntimeColorFilter(FROSTED_BACKDROP_AGSL).apply {
            setFloatUniform("saturationBoost", 1.18f + 0.10f * radiusNormalized)
            setFloatUniform("frostLift", if (isDark) 0.025f * glassOpacity else 0.055f * glassOpacity)
            setFloatUniform("tintMix", if (isDark) 0.05f * glassOpacity else 0.09f * glassOpacity)
            setColorUniform(
                "frostTint",
                if (isDark) AndroidColor.rgb(225, 232, 245) else AndroidColor.rgb(248, 250, 255)
            )
        }
    } catch (_: Throwable) {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
fun createAndroid16FrostedSurfaceColorFilter(
    isDark: Boolean,
    transparency: Float = 0.40f
): RuntimeColorFilter? {
    return try {
        val glassDensity = (1f - transparency.coerceIn(0f, 0.95f)).coerceIn(0.05f, 1f)
        RuntimeColorFilter(FROSTED_BACKDROP_AGSL).apply {
            setFloatUniform("saturationBoost", 1.08f)
            setFloatUniform("frostLift", if (isDark) 0.018f * glassDensity else 0.032f * glassDensity)
            setFloatUniform("tintMix", 0.04f * glassDensity)
            setColorUniform(
                "frostTint",
                if (isDark) AndroidColor.rgb(235, 242, 255) else AndroidColor.WHITE
            )
        }
    } catch (_: Throwable) {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
fun createAndroid16FrostedSurfaceXfermode(
    isDark: Boolean,
    transparency: Float = 0.40f
): RuntimeXfermode? {
    return try {
        val glassDensity = (1f - transparency.coerceIn(0f, 0.95f)).coerceIn(0.05f, 1f)
        RuntimeXfermode(FROSTED_SURFACE_XFERMODE_AGSL).apply {
            setFloatUniform("dispersion", (if (isDark) 0.28f else 0.36f) * glassDensity)
        }
    } catch (_: Throwable) {
        null
    }
}

/**
 * Builds an AGSL [RuntimeShader] that draws curved glass bevel & chromatic Fresnel rim
 * highlights strictly using the rounded-rectangle SDF of an individual card or container.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun createFrostedGlassCardBevelShader(
    widthPx: Float,
    heightPx: Float,
    topRadiusPx: Float,
    bottomRadiusPx: Float = topRadiusPx,
    density: Float = 2.625f,
    sensitivity: Float = 0.50f,
    isDark: Boolean = true,
    refractionStyle: RefractionStyle = RefractionStyle.LIQUID,
    patternScale: Float = refractionStyle.defaultScale,
    dispersion: Float = refractionStyle.defaultDispersion
): RuntimeShader? {
    if (widthPx <= 1f || heightPx <= 1f) return null
    return try {
        RuntimeShader(FROSTED_GLASS_CARD_SURFACE_AGSL).apply {
            setFloatUniform("cardSize", widthPx, heightPx)
            setFloatUniform("topRadius", topRadiusPx.coerceAtLeast(0f))
            setFloatUniform("botRadius", bottomRadiusPx.coerceAtLeast(0f))
            setFloatUniform("density", density.coerceAtLeast(1f))
            setFloatUniform("sensitivity", sensitivity.coerceIn(0.1f, 1.0f))
            setFloatUniform("isDark", if (isDark) 1f else 0f)
            setIntUniform("refractionStyle", refractionStyle.shaderIndex)
            setFloatUniform("patternScale", patternScale.coerceIn(0.1f, 1.0f))
            setFloatUniform("dispersion", dispersion.coerceIn(0.0f, 1.0f))
        }
    } catch (_: Throwable) {
        null
    }
}

/**
 * Builds an API 33+ AGSL [AndroidRenderEffect] using [AndroidRenderEffect.createRuntimeShaderEffect]
 * that genuinely samples and refracts underlying `content` pixels across each active glass card's
 * rounded edges (`qs_card`, `qqs_carrier_footer`, notification cards) with chromatic RGB dispersion.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun createLiquidGlassRefractionRuntimeEffect(
    regions: List<QsCardBlurBounds>,
    viewportWidthPx: Float,
    viewportHeightPx: Float,
    density: Float,
    sensitivity: Float,
    isDark: Boolean,
    performanceMode: Boolean = false,
    refractionStyle: RefractionStyle = RefractionStyle.LIQUID,
    patternScale: Float = refractionStyle.defaultScale,
    dispersion: Float = refractionStyle.defaultDispersion,
    styleParam: Float = refractionStyle.defaultCustomParam,
    diamondCrossed: Boolean = false
): AndroidRenderEffect? {
    val validRegions = regions.filter { it.isValid }.take(MAX_REFRACTION_REGIONS)
    if (validRegions.isEmpty() || viewportWidthPx <= 1f || viewportHeightPx <= 1f) return null

    return try {
        val rectsArray = FloatArray(MAX_REFRACTION_REGIONS * 4)
        val radiiArray = FloatArray(MAX_REFRACTION_REGIONS * 2)
        validRegions.forEachIndexed { idx, bounds ->
            val rBase = idx * 4
            rectsArray[rBase] = bounds.left
            rectsArray[rBase + 1] = bounds.top
            rectsArray[rBase + 2] = bounds.right
            rectsArray[rBase + 3] = bounds.bottom

            val crBase = idx * 2
            radiiArray[crBase] = bounds.topCornerRadiusPx.coerceAtLeast(0f)
            radiiArray[crBase + 1] = bounds.bottomCornerRadiusPx.coerceAtLeast(0f)
        }
        val shader = RuntimeShader(LIQUID_GLASS_BACKDROP_REFRACTION_AGSL).apply {
            setFloatUniform("resolution", viewportWidthPx, viewportHeightPx)
            setFloatUniform("density", density.coerceAtLeast(1f))
            setFloatUniform("sensitivity", sensitivity.coerceIn(0.1f, 1.0f))
            setFloatUniform("isDark", if (isDark) 1f else 0f)
            setFloatUniform("performanceMode", if (performanceMode) 1f else 0f)
            setIntUniform("refractionStyle", refractionStyle.shaderIndex)
            setFloatUniform("patternScale", patternScale.coerceIn(0.1f, 1.0f))
            setFloatUniform("dispersion", dispersion.coerceIn(0.0f, 1.0f))
            setFloatUniform("styleParam", styleParam.coerceIn(0.0f, 1.0f))
            setFloatUniform("diamondCrossed", if (diamondCrossed) 1f else 0f)
            setFloatUniform("uRects", rectsArray)
            setFloatUniform("uRadii", radiiArray)
            setIntUniform("regionCount", validRegions.size)
        }
        AndroidRenderEffect.createRuntimeShaderEffect(shader, "content")
    } catch (_: Throwable) {
        null
    }
}

/**
 * Builds an API 31+ blur-only (plus API 36+ frosted color filter) [AndroidRenderEffect]
 * for downscaled GPU blur passes in Performance Mode. Returns null if blur is 0 or negligible.
 */
@RequiresApi(Build.VERSION_CODES.S)
fun createFrostedBlurOnlyPlatformRenderEffect(
    blurRadiusPx: Float,
    isDark: Boolean,
    blurRadiusDp: Float = DEFAULT_FROSTED_BLUR_RADIUS_DP,
    transparency: Float = 0.40f
): AndroidRenderEffect? {
    if (blurRadiusPx <= 0.5f || blurRadiusDp <= 0.25f) return null
    val baseBlur = try {
        AndroidRenderEffect.createBlurEffect(
            blurRadiusPx,
            blurRadiusPx,
            Shader.TileMode.CLAMP
        )
    } catch (_: Throwable) {
        return null
    }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        val frostedFilter = createAndroid16FrostedBackdropColorFilter(
            isDark = isDark,
            blurRadiusDp = blurRadiusDp,
            transparency = transparency
        )
        if (frostedFilter != null) {
            try {
                AndroidRenderEffect.createColorFilterEffect(frostedFilter, baseBlur)
            } catch (_: Throwable) {
                baseBlur
            }
        } else {
            baseBlur
        }
    } else {
        baseBlur
    }
}

/**
 * Builds the GPU RenderEffect pipeline for frosted glass backdrops:
 * - Chains [AndroidRenderEffect.createBlurEffect] with Android 16's AGSL [RuntimeColorFilter]
 *   on API 36+.
 * - When [enableGlassRefraction] is true on API 33+ and [refractionRegions] has active glass cards
 *   with valid viewport size, chains [AndroidRenderEffect.createRuntimeShaderEffect] so underlying
 *   content is optically refracted and chromatically dispersed along every card boundary.
 * - Skips blur passes completely when blur intensity is 0 / negligible; in [performanceMode],
 *   uses faster 3-tap refraction dispersion and skips negligible sub-threshold blur.
 */
fun createFrostedBackdropRenderEffect(
    blurRadiusPx: Float,
    isDark: Boolean,
    blurRadiusDp: Float = DEFAULT_FROSTED_BLUR_RADIUS_DP,
    transparency: Float = 0.40f,
    enableGlassRefraction: Boolean = false,
    glassRefractionSensitivity: Float = 0.50f,
    refractionRegions: List<QsCardBlurBounds> = emptyList(),
    viewportWidthPx: Float = 0f,
    viewportHeightPx: Float = 0f,
    density: Float = 2.625f,
    performanceMode: Boolean = false,
    refractionStyle: RefractionStyle = RefractionStyle.LIQUID,
    patternScale: Float = refractionStyle.defaultScale,
    dispersion: Float = refractionStyle.defaultDispersion,
    styleParam: Float = refractionStyle.defaultCustomParam,
    diamondCrossed: Boolean = false
): ComposeRenderEffect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null

    val wantsRefraction = enableGlassRefraction &&
        isRuntimeShaderRefractionSupported() &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        refractionRegions.isNotEmpty() &&
        viewportWidthPx > 1f &&
        viewportHeightPx > 1f

    val skipBlur = blurRadiusPx <= 0.5f ||
        blurRadiusDp <= 0.25f ||
        (performanceMode && blurRadiusDp < 0.75f)

    if (skipBlur) {
        if (!wantsRefraction) return null
        val refractionOnly = createLiquidGlassRefractionRuntimeEffect(
            regions = refractionRegions,
            viewportWidthPx = viewportWidthPx,
            viewportHeightPx = viewportHeightPx,
            density = density,
            sensitivity = glassRefractionSensitivity,
            isDark = isDark,
            performanceMode = performanceMode,
            refractionStyle = refractionStyle,
            patternScale = patternScale,
            dispersion = dispersion,
            styleParam = styleParam,
            diamondCrossed = diamondCrossed
        ) ?: return null
        return try {
            refractionOnly.asComposeRenderEffect()
        } catch (_: Throwable) {
            null
        }
    }

    var platformEffect = createFrostedBlurOnlyPlatformRenderEffect(
        blurRadiusPx = blurRadiusPx,
        isDark = isDark,
        blurRadiusDp = blurRadiusDp,
        transparency = transparency
    ) ?: return null

    if (wantsRefraction) {
        val refractionEffect = createLiquidGlassRefractionRuntimeEffect(
            regions = refractionRegions,
            viewportWidthPx = viewportWidthPx,
            viewportHeightPx = viewportHeightPx,
            density = density,
            sensitivity = glassRefractionSensitivity,
            isDark = isDark,
            performanceMode = performanceMode,
            refractionStyle = refractionStyle,
            patternScale = patternScale,
            dispersion = dispersion,
            styleParam = styleParam,
            diamondCrossed = diamondCrossed
        )
        if (refractionEffect != null) {
            platformEffect = try {
                AndroidRenderEffect.createChainEffect(refractionEffect, platformEffect)
            } catch (_: Throwable) {
                platformEffect
            }
        }
    }

    return try {
        platformEffect.asComposeRenderEffect()
    } catch (_: Throwable) {
        null
    }
}

fun frostedContainerColor(
    baseColor: Color,
    enableBlur: Boolean,
    isDark: Boolean,
    transparency: Float? = null
): Color {
    if (!enableBlur) return baseColor
    val frostedAlpha = if (transparency != null) {
        (1f - transparency.coerceIn(0f, 0.95f))
    } else {
        if (isDark) 0.56f else 0.68f
    }
    return baseColor.copy(alpha = frostedAlpha)
}

/**
 * Applies the translucent frosted glass container surface strictly behind foreground children.
 * On Android 16+ (API 36 / BAKLAVA), layers an AGSL [RuntimeColorFilter] + [RuntimeXfermode]
 * luminous frost sheen inside the clipped surface without blurring content or adding any outline.
 */
fun Modifier.frostedGlassSurface(
    baseColor: Color,
    enableBlur: Boolean,
    isDark: Boolean,
    transparency: Float? = null,
    cornerRadiusDp: Float = 24f,
    topCornerRadiusDp: Float = cornerRadiusDp,
    bottomCornerRadiusDp: Float = cornerRadiusDp,
    shape: Shape? = null,
    enableRefractionOverride: Boolean? = null
): Modifier = composed {
    val supportedBlur = enableBlur && isAndroid16FrostedGlassSupported()
    val effectiveColor = frostedContainerColor(
        baseColor = baseColor,
        enableBlur = supportedBlur,
        isDark = isDark,
        transparency = transparency
    )
    if (!supportedBlur) {
        return@composed this.background(effectiveColor)
    }

    val resolvedTransparency = transparency ?: if (isDark) 0.44f else 0.32f
    val bevelPaint = remember {
        Paint().apply {
            isAntiAlias = true
        }
    }

    val glassMode by PanelPreferences.glassBackdropMode.collectAsState()
    val performanceMode by PanelPreferences.glassPerformanceMode.collectAsState()
    val refractionStyle by PanelPreferences.refractionStyle.collectAsState()
    val enableRefraction = (enableRefractionOverride ?: (glassMode == GlassBackdropMode.LIQUID)) &&
        isRuntimeShaderRefractionSupported()
    val refractionSensitivity by PanelPreferences.glassRefractionSensitivity.collectAsState()
    val refractionScale by PanelPreferences.glassRefractionScale.collectAsState()
    val refractionDispersion by PanelPreferences.glassRefractionDispersion.collectAsState()
    val density = LocalDensity.current
    val topCornerPx = with(density) { topCornerRadiusDp.coerceAtLeast(0f).dp.toPx() }
    val bottomCornerPx = with(density) { bottomCornerRadiusDp.coerceAtLeast(0f).dp.toPx() }
    val densityPx = density.density

    var cachedWidthPx by remember { mutableStateOf(-1f) }
    var cachedHeightPx by remember { mutableStateOf(-1f) }
    var cachedTopR by remember { mutableStateOf(-1f) }
    var cachedBotR by remember { mutableStateOf(-1f) }
    var cachedSens by remember { mutableStateOf(-1f) }
    var cachedStyle by remember { mutableStateOf(refractionStyle) }
    var cachedScale by remember { mutableStateOf(-1f) }
    var cachedDispersion by remember { mutableStateOf(-1f) }
    var cachedIsDark by remember { mutableStateOf(isDark) }
    var cachedShader by remember { mutableStateOf<RuntimeShader?>(null) }
    var cachedBevelNode by remember { mutableStateOf<Any?>(null) }

    DisposableEffect(cachedBevelNode) {
        val currentNode = cachedBevelNode
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    (currentNode as? android.graphics.RenderNode)?.discardDisplayList()
                } catch (_: Throwable) {}
            }
            bevelPaint.shader = null
        }
    }

    this
        .background(effectiveColor)
        .drawBehind {
            if (size.width <= 0f || size.height <= 0f) return@drawBehind

            // Real AGSL rounded-rect SDF Fresnel dispersion & bevel shader (Liquid Glass mode on API 36+)
            if (enableRefraction && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val resolvedTopRadiusPx = if (shape is CornerBasedShape) {
                    maxOf(shape.topStart.toPx(size, this), shape.topEnd.toPx(size, this))
                } else {
                    topCornerPx
                }
                val resolvedBottomRadiusPx = if (shape is CornerBasedShape) {
                    maxOf(shape.bottomStart.toPx(size, this), shape.bottomEnd.toPx(size, this))
                } else {
                    bottomCornerPx
                }
                val paramsChanged = cachedShader == null ||
                    kotlin.math.abs(cachedWidthPx - size.width) > 0.5f ||
                    kotlin.math.abs(cachedHeightPx - size.height) > 0.5f ||
                    kotlin.math.abs(cachedTopR - resolvedTopRadiusPx) > 0.5f ||
                    kotlin.math.abs(cachedBotR - resolvedBottomRadiusPx) > 0.5f ||
                    kotlin.math.abs(cachedSens - refractionSensitivity) > 0.005f ||
                    cachedStyle != refractionStyle ||
                    kotlin.math.abs(cachedScale - refractionScale) > 0.005f ||
                    kotlin.math.abs(cachedDispersion - refractionDispersion) > 0.005f ||
                    cachedIsDark != isDark

                if (paramsChanged) {
                    cachedShader = createFrostedGlassCardBevelShader(
                        widthPx = size.width,
                        heightPx = size.height,
                        topRadiusPx = resolvedTopRadiusPx,
                        bottomRadiusPx = resolvedBottomRadiusPx,
                        density = densityPx,
                        sensitivity = refractionSensitivity,
                        isDark = isDark,
                        refractionStyle = refractionStyle,
                        patternScale = refractionScale,
                        dispersion = refractionDispersion
                    )
                    cachedWidthPx = size.width
                    cachedHeightPx = size.height
                    cachedTopR = resolvedTopRadiusPx
                    cachedBotR = resolvedBottomRadiusPx
                    cachedSens = refractionSensitivity
                    cachedStyle = refractionStyle
                    cachedScale = refractionScale
                    cachedDispersion = refractionDispersion
                    cachedIsDark = isDark
                    if (performanceMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val shader = cachedShader
                        if (shader != null) {
                            val node = (cachedBevelNode as? android.graphics.RenderNode)
                                ?: android.graphics.RenderNode("GlassBevelCache").also { cachedBevelNode = it }
                            val w = size.width.roundToInt().coerceAtLeast(1)
                            val h = size.height.roundToInt().coerceAtLeast(1)
                            node.setPosition(0, 0, w, h)
                            bevelPaint.shader = shader
                            val rec = node.beginRecording(w, h)
                            rec.drawRect(0f, 0f, size.width, size.height, bevelPaint)
                            node.endRecording()
                        } else {
                            cachedBevelNode = null
                        }
                    } else {
                        cachedBevelNode = null
                    }
                } else if (performanceMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cachedBevelNode == null) {
                    val shader = cachedShader
                    if (shader != null) {
                        val node = android.graphics.RenderNode("GlassBevelCache").also { cachedBevelNode = it }
                        val w = size.width.roundToInt().coerceAtLeast(1)
                        val h = size.height.roundToInt().coerceAtLeast(1)
                        node.setPosition(0, 0, w, h)
                        bevelPaint.shader = shader
                        val rec = node.beginRecording(w, h)
                        rec.drawRect(0f, 0f, size.width, size.height, bevelPaint)
                        node.endRecording()
                    }
                }

                val bevelShader = cachedShader
                if (bevelShader != null) {
                    drawIntoCanvas { canvas ->
                        val nc = canvas.nativeCanvas
                        val node = if (performanceMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && nc.isHardwareAccelerated) {
                            cachedBevelNode as? android.graphics.RenderNode
                        } else null
                        if (node != null && node.hasDisplayList()) {
                            nc.drawRenderNode(node)
                        } else {
                            bevelPaint.shader = bevelShader
                            nc.drawRect(0f, 0f, size.width, size.height, bevelPaint)
                        }
                    }
                }
            }
        }
}

private data class QsCardBlurRegionsClipShape(
    private val regions: List<QsCardBlurBounds>
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): ComposeOutline {
        val path = Path().apply {
            regions.forEach { bounds ->
                if (!bounds.isValid) return@forEach
                val clampedLeft = bounds.left.coerceIn(0f, size.width)
                val clampedTop = bounds.top.coerceIn(0f, size.height)
                val clampedRight = bounds.right.coerceIn(0f, size.width)
                val clampedBottom = bounds.bottom.coerceIn(0f, size.height)
                if (clampedRight - clampedLeft <= 1f || clampedBottom - clampedTop <= 1f) return@forEach
                val topCorner = CornerRadius(bounds.topCornerRadiusPx.coerceAtLeast(0f))
                val bottomCorner = CornerRadius(bounds.bottomCornerRadiusPx.coerceAtLeast(0f))
                addRoundRect(
                    RoundRect(
                        left = clampedLeft,
                        top = clampedTop,
                        right = clampedRight,
                        bottom = clampedBottom,
                        topLeftCornerRadius = topCorner,
                        topRightCornerRadius = topCorner,
                        bottomRightCornerRadius = bottomCorner,
                        bottomLeftCornerRadius = bottomCorner
                    )
                )
            }
        }
        return ComposeOutline.Generic(path)
    }
}

/**
 * Renders the real captured live display backdrop strictly underneath glass panel & notification
 * cards in the accessibility overlay, passing the live screen pixels through the AGSL
 * [createFrostedBackdropRenderEffect] + [LIQUID_GLASS_BACKDROP_REFRACTION_AGSL] shader so
 * apps, icons, and text behind every glass card optically bend, magnify, and disperse in real time.
 */
@Composable
fun LiveScreenRefractionBackdrop(
    screenBitmap: Bitmap?,
    enableBlur: Boolean,
    enableRefraction: Boolean,
    refractionSensitivity: Float,
    blurRadiusDp: Float,
    transparency: Float,
    isDark: Boolean,
    revealAlpha: Float,
    qsCardBounds: QsCardBlurBounds?,
    extraBlurRegions: List<QsCardBlurBounds> = emptyList(),
    hostOffsetX: Float = 0f,
    hostOffsetY: Float = 0f,
    modifier: Modifier = Modifier,
    refractionStyle: RefractionStyle? = null,
    refractionScale: Float? = null,
    refractionDispersion: Float? = null,
    refractionStyleParam: Float? = null,
    diamondCrossedCut: Boolean? = null
) {
    val isDynamic = PanelPreferences.isDynamicRefractionActive()
    if (!isAndroid16FrostedGlassSupported() ||
        !isRuntimeShaderRefractionSupported() ||
        !enableBlur ||
        !enableRefraction ||
        (!isDynamic && (screenBitmap == null || screenBitmap.isRecycled))
    ) return
    val performanceMode by PanelPreferences.glassPerformanceMode.collectAsState()
    val prefRefractionStyle by PanelPreferences.refractionStyle.collectAsState()
    val prefRefractionScale by PanelPreferences.glassRefractionScale.collectAsState()
    val prefRefractionDispersion by PanelPreferences.glassRefractionDispersion.collectAsState()
    val prefRefractionStyleParam by PanelPreferences.glassRefractionCustomParam.collectAsState()
    val prefDiamondCrossedCut by PanelPreferences.diamondCrossedCut.collectAsState()
    val resolvedStyle = refractionStyle ?: prefRefractionStyle
    val resolvedScale = refractionScale ?: prefRefractionScale
    val resolvedDispersion = refractionDispersion ?: prefRefractionDispersion
    val resolvedStyleParam = refractionStyleParam ?: prefRefractionStyleParam
    val resolvedDiamondCrossed = diamondCrossedCut ?: prefDiamondCrossedCut
    val rawActiveRegions = remember(qsCardBounds, extraBlurRegions) {
        buildList {
            if (qsCardBounds != null && qsCardBounds.isValid) add(qsCardBounds)
            extraBlurRegions.forEach { region ->
                if (region.isValid) add(region)
            }
        }
    }
    val activeRegions = remember(rawActiveRegions, performanceMode) {
        if (performanceMode) quantizeBlurRegionsForPerformance(rawActiveRegions, stepPx = 2f)
        else rawActiveRegions
    }
    if (activeRegions.isEmpty() || revealAlpha <= 0.01f) return

    val density = LocalDensity.current
    val effectiveBlurDp = blurRadiusDp.coerceIn(0f, 80f)
    val effectiveBlurPx = with(density) { effectiveBlurDp.dp.toPx() }
    val skipBlurPass = effectiveBlurPx <= 0.5f || effectiveBlurDp <= 0.25f || (performanceMode && effectiveBlurDp < 0.75f)
    var viewportWidthPx by remember { mutableStateOf(screenBitmap?.width?.toFloat() ?: 1080f) }
    var viewportHeightPx by remember { mutableStateOf(screenBitmap?.height?.toFloat() ?: 2400f) }

    val drawPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG) }
    val glassPaint = remember { Paint() }
    val hostView = LocalView.current
    val locBuffer = remember { IntArray(2) }
    var measuredOffsetX by remember { mutableStateOf(0f) }
    var measuredOffsetY by remember { mutableStateOf(0f) }
    val resolvedOffsetX = if (hostOffsetX != 0f) hostOffsetX else measuredOffsetX
    val resolvedOffsetY = if (hostOffsetY != 0f) hostOffsetY else measuredOffsetY

    // When Performance Mode is enabled with non-zero blur, downsample blur pass to 0.5x inside a
    // cached GPU RenderNode (75% fewer blur pixels + skips re-blurring across static frames),
    // while keeping Liquid Glass edge refraction & inner corner rounding at crisp 1x resolution.
    val useDownscaledBlurNode = performanceMode && !skipBlurPass && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val cachedDownscaledBlurNode = remember(
        useDownscaledBlurNode,
        isDynamic,
        screenBitmap,
        effectiveBlurPx,
        effectiveBlurDp,
        isDark,
        transparency,
        viewportWidthPx,
        viewportHeightPx,
        resolvedOffsetX,
        resolvedOffsetY
    ) {
        if (!useDownscaledBlurNode || viewportWidthPx <= 1f || viewportHeightPx <= 1f || screenBitmap == null || screenBitmap.isRecycled) {
            null
        } else {
            try {
                val halfW = (viewportWidthPx * 0.5f).roundToInt().coerceAtLeast(1)
                val halfH = (viewportHeightPx * 0.5f).roundToInt().coerceAtLeast(1)
                val halfBlurEffect = createFrostedBlurOnlyPlatformRenderEffect(
                    blurRadiusPx = (effectiveBlurPx * 0.5f).coerceAtLeast(0.6f),
                    isDark = isDark,
                    blurRadiusDp = effectiveBlurDp,
                    transparency = transparency
                )
                android.graphics.RenderNode("LiveBackdropDownscaleBlur").apply {
                    setPosition(0, 0, halfW, halfH)
                    setRenderEffect(halfBlurEffect)
                    val rec = beginRecording(halfW, halfH)
                    rec.scale(0.5f, 0.5f)
                    val dstRect = RectF(
                        -resolvedOffsetX,
                        -resolvedOffsetY,
                        -resolvedOffsetX + (screenBitmap.width.toFloat()),
                        -resolvedOffsetY + (screenBitmap.height.toFloat())
                    )
                    val nodePaintAlpha = if (isDynamic) {
                        ((1f - (transparency * 0.35f).coerceIn(0f, 0.40f)) * 255f).roundToInt().coerceIn(160, 255)
                    } else {
                        255
                    }
                    drawPaint.alpha = nodePaintAlpha
                    rec.drawBitmap(screenBitmap, null, dstRect, drawPaint)
                    endRecording()
                }
            } catch (_: Throwable) {
                null
            }
        }
    }

    DisposableEffect(cachedDownscaledBlurNode) {
        val node = cachedDownscaledBlurNode
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    node?.discardDisplayList()
                } catch (_: Throwable) {}
            }
        }
    }

    val frostedRefractionEffect = remember(
        effectiveBlurPx,
        isDark,
        effectiveBlurDp,
        transparency,
        refractionSensitivity,
        activeRegions,
        viewportWidthPx,
        viewportHeightPx,
        density.density,
        performanceMode,
        useDownscaledBlurNode,
        resolvedStyle,
        resolvedScale,
        resolvedDispersion,
        resolvedStyleParam,
        resolvedDiamondCrossed
    ) {
        if (useDownscaledBlurNode) {
            // Blur is handled by the 0.5x cached RenderNode; keep 1x RenderEffect for crisp refraction only
            createFrostedBackdropRenderEffect(
                blurRadiusPx = 0f,
                isDark = isDark,
                blurRadiusDp = 0f,
                transparency = transparency,
                enableGlassRefraction = true,
                glassRefractionSensitivity = refractionSensitivity,
                refractionRegions = activeRegions,
                viewportWidthPx = viewportWidthPx,
                viewportHeightPx = viewportHeightPx,
                density = density.density,
                performanceMode = true,
                refractionStyle = resolvedStyle,
                patternScale = resolvedScale,
                dispersion = resolvedDispersion,
                styleParam = resolvedStyleParam,
                diamondCrossed = resolvedDiamondCrossed
            )
        } else {
            createFrostedBackdropRenderEffect(
                blurRadiusPx = effectiveBlurPx,
                isDark = isDark,
                blurRadiusDp = effectiveBlurDp,
                transparency = transparency,
                enableGlassRefraction = true,
                glassRefractionSensitivity = refractionSensitivity,
                refractionRegions = activeRegions,
                viewportWidthPx = viewportWidthPx,
                viewportHeightPx = viewportHeightPx,
                density = density.density,
                performanceMode = performanceMode,
                refractionStyle = resolvedStyle,
                patternScale = resolvedScale,
                dispersion = resolvedDispersion,
                styleParam = resolvedStyleParam,
                diamondCrossed = resolvedDiamondCrossed
            )
        }
    }

    val fullResFallbackEffect = remember(
        effectiveBlurPx,
        isDark,
        effectiveBlurDp,
        transparency,
        refractionSensitivity,
        activeRegions,
        viewportWidthPx,
        viewportHeightPx,
        density.density,
        performanceMode,
        useDownscaledBlurNode,
        resolvedStyle,
        resolvedScale,
        resolvedDispersion,
        resolvedStyleParam,
        resolvedDiamondCrossed
    ) {
        if (!useDownscaledBlurNode) null
        else createFrostedBackdropRenderEffect(
            blurRadiusPx = effectiveBlurPx,
            isDark = isDark,
            blurRadiusDp = effectiveBlurDp,
            transparency = transparency,
            enableGlassRefraction = true,
            glassRefractionSensitivity = refractionSensitivity,
            refractionRegions = activeRegions,
            viewportWidthPx = viewportWidthPx,
            viewportHeightPx = viewportHeightPx,
            density = density.density,
            performanceMode = true,
            refractionStyle = resolvedStyle,
            patternScale = resolvedScale,
            dispersion = resolvedDispersion,
            styleParam = resolvedStyleParam,
            diamondCrossed = resolvedDiamondCrossed
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                viewportWidthPx = coords.size.width.toFloat().coerceAtLeast(1f)
                viewportHeightPx = coords.size.height.toFloat().coerceAtLeast(1f)
                if (hostOffsetX == 0f && hostOffsetY == 0f && hostView.isAttachedToWindow) {
                    hostView.getLocationOnScreen(locBuffer)
                    measuredOffsetX = locBuffer[0].toFloat()
                    measuredOffsetY = locBuffer[1].toFloat()
                }
            }
            .clip(QsCardBlurRegionsClipShape(activeRegions))
            .graphicsLayer {
                alpha = revealAlpha.coerceIn(0f, 1f)
                clip = true
                val chosenEffect = if (cachedDownscaledBlurNode != null && hostView.isHardwareAccelerated) {
                    frostedRefractionEffect
                } else {
                    fullResFallbackEffect ?: frostedRefractionEffect
                }
                if (chosenEffect != null) {
                    renderEffect = chosenEffect
                }
            }
            .testTag("live_screen_refraction_backdrop")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                val nc = canvas.nativeCanvas
                if (screenBitmap != null && !screenBitmap.isRecycled) {
                    val paintAlpha = if (isDynamic) {
                        ((1f - (transparency * 0.35f).coerceIn(0f, 0.40f)) * 255f).roundToInt().coerceIn(160, 255)
                    } else {
                        255
                    }
                    drawPaint.alpha = paintAlpha
                    if (cachedDownscaledBlurNode != null &&
                        nc.isHardwareAccelerated &&
                        cachedDownscaledBlurNode.hasDisplayList()
                    ) {
                        nc.save()
                        nc.scale(2f, 2f)
                        nc.drawRenderNode(cachedDownscaledBlurNode)
                        nc.restore()
                    } else {
                        val dstRect = RectF(
                            -resolvedOffsetX,
                            -resolvedOffsetY,
                            -resolvedOffsetX + screenBitmap.width.toFloat(),
                            -resolvedOffsetY + screenBitmap.height.toFloat()
                        )
                        nc.drawBitmap(screenBitmap, null, dstRect, drawPaint)
                    }
                } else {
                    val glassAlpha = ((1f - transparency.coerceIn(0f, 0.95f)) * (if (isDark) 0.35f else 0.25f)).coerceIn(0.04f, 0.40f)
                    val tintR = if (isDark) 28 else 235
                    val tintG = if (isDark) 30 else 238
                    val tintB = if (isDark) 36 else 244
                    val alphaInt = (glassAlpha * 255f).toInt().coerceIn(1, 255)
                    val glassColor = AndroidColor.argb(alphaInt, tintR, tintG, tintB)
                    glassPaint.color = glassColor
                    nc.drawRect(0f, 0f, size.width, size.height, glassPaint)
                }
            }
        }
    }
}

/**
 * Preview-tab underlying home screen/wallpaper backdrop rendered strictly *beneath* [QuickSettingsPanel].
 * When [enableBlur] is true:
 *  - Chains [AndroidRenderEffect.createBlurEffect] with Android 16+ [RuntimeColorFilter] and,
 *    when [enableGlassRefraction] is enabled on API 33+, chains [AndroidRenderEffect.createRuntimeShaderEffect]
 *    so the underlying preview widgets and icons genuinely refract through every glass card.
 */
@Composable
fun FrostedPreviewBackdrop(
    enableBlur: Boolean,
    dimAlpha: Float,
    modifier: Modifier = Modifier,
    blurOnlyBehindQsCard: Boolean = false,
    qsCardBounds: QsCardBlurBounds? = null,
    blurRadiusDp: Float = DEFAULT_FROSTED_BLUR_RADIUS_DP,
    transparency: Float = 0.40f,
    extraBlurRegions: List<QsCardBlurBounds> = emptyList(),
    enableGlassRefraction: Boolean? = null,
    glassRefractionSensitivity: Float? = null,
    refractionStyle: RefractionStyle? = null,
    glassRefractionScale: Float? = null,
    glassRefractionDispersion: Float? = null,
    glassRefractionStyleParam: Float? = null,
    diamondCrossedCut: Boolean? = null
) {
    val glassMode by PanelPreferences.glassBackdropMode.collectAsState()
    val prefRefractionSens by PanelPreferences.glassRefractionSensitivity.collectAsState()
    val prefRefractionStyle by PanelPreferences.refractionStyle.collectAsState()
    val prefRefractionScale by PanelPreferences.glassRefractionScale.collectAsState()
    val prefRefractionDispersion by PanelPreferences.glassRefractionDispersion.collectAsState()
    val prefRefractionStyleParam by PanelPreferences.glassRefractionCustomParam.collectAsState()
    val prefDiamondCrossedCut by PanelPreferences.diamondCrossedCut.collectAsState()
    val performanceMode by PanelPreferences.glassPerformanceMode.collectAsState()
    val effectiveBlur = enableBlur && isAndroid16FrostedGlassSupported()
    val resolvedRefraction = (enableGlassRefraction ?: (glassMode == GlassBackdropMode.LIQUID)) &&
        isRuntimeShaderRefractionSupported()
    val resolvedRefractionSens = glassRefractionSensitivity ?: prefRefractionSens
    val resolvedRefractionStyle = refractionStyle ?: prefRefractionStyle
    val resolvedRefractionScale = glassRefractionScale ?: prefRefractionScale
    val resolvedRefractionDispersion = glassRefractionDispersion ?: prefRefractionDispersion
    val resolvedRefractionStyleParam = glassRefractionStyleParam ?: prefRefractionStyleParam
    val resolvedDiamondCrossed = diamondCrossedCut ?: prefDiamondCrossedCut

    val rawActiveRegions = remember(qsCardBounds, extraBlurRegions) {
        buildList {
            if (qsCardBounds != null && qsCardBounds.isValid) add(qsCardBounds)
            extraBlurRegions.forEach { region ->
                if (region.isValid) add(region)
            }
        }
    }
    val activeRegions = remember(rawActiveRegions, performanceMode) {
        if (performanceMode) quantizeBlurRegionsForPerformance(rawActiveRegions, stepPx = 2f)
        else rawActiveRegions
    }
    val shouldBlurFullScreen = effectiveBlur && !blurOnlyBehindQsCard
    val shouldBlurQsOnly = effectiveBlur && blurOnlyBehindQsCard && activeRegions.isNotEmpty()
    val effectiveBlurDp = blurRadiusDp.coerceIn(0f, 80f)
    val density = LocalDensity.current
    val effectiveBlurPx = with(density) { effectiveBlurDp.dp.toPx() }
    val skipBlurPass = effectiveBlurPx <= 0.5f || effectiveBlurDp <= 0.25f || (performanceMode && effectiveBlurDp < 0.75f)
    val isDark = MaterialTheme.colorScheme.surface.let {
        (0.2126f * it.red + 0.7152f * it.green + 0.0722f * it.blue) < 0.5f
    }
    var viewportWidthPx by remember { mutableStateOf(0f) }
    var viewportHeightPx by remember { mutableStateOf(0f) }

    val frostedRenderEffect = remember(
        effectiveBlur,
        effectiveBlurPx,
        isDark,
        effectiveBlurDp,
        transparency,
        resolvedRefraction,
        resolvedRefractionSens,
        resolvedRefractionStyle,
        resolvedRefractionScale,
        resolvedRefractionDispersion,
        resolvedRefractionStyleParam,
        resolvedDiamondCrossed,
        activeRegions,
        viewportWidthPx,
        viewportHeightPx,
        density.density,
        performanceMode
    ) {
        if (!effectiveBlur) null
        else createFrostedBackdropRenderEffect(
            blurRadiusPx = effectiveBlurPx,
            isDark = isDark,
            blurRadiusDp = effectiveBlurDp,
            transparency = transparency,
            enableGlassRefraction = resolvedRefraction,
            glassRefractionSensitivity = resolvedRefractionSens,
            refractionRegions = activeRegions,
            viewportWidthPx = viewportWidthPx,
            viewportHeightPx = viewportHeightPx,
            density = density.density,
            performanceMode = performanceMode,
            refractionStyle = resolvedRefractionStyle,
            patternScale = resolvedRefractionScale,
            dispersion = resolvedRefractionDispersion,
            styleParam = resolvedRefractionStyleParam,
            diamondCrossed = resolvedDiamondCrossed
        )
    }

    val blurModifier = when {
        frostedRenderEffect != null -> Modifier.graphicsLayer {
            clip = true
            renderEffect = frostedRenderEffect
        }
        !skipBlurPass -> Modifier.blur(
            radius = effectiveBlurDp.dp,
            edgeTreatment = BlurredEdgeTreatment.Rectangle
        )
        else -> Modifier
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                viewportWidthPx = coords.size.width.toFloat()
                viewportHeightPx = coords.size.height.toFloat()
            }
            .testTag("frosted_preview_backdrop_root")
    ) {
        // Base underlying wallpaper & dock layer (blurs full-screen when full blur is active, sharp otherwise)
        PreviewBackdropScene(
            modifier = (if (shouldBlurFullScreen) {
                Modifier
                    .fillMaxSize()
                    .then(blurModifier)
            } else {
                Modifier.fillMaxSize()
            }).testTag("frosted_preview_backdrop_layer")
        )

        // When "Blur Behind QS/QQS Only" is active, overlay the frosted + refracted scene clipped strictly to QS/QQS + carrier + notification bounds
        if (shouldBlurQsOnly) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(QsCardBlurRegionsClipShape(activeRegions))
                    .testTag("frosted_preview_qs_blur_region")
            ) {
                PreviewBackdropScene(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(blurModifier)
                )
            }
        }

        // Dim scrim over the underlying backdrop and beneath QuickSettingsPanel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimAlpha.coerceIn(0f, 1f)))
                .testTag("frosted_preview_scrim")
        )
    }
}

@Composable
private fun PreviewBackdropScene(
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.surface.let {
        (0.2126f * it.red + 0.7152f * it.green + 0.0722f * it.blue) < 0.5f
    }
    val solidWallpaperColor = if (isDark) Color(0xFF1B1D21) else Color(0xFFE2E4E9)

    Box(
        modifier = modifier.background(solidWallpaperColor)
    )
}

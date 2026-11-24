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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Quantizes intermediate animation values into 4px steps so SurfaceFlinger does not get flooded
 * on every spring frame while still reaching exact 0px (closed/off) and full target radius (open).
 */
fun computeWindowBlurBehindRadiusPx(
    enabled: Boolean,
    density: Float,
    fraction: Float = 1f,
    maxBlurDp: Float = DEFAULT_FROSTED_BLUR_RADIUS_DP
): Int {
    if (!enabled || density <= 0f || maxBlurDp <= 0f) return 0
    val clampedFraction = fraction.coerceIn(0f, 1f)
    if (clampedFraction <= 0.01f) return 0
    val maxPx = (maxBlurDp * density).roundToInt().coerceAtLeast(1)
    if (clampedFraction >= 0.99f) return maxPx
    val rawPx = (maxPx * clampedFraction).roundToInt().coerceIn(0, maxPx)
    if (rawPx <= 0) return 0
    return ((rawPx + 2) / 4 * 4).coerceIn(4, maxPx)
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
    private val maxNotificationSlots: Int = MAX_NOTIFICATION_BLUR_SLOTS
) {
    private val hosts: Map<String, QsCardBlurWindowHost> = regionKeys.associateWith {
        QsCardBlurWindowHost(context, layoutType)
    }

    private val notificationHosts: List<QsCardBlurWindowHost> = List(maxNotificationSlots.coerceAtLeast(0)) {
        QsCardBlurWindowHost(context, layoutType)
    }

    fun showBehindOverlay() {
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
    uniform float4 uRects[18];
    uniform float2 uRadii[18];
    uniform int regionCount;

    float sdRoundRect(float2 p, float2 halfSize, float topR, float botR) {
        float cr = (p.y < 0.0) ? topR : botR;
        cr = clamp(cr, 0.0, min(halfSize.x, halfSize.y));
        float2 d = abs(p) - (halfSize - float2(cr));
        return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - cr;
    }

    half4 main(float2 fragCoord) {
        half4 base = content.eval(fragCoord);
        float s = clamp(sensitivity, 0.1, 1.0);
        float dScale = max(density, 1.0);
        float bevelW = (14.0 + 22.0 * s) * dScale;

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
            float sdf = sdRoundRect(local, halfSize, topR, botR);
            if (sdf > 0.0) continue;

            float distIn = -sdf;

            // Smooth surface normal from central-difference SDF gradient
            float eps = 1.5;
            float gx = sdRoundRect(local + float2(eps, 0.0), halfSize, topR, botR)
                     - sdRoundRect(local - float2(eps, 0.0), halfSize, topR, botR);
            float gy = sdRoundRect(local + float2(0.0, eps), halfSize, topR, botR)
                     - sdRoundRect(local - float2(0.0, eps), halfSize, topR, botR);
            float2 n = float2(gx, gy);
            float nLen = length(n);
            n = (nLen > 0.0001) ? (n / nLen) : float2(0.0, -1.0);

            // Optical curved bevel chamfer + subtle peripheral pillow lens warp
            float edgeT = 1.0 - smoothstep(0.0, bevelW, distIn);
            float bevelMag = pow(edgeT, 1.45) * (14.0 + 36.0 * s) * dScale;
            float2 normLocal = local / max(halfSize, float2(1.0));
            float radialWeight = smoothstep(0.25, 1.0, length(normLocal)) * (1.0 - smoothstep(bevelW, bevelW * 3.0, distIn));
            float domeMag = radialWeight * (3.5 + 8.5 * s) * dScale;

            float2 disp = -n * (bevelMag + domeMag);

            // Chromatic dispersion: Red, Green, and Blue wavelengths bend at slightly different indices
            float chroma = (0.12 + 0.16 * s) * edgeT;
            float2 uvG = clamp(fragCoord + disp, float2(0.5), resolution - float2(0.5));
            float2 uvR = clamp(fragCoord + disp * (1.0 - chroma), float2(0.5), resolution - float2(0.5));
            float2 uvB = clamp(fragCoord + disp * (1.0 + chroma), float2(0.5), resolution - float2(0.5));

            half4 cG = content.eval(uvG);
            half cR = content.eval(uvR).r;
            half cB = content.eval(uvB).b;
            half4 refracted = half4(cR, cG.g, cB, cG.a);

            // Physically derived Fresnel specular rim + subtle inner caustic
            float rimMask = smoothstep(0.0, 1.5 * dScale, distIn) * (1.0 - smoothstep(1.2 * dScale, 6.0 * dScale, distIn));
            float fresnel = pow(edgeT, 3.0);
            float keyLight = clamp(dot(n, normalize(float2(-0.55, -0.83))), 0.0, 1.0);
            float backLight = clamp(dot(n, normalize(float2(0.55, 0.83))), 0.0, 1.0);
            float rimGlow = (0.28 + 0.72 * keyLight + 0.35 * backLight) * rimMask * (0.14 + 0.30 * s);
            float innerCaustic = fresnel * (0.04 + 0.08 * s) * (isDark > 0.5 ? 0.65 : 0.90);

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

    float sdRoundRect(float2 p, float2 halfSize, float topR, float botR) {
        float cr = (p.y < 0.0) ? topR : botR;
        cr = clamp(cr, 0.0, min(halfSize.x, halfSize.y));
        float2 d = abs(p) - (halfSize - float2(cr));
        return length(max(d, 0.0)) + min(max(d.x, d.y), 0.0) - cr;
    }

    half4 main(float2 fragCoord) {
        float2 halfSize = cardSize * 0.5;
        if (halfSize.x <= 1.0 || halfSize.y <= 1.0) return half4(0.0);

        float2 local = fragCoord - halfSize;
        float sdf = sdRoundRect(local, halfSize, topRadius, botRadius);
        if (sdf > 0.0) return half4(0.0);

        float distIn = -sdf;
        float dScale = max(density, 1.0);
        float s = clamp(sensitivity, 0.1, 1.0);

        float eps = 1.5;
        float gx = sdRoundRect(local + float2(eps, 0.0), halfSize, topRadius, botRadius)
                 - sdRoundRect(local - float2(eps, 0.0), halfSize, topRadius, botRadius);
        float gy = sdRoundRect(local + float2(0.0, eps), halfSize, topRadius, botRadius)
                 - sdRoundRect(local - float2(0.0, eps), halfSize, topRadius, botRadius);
        float2 n = float2(gx, gy);
        float nLen = length(n);
        n = (nLen > 0.0001) ? (n / nLen) : float2(0.0, -1.0);

        float bevelW = (10.0 + 14.0 * s) * dScale;
        float edgeT = 1.0 - smoothstep(0.0, bevelW, distIn);
        float rimBand = smoothstep(0.0, 1.35 * dScale, distIn) * (1.0 - smoothstep(1.1 * dScale, 4.8 * dScale, distIn));

        float keyLight = clamp(dot(n, normalize(float2(-0.55, -0.83))), 0.0, 1.0);
        float counterLight = clamp(dot(n, normalize(float2(0.55, 0.83))), 0.0, 1.0);

        // Subtle chromatic dispersion across the bevel normal
        float spectrumPhase = n.x * 0.65 - n.y * 0.35;
        half3 prismTint = half3(
            clamp(1.0 + 0.18 * spectrumPhase, 0.82, 1.0),
            1.0,
            clamp(1.0 - 0.18 * spectrumPhase, 0.82, 1.0)
        );

        float highlightStrength = (0.14 + 0.28 * s) * (0.28 + 0.72 * keyLight) * rimBand;
        float causticStrength = pow(edgeT, 2.6) * (0.04 + 0.07 * s) * (0.4 + 0.6 * keyLight);
        float shadowStrength = (isDark > 0.5 ? 0.28 : 0.12) * s * counterLight * rimBand;

        half3 lightColor = prismTint * half(highlightStrength + causticStrength);
        float outAlpha = clamp(highlightStrength + causticStrength + shadowStrength, 0.0, 0.65);
        return half4(lightColor, half(outAlpha));
    }
"""

fun isAndroid16FrostedGlassSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA

fun isRuntimeShaderRefractionSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

@RequiresApi(Build.VERSION_CODES.BAKLAVA)
fun createAndroid16FrostedBackdropColorFilter(
    isDark: Boolean,
    blurRadiusDp: Float,
    transparency: Float = 0.40f
): RuntimeColorFilter? {
    return try {
        val radiusNormalized = (blurRadiusDp.coerceIn(4f, 60f) / 60f)
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
    isDark: Boolean = true
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
    isDark: Boolean
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
 * Builds the GPU RenderEffect pipeline for frosted glass backdrops:
 * - Chains [AndroidRenderEffect.createBlurEffect] with Android 16's AGSL [RuntimeColorFilter]
 *   on API 36+.
 * - When [enableGlassRefraction] is true on API 33+ and [refractionRegions] has active glass cards
 *   with valid viewport size, chains [AndroidRenderEffect.createRuntimeShaderEffect] so underlying
 *   content is optically refracted and chromatically dispersed along every card boundary.
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
    density: Float = 2.625f
): ComposeRenderEffect? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || blurRadiusPx <= 0.5f) return null
    val baseBlur = try {
        AndroidRenderEffect.createBlurEffect(
            blurRadiusPx,
            blurRadiusPx,
            Shader.TileMode.CLAMP
        )
    } catch (_: Throwable) {
        return null
    }

    var platformEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
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

    if (enableGlassRefraction &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        refractionRegions.isNotEmpty() &&
        viewportWidthPx > 1f &&
        viewportHeightPx > 1f
    ) {
        val refractionEffect = createLiquidGlassRefractionRuntimeEffect(
            regions = refractionRegions,
            viewportWidthPx = viewportWidthPx,
            viewportHeightPx = viewportHeightPx,
            density = density,
            sensitivity = glassRefractionSensitivity,
            isDark = isDark
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
    cornerRadiusDp: Float = 24f
): Modifier = composed {
    val effectiveColor = frostedContainerColor(
        baseColor = baseColor,
        enableBlur = enableBlur,
        isDark = isDark,
        transparency = transparency
    )
    if (!enableBlur) {
        return@composed this.background(effectiveColor)
    }

    val resolvedTransparency = transparency ?: if (isDark) 0.44f else 0.32f
    val api36ColorFilter = remember(isDark, resolvedTransparency) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            createAndroid16FrostedSurfaceColorFilter(
                isDark = isDark,
                transparency = resolvedTransparency
            )
        } else {
            null
        }
    }
    val api36Xfermode = remember(isDark, resolvedTransparency) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            createAndroid16FrostedSurfaceXfermode(
                isDark = isDark,
                transparency = resolvedTransparency
            )
        } else {
            null
        }
    }
    val surfacePaint = remember(api36ColorFilter, api36Xfermode) {
        Paint().apply {
            isAntiAlias = true
            if (api36ColorFilter != null) {
                colorFilter = api36ColorFilter
            }
            if (api36Xfermode != null) {
                xfermode = api36Xfermode
            }
        }
    }
    val bevelPaint = remember {
        Paint().apply {
            isAntiAlias = true
        }
    }

    val enableRefraction by PanelPreferences.enableGlassRefraction.collectAsState()
    val refractionSensitivity by PanelPreferences.glassRefractionSensitivity.collectAsState()
    val density = LocalDensity.current

    val sheenStrength = ((1f - resolvedTransparency.coerceIn(0f, 0.95f)) * if (isDark) 0.09f else 0.14f)
        .coerceIn(0.015f, 0.16f)
    val topSheen = Color.White.copy(alpha = sheenStrength)
    val bottomSheen = Color.Transparent
    val cornerPx = with(density) { cornerRadiusDp.coerceAtLeast(0f).dp.toPx() }
    val densityPx = density.density

    this
        .background(effectiveColor)
        .drawBehind {
            if (size.width <= 0f || size.height <= 0f) return@drawBehind
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA && api36ColorFilter != null) {
                drawIntoCanvas { canvas ->
                    surfacePaint.shader = LinearGradient(
                        0f,
                        0f,
                        0f,
                        size.height,
                        topSheen.toArgb(),
                        bottomSheen.toArgb(),
                        Shader.TileMode.CLAMP
                    )
                    canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, surfacePaint)
                }
            } else {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(topSheen, bottomSheen)
                    )
                )
            }

            // Real AGSL rounded-rect SDF Fresnel dispersion & bevel shader on API 33+
            if (enableRefraction && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val bevelShader = createFrostedGlassCardBevelShader(
                    widthPx = size.width,
                    heightPx = size.height,
                    topRadiusPx = cornerPx,
                    bottomRadiusPx = cornerPx,
                    density = densityPx,
                    sensitivity = refractionSensitivity,
                    isDark = isDark
                )
                if (bevelShader != null) {
                    bevelPaint.shader = bevelShader
                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, bevelPaint)
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
    modifier: Modifier = Modifier
) {
    if (!enableBlur || !enableRefraction || screenBitmap == null || screenBitmap.isRecycled) return
    val activeRegions = remember(qsCardBounds, extraBlurRegions) {
        buildList {
            if (qsCardBounds != null && qsCardBounds.isValid) add(qsCardBounds)
            extraBlurRegions.forEach { region ->
                if (region.isValid) add(region)
            }
        }
    }
    if (activeRegions.isEmpty() || revealAlpha <= 0.01f) return

    val density = LocalDensity.current
    val effectiveBlurDp = blurRadiusDp.coerceIn(1f, 80f)
    val effectiveBlurPx = with(density) { effectiveBlurDp.dp.toPx() }
    var viewportWidthPx by remember { mutableStateOf(screenBitmap.width.toFloat()) }
    var viewportHeightPx by remember { mutableStateOf(screenBitmap.height.toFloat()) }

    val frostedRefractionEffect = remember(
        effectiveBlurPx,
        isDark,
        effectiveBlurDp,
        transparency,
        refractionSensitivity,
        activeRegions,
        viewportWidthPx,
        viewportHeightPx,
        density.density
    ) {
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
            density = density.density
        )
    }

    val drawPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG) }
    val hostView = LocalView.current
    val locBuffer = remember { IntArray(2) }
    var measuredOffsetX by remember { mutableStateOf(0f) }
    var measuredOffsetY by remember { mutableStateOf(0f) }
    val resolvedOffsetX = if (hostOffsetX != 0f) hostOffsetX else measuredOffsetX
    val resolvedOffsetY = if (hostOffsetY != 0f) hostOffsetY else measuredOffsetY

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
                if (frostedRefractionEffect != null) {
                    renderEffect = frostedRefractionEffect
                }
            }
            .testTag("live_screen_refraction_backdrop")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawIntoCanvas { canvas ->
                val dstRect = RectF(
                    -resolvedOffsetX,
                    -resolvedOffsetY,
                    -resolvedOffsetX + screenBitmap.width.toFloat(),
                    -resolvedOffsetY + screenBitmap.height.toFloat()
                )
                canvas.nativeCanvas.drawBitmap(screenBitmap, null, dstRect, drawPaint)
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
    glassRefractionSensitivity: Float? = null
) {
    val prefRefraction by PanelPreferences.enableGlassRefraction.collectAsState()
    val prefRefractionSens by PanelPreferences.glassRefractionSensitivity.collectAsState()
    val resolvedRefraction = enableGlassRefraction ?: prefRefraction
    val resolvedRefractionSens = glassRefractionSensitivity ?: prefRefractionSens

    val activeRegions = remember(qsCardBounds, extraBlurRegions) {
        buildList {
            if (qsCardBounds != null && qsCardBounds.isValid) add(qsCardBounds)
            extraBlurRegions.forEach { region ->
                if (region.isValid) add(region)
            }
        }
    }
    val shouldBlurFullScreen = enableBlur && !blurOnlyBehindQsCard
    val shouldBlurQsOnly = enableBlur && blurOnlyBehindQsCard && activeRegions.isNotEmpty()
    val effectiveBlurDp = blurRadiusDp.coerceIn(1f, 80f)
    val density = LocalDensity.current
    val effectiveBlurPx = with(density) { effectiveBlurDp.dp.toPx() }
    val isDark = MaterialTheme.colorScheme.surface.let {
        (0.2126f * it.red + 0.7152f * it.green + 0.0722f * it.blue) < 0.5f
    }
    var viewportWidthPx by remember { mutableStateOf(0f) }
    var viewportHeightPx by remember { mutableStateOf(0f) }

    val frostedRenderEffect = remember(
        enableBlur,
        effectiveBlurPx,
        isDark,
        effectiveBlurDp,
        transparency,
        resolvedRefraction,
        resolvedRefractionSens,
        activeRegions,
        viewportWidthPx,
        viewportHeightPx,
        density.density
    ) {
        if (!enableBlur) null
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
            density = density.density
        )
    }

    val blurModifier = when {
        frostedRenderEffect != null -> Modifier.graphicsLayer {
            clip = true
            renderEffect = frostedRenderEffect
        }
        else -> Modifier.blur(
            radius = effectiveBlurDp.dp,
            edgeTreatment = BlurredEdgeTreatment.Rectangle
        )
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
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.55f),
                    secondaryColor.copy(alpha = 0.45f),
                    tertiaryColor.copy(alpha = 0.52f),
                    surfaceColor.copy(alpha = 0.85f)
                )
            )
        )
    ) {
        // Atmospheric wallpaper shapes so behind-panel blur has visible texture & depth
        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = (-40).dp, y = 40.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            tertiaryColor.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = (-80).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.80f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Simulated underlying home-screen widgets & app icons beneath the shade
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Underlying widget silhouette
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.22f))
                    .border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "72° Partly Sunny",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Underlying app & wallpaper layer",
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(tertiaryColor.copy(alpha = 0.85f))
                )
            }

            // Underlying app dock silhouettes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val dockItems = listOf(
                    Icons.Default.CameraAlt to primaryColor,
                    Icons.Default.MusicNote to tertiaryColor,
                    Icons.Default.Email to secondaryColor,
                    Icons.Default.Photo to Color(0xFF4CAF50),
                    Icons.Default.Explore to Color(0xFFFF9800)
                )
                dockItems.forEach { (icon, tint) ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(tint.copy(alpha = 0.88f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

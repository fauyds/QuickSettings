package com.example.ui.shape

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.PathParser
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

data class QsShapeItem(
    val id: String,
    val label: String,
    val description: String,
    val pathData: String,
    val viewportWidth: Float = 100f,
    val viewportHeight: Float = 100f,
    @DrawableRes val drawableRes: Int? = null,
    val isCustom: Boolean = false,
    val rawXml: String? = null
)

/**
 * Compose Shape implementation backed by Android Vector Path data.
 * Scales dynamically to any component dimensions.
 */
class VectorPathShape(
    val pathData: String,
    val viewportWidth: Float = 100f,
    val viewportHeight: Float = 100f
) : Shape {
    private val basePath: android.graphics.Path by lazy {
        try {
            PathParser.createPathFromPathData(pathData)
        } catch (_: Exception) {
            android.graphics.Path()
        }
    }

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        if (size.width <= 0f || size.height <= 0f) {
            return Outline.Rectangle(androidx.compose.ui.geometry.Rect(0f, 0f, 0f, 0f))
        }
        val outPath = android.graphics.Path()
        val matrix = Matrix().apply {
            setScale(size.width / viewportWidth, size.height / viewportHeight)
        }
        basePath.transform(matrix, outPath)
        return Outline.Generic(outPath.asComposePath())
    }
}

/**
 * Drawable implementation backed by Android Vector Path data for traditional Views (like QsTileViewHelper).
 */
class QsPathDrawable(
    private val pathData: String,
    private val viewportWidth: Float = 100f,
    private val viewportHeight: Float = 100f,
    color: Int = Color.WHITE
) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    private val basePath: android.graphics.Path by lazy {
        try {
            PathParser.createPathFromPathData(pathData)
        } catch (_: Exception) {
            android.graphics.Path()
        }
    }
    private val scaledPath = android.graphics.Path()
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    var strokeWidth: Float = 0f
        set(value) {
            field = value
            invalidateSelf()
        }

    var strokeColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            invalidateSelf()
        }

    fun setColor(color: Int) {
        if (paint.color != color) {
            paint.color = color
            invalidateSelf()
        }
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        if (bounds.width() > 0 && bounds.height() > 0) {
            scaledPath.reset()
            val matrix = Matrix()
            matrix.setScale(bounds.width().toFloat() / viewportWidth, bounds.height().toFloat() / viewportHeight)
            basePath.transform(matrix, scaledPath)
            scaledPath.offset(bounds.left.toFloat(), bounds.top.toFloat())
        }
    }

    override fun draw(canvas: Canvas) {
        if (Color.alpha(paint.color) > 0) {
            canvas.drawPath(scaledPath, paint)
        }
        if (strokeWidth > 0f && strokeColor != Color.TRANSPARENT) {
            strokePaint.strokeWidth = strokeWidth
            strokePaint.color = strokeColor
            canvas.drawPath(scaledPath, strokePaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}

object QsShapeManager {
    private const val PREFS_NAME = "qs_custom_shapes_prefs"
    private const val KEY_IMPORTED_SHAPES = "imported_shapes_json"

    // 13 Built-in shapes, backed by res/drawable/qs_shape_*.xml
    val BUILT_IN_SHAPES: List<QsShapeItem> = listOf(
        QsShapeItem(
            id = "circle",
            label = "Circle",
            description = "Classic round tile shape",
            pathData = "M 50,0 C 77.614,0 100,22.386 100,50 C 100,77.614 77.614,100 50,100 C 22.386,100 0,77.614 0,50 C 0,22.386 22.386,0 50,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_circle
        ),
        QsShapeItem(
            id = "squircle",
            label = "Squircle",
            description = "Continuous curvature squircle",
            pathData = "M50,0 C10,0 0,10 0,50 0,90 10,100 50,100 90,100 100,90 100,50 100,10 90,0 50,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_squircle
        ),
        QsShapeItem(
            id = "square",
            label = "Square",
            description = "Sharp square with 4dp corners",
            pathData = "M 8,0 L 92,0 C 96.418,0 100,3.582 100,8 L 100,92 C 100,96.418 96.418,100 92,100 L 8,100 C 3.582,100 0,96.418 0,92 L 0,8 C 0,3.582 3.582,0 8,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_square
        ),
        QsShapeItem(
            id = "teardrop",
            label = "Teardrop",
            description = "Asymmetric teardrop style",
            pathData = "M50,0A50,50,0,0 1 100,50 L100,85 A15,15,0,0 1 85,100 L50,100 A50,50,0,0 1 50,0z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_teardrop
        ),
        QsShapeItem(
            id = "pill",
            label = "12dp Square",
            description = "Square with 12dp rounded corners",
            pathData = "M 24,0 L 76,0 C 89.255,0 100,10.745 100,24 L 100,76 C 100,89.255 89.255,100 76,100 L 24,100 C 10.745,100 0,89.255 0,76 L 0,24 C 0,10.745 10.745,0 24,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_pill
        ),
        QsShapeItem(
            id = "custom_1",
            label = "Leaf",
            description = "Leaf shape with diagonal rounded corners",
            pathData = "M 48,0 L 88,0 C 94.627,0 100,5.373 100,12 L 100,52 C 100,78.51 78.51,100 52,100 L 12,100 C 5.373,100 0,94.627 0,88 L 0,48 C 0,21.49 21.49,0 48,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_custom_1
        ),
        QsShapeItem(
            id = "custom_3",
            label = "Arch",
            description = "Arch / dome with rounded top corners",
            pathData = "M 48,0 L 52,0 C 78.51,0 100,21.49 100,48 L 100,88 C 100,94.627 94.627,100 88,100 L 12,100 C 5.373,100 0,94.627 0,88 L 0,48 C 0,21.49 21.49,0 48,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_custom_3
        ),
        QsShapeItem(
            id = "custom_8",
            label = "Hexagon",
            description = "Six-sided regular hexagon",
            pathData = "M4.8 33V67c0 5.8 3 11 8 13.7l29.4 17c4.9 2.7 11 2.7 15.9 0l29.4 -17c4.9 -2.7 8 -8 8 -13.7V33c0 -5.8 -3 -11 -8 -13.7l-29.4 -17c-4.9 -2.7 -11 -2.7 -15.9 0l-29.7 17C7.8 22.2 4.8 27.5 4.8 33z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_custom_8
        ),
        QsShapeItem(
            id = "cloud",
            label = "Cloud",
            description = "Fluffy cloud shape with rounded bumps",
            pathData = "M4,50 C2,45 0,39 0,33 C0,15 15,0 33,0 C39,0 45,2 50,4 C55,2 61,0 67,0 C85,0 100,15 100,33 C100,39 98,45 96,50 C98,55 100,61 100,66 C100,85 85,100 66,100 C61,100 55,98 50,96 C45,98 39,100 33,100 C15,100 0,85 0,66 C0,61 2,55 3,50 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_cloud
        ),
        QsShapeItem(
            id = "octagon",
            label = "Octagon",
            description = "Eight-sided symmetrical polygon",
            pathData = "M20,0 80,0 100,20 100,80 80,100 20,100 0,80 0,20 20,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_octagon
        ),
        QsShapeItem(
            id = "stamp",
            label = "Stamp",
            description = "Postage stamp style with scalloped edges",
            pathData = "M12.97,0 C8.41,0 4.14,2.55 2.21,6.68 -1.03,13.61 -0.71,21.78 3.16,28.46 4.89,31.46 4.89,35.2 3.16,38.2 -1.05,45.48 -1.05,54.52 3.16,61.8 4.89,64.8 4.89,68.54 3.16,71.54 -0.71,78.22 -1.03,86.39 2.21,93.32 4.14,97.45 8.41,100 12.97,100 21.38,100 78.62,100 87.03,100 91.59,100 95.85,97.45 97.79,93.32 101.02,86.39 100.71,78.22 96.84,71.54 95.1,68.54 95.1,64.8 96.84,61.8 101.05,54.52 101.05,45.48 96.84,38.2 95.1,35.2 95.1,31.46 96.84,28.46 100.71,21.78 101.02,13.61 97.79,6.68 95.85,2.55 91.59,0 87.03,0 78.62,0 21.38,0 12.97,0 Z",
            viewportWidth = 100f,
            viewportHeight = 100f,
            drawableRes = R.drawable.qs_shape_stamp
        )
    )

    private val _allShapes = MutableStateFlow<List<QsShapeItem>>(BUILT_IN_SHAPES)
    val allShapes: StateFlow<List<QsShapeItem>> = _allShapes.asStateFlow()

    private val shapeCache = ConcurrentHashMap<String, VectorPathShape>()

    fun init(context: Context) {
        loadImportedShapes(context)
    }

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun loadImportedShapes(context: Context) {
        val jsonStr = getPrefs(context).getString(KEY_IMPORTED_SHAPES, null)
        val importedList = mutableListOf<QsShapeItem>()
        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    importedList.add(
                        QsShapeItem(
                            id = obj.getString("id"),
                            label = obj.getString("label"),
                            description = obj.optString("description", "User imported XML shape"),
                            pathData = obj.getString("pathData"),
                            viewportWidth = obj.optDouble("viewportWidth", 100.0).toFloat(),
                            viewportHeight = obj.optDouble("viewportHeight", 100.0).toFloat(),
                            isCustom = true,
                            rawXml = obj.optString("rawXml", null)
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _allShapes.value = BUILT_IN_SHAPES + importedList
    }

    private fun saveImportedShapes(context: Context, imported: List<QsShapeItem>) {
        val array = JSONArray()
        imported.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("label", item.label)
                put("description", item.description)
                put("pathData", item.pathData)
                put("viewportWidth", item.viewportWidth.toDouble())
                put("viewportHeight", item.viewportHeight.toDouble())
                put("rawXml", item.rawXml)
            }
            array.put(obj)
        }
        getPrefs(context).edit().putString(KEY_IMPORTED_SHAPES, array.toString()).apply()
        _allShapes.value = BUILT_IN_SHAPES + imported
    }

    /**
     * Generates a standard Android VectorDrawable XML from path data and viewport dimensions.
     */
    fun generateVectorXml(pathData: String, viewportWidth: Float = 100f, viewportHeight: Float = 100f): String {
        return """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="${viewportWidth.toInt()}dp"
    android:height="${viewportHeight.toInt()}dp"
    android:viewportWidth="$viewportWidth"
    android:viewportHeight="$viewportHeight">
    <path
        android:fillColor="#FFFFFFFF"
        android:pathData="$pathData" />
</vector>""".trimIndent()
    }

    /**
     * Cleans up pasted path data if the user included attribute wrappers like android:pathData="..." or quotes.
     */
    fun cleanPathData(rawInput: String): String {
        val trimmed = rawInput.trim()
        val attrRegex = Regex("""(?:android:pathData|pathData|\bd)\s*=\s*["']([^"']+)["']""")
        val match = attrRegex.find(trimmed)
        if (match != null) {
            return match.groupValues[1].trim()
        }
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) ||
            (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length - 1).trim()
        }
        return trimmed
    }

    /**
     * Parses raw path data (e.g. SVG/Vector path like "M 50,0 C ... Z") into a QsShapeItem.
     * Computes bounds if default 100x100 is specified but coordinates differ.
     */
    fun parsePathData(
        rawPathData: String,
        fallbackLabel: String = "Custom Shape",
        viewportWidth: Float = 100f,
        viewportHeight: Float = 100f
    ): QsShapeItem {
        val clean = cleanPathData(rawPathData)
        if (clean.isBlank()) {
            throw IllegalArgumentException("Path data cannot be empty")
        }

        val path: android.graphics.Path
        try {
            path = PathParser.createPathFromPathData(clean)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid path data syntax: ${e.message}")
        }

        var finalVpWidth = viewportWidth
        var finalVpHeight = viewportHeight

        val bounds = android.graphics.RectF()
        path.computeBounds(bounds, true)
        if (viewportWidth == 100f && viewportHeight == 100f && bounds.width() > 0 && bounds.height() > 0) {
            val maxBound = maxOf(bounds.right, bounds.bottom)
            if (maxBound > 105f || (maxBound in 1f..32f)) {
                val ceilMax = kotlin.math.ceil(maxBound)
                finalVpWidth = ceilMax
                finalVpHeight = ceilMax
            }
        }

        val id = "imported_" + System.currentTimeMillis()
        val rawXml = generateVectorXml(clean, finalVpWidth, finalVpHeight)

        return QsShapeItem(
            id = id,
            label = fallbackLabel.trim().ifBlank { "Custom Shape" },
            description = "User custom path shape",
            pathData = clean,
            viewportWidth = finalVpWidth,
            viewportHeight = finalVpHeight,
            isCustom = true,
            rawXml = rawXml
        )
    }

    /**
     * Parses an XML Vector Drawable string and returns a QsShapeItem.
     * Throws IllegalArgumentException on invalid XML or missing pathData.
     */
    fun parseXmlDrawable(xmlContent: String, fallbackLabel: String = "Imported Shape"): QsShapeItem {
        val trimmed = xmlContent.trim()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("XML content cannot be empty")
        }

        // Viewport dimensions
        val vpWidthRegex = Regex("""(?:android:)?viewportWidth\s*=\s*["']([0-9.]+)["']""")
        val vpHeightRegex = Regex("""(?:android:)?viewportHeight\s*=\s*["']([0-9.]+)["']""")
        val vpWidth = vpWidthRegex.find(trimmed)?.groupValues?.get(1)?.toFloatOrNull() ?: 100f
        val vpHeight = vpHeightRegex.find(trimmed)?.groupValues?.get(1)?.toFloatOrNull() ?: 100f

        // Path data
        val pathRegex = Regex("""(?:android:pathData|pathData|\bd)\s*=\s*["']([^"']+)["']""")
        val pathMatches = pathRegex.findAll(trimmed).map { it.groupValues[1].trim() }.filter { it.isNotEmpty() }.toList()

        if (pathMatches.isEmpty()) {
            throw IllegalArgumentException("No pathData or vector path found in the provided XML")
        }

        val combinedPath = pathMatches.joinToString(" ")

        // Validate path with PathParser
        try {
            PathParser.createPathFromPathData(combinedPath)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid path data syntax in XML: ${e.message}")
        }

        // Try extracting label from android:name
        val nameRegex = Regex("""(?:android:)?name\s*=\s*["']([^"']+)["']""")
        val detectedName = nameRegex.find(trimmed)?.groupValues?.get(1)?.trim()
        val label = if (!detectedName.isNullOrBlank() && detectedName.length <= 25) {
            detectedName
        } else {
            fallbackLabel
        }

        val id = "imported_" + System.currentTimeMillis()

        return QsShapeItem(
            id = id,
            label = label,
            description = "User imported XML drawable shape",
            pathData = combinedPath,
            viewportWidth = vpWidth,
            viewportHeight = vpHeight,
            isCustom = true,
            rawXml = trimmed
        )
    }

    /**
     * Intelligently parses input that can be either full XML (<vector>) or raw path data ("M 50,0...").
     */
    fun parseShapeInput(
        input: String,
        fallbackLabel: String = "Custom Shape",
        defaultVpWidth: Float = 100f,
        defaultVpHeight: Float = 100f
    ): QsShapeItem {
        val trimmed = input.trim()
        return if (trimmed.startsWith("<") || trimmed.contains("<vector") || trimmed.contains("<path")) {
            parseXmlDrawable(trimmed, fallbackLabel)
        } else {
            parsePathData(trimmed, fallbackLabel, defaultVpWidth, defaultVpHeight)
        }
    }

    /**
     * Imports a shape directly from a raw path string (e.g. SVG path data).
     */
    fun importShapeFromPathData(
        context: Context,
        pathData: String,
        customLabel: String? = null,
        viewportWidth: Float = 100f,
        viewportHeight: Float = 100f
    ): Result<QsShapeItem> {
        return try {
            val labelToUse = customLabel?.trim()?.takeIf { it.isNotEmpty() } ?: "Custom Shape"
            val shape = parsePathData(pathData, labelToUse, viewportWidth, viewportHeight)
            val finalShape = if (!customLabel.isNullOrBlank()) shape.copy(label = customLabel.trim()) else shape

            val currentCustom = _allShapes.value.filter { it.isCustom }
            saveImportedShapes(context, currentCustom + finalShape)
            Result.success(finalShape)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun importShapeFromXml(
        context: Context,
        xmlContent: String,
        customLabel: String? = null
    ): Result<QsShapeItem> {
        return try {
            val labelToUse = customLabel?.trim()?.takeIf { it.isNotEmpty() } ?: "Custom Shape"
            val shape = parseXmlDrawable(xmlContent, labelToUse)
            val finalShape = if (!customLabel.isNullOrBlank()) shape.copy(label = customLabel.trim()) else shape

            val currentCustom = _allShapes.value.filter { it.isCustom }
            saveImportedShapes(context, currentCustom + finalShape)
            Result.success(finalShape)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Universal import method: accepts either XML or raw path data.
     */
    fun importShape(
        context: Context,
        input: String,
        customLabel: String? = null,
        viewportWidth: Float = 100f,
        viewportHeight: Float = 100f
    ): Result<QsShapeItem> {
        val trimmed = input.trim()
        return if (trimmed.startsWith("<") || trimmed.contains("<vector") || trimmed.contains("<path")) {
            importShapeFromXml(context, trimmed, customLabel)
        } else {
            importShapeFromPathData(context, trimmed, customLabel, viewportWidth, viewportHeight)
        }
    }

    fun importShapeFromUri(
        context: Context,
        uri: Uri,
        customLabel: String? = null
    ): Result<QsShapeItem> {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return Result.failure(IllegalArgumentException("Unable to read selected file"))
            val xml = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

            val derivedName = customLabel?.takeIf { it.isNotBlank() } ?: run {
                var fileName: String? = null
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            fileName = cursor.getString(nameIndex)
                        }
                    }
                }
                fileName?.removeSuffix(".xml") ?: "Imported Shape"
            }

            importShapeFromXml(context, xml, derivedName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteImportedShape(context: Context, shapeId: String): Boolean {
        val currentCustom = _allShapes.value.filter { it.isCustom }
        val updated = currentCustom.filterNot { it.id == shapeId }
        if (updated.size != currentCustom.size) {
            saveImportedShapes(context, updated)
            shapeCache.remove(shapeId)
            return true
        }
        return false
    }

    fun getShapeItem(shapeId: String?): QsShapeItem {
        val shapes = _allShapes.value
        return shapes.firstOrNull { 
            it.id.equals(shapeId, ignoreCase = true) || 
            (it.id == "pill" && (
                shapeId.equals("12dp_square", true) || 
                shapeId.equals("12dp square", true) ||
                shapeId.equals("12dp", true) ||
                shapeId.equals("8dp_square", true) || 
                shapeId.equals("8dp square", true) ||
                shapeId.equals("8dp", true) ||
                shapeId.equals("16dp_square", true) || 
                shapeId.equals("16dp square", true)
            ))
        }
            ?: shapes.firstOrNull { it.id == "circle" }
            ?: BUILT_IN_SHAPES.first()
    }

    fun getComposeShape(shapeId: String?): Shape {
        val item = getShapeItem(shapeId)
        return shapeCache.getOrPut(item.id) {
            VectorPathShape(item.pathData, item.viewportWidth, item.viewportHeight)
        }
    }

    fun createDrawable(shapeId: String?, color: Int = Color.WHITE): QsPathDrawable {
        val item = getShapeItem(shapeId)
        return QsPathDrawable(item.pathData, item.viewportWidth, item.viewportHeight, color)
    }
}
